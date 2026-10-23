package com.cloudBasedStorageService.StorageContainer.config;

import com.cloudBasedStorageService.StorageContainer.model.Role;
import com.cloudBasedStorageService.StorageContainer.model.User;
import com.cloudBasedStorageService.StorageContainer.repo.UserRepository;
import com.cloudBasedStorageService.StorageContainer.service.JwtService;
import com.cloudBasedStorageService.StorageContainer.util.Emails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final String frontendBaseUrl;

    public OAuth2SuccessHandler(
            UserRepository userRepository,
            JwtService jwtService,
            @Value("${app.auth.frontend-base-url}") String frontendBaseUrl) {

        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication)
            throws IOException, ServletException {

        OAuth2User oauthUser =
                (OAuth2User) authentication.getPrincipal();

        String email = Emails.normalize(oauthUser.getAttribute("email"));

        if (email == null || email.isBlank()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Google email not available"
            );
            return;
        }

        User user = userRepository
                .findByUserEmail(email)
                .orElseGet(() -> {

                    User newUser = new User();

                    newUser.setUserEmail(email);

                    newUser.setUserRole(Role.PUBLIC_USER);

                    return userRepository.save(newUser);
                });

        Role role = user.getUserRole() == null ? Role.PUBLIC_USER : user.getUserRole();

        String token = jwtService.generateToken(
                org.springframework.security.core.userdetails.User
                        .withUsername(user.getUserEmail())
                        .password("")
                        .authorities("ROLE_" + role.name())
                        .build()
        );

        // The token travels as a URL fragment so it is never sent to any server,
        // never lands in access logs, and is stripped from history by the client.
        String redirectUrl =
                frontendBaseUrl + "/oauth2/success#token=" + token;

        getRedirectStrategy()
                .sendRedirect(request, response, redirectUrl);
    }
}
