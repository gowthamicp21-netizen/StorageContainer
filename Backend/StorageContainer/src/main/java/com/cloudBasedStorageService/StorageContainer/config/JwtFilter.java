package com.cloudBasedStorageService.StorageContainer.config;

import com.cloudBasedStorageService.StorageContainer.service.JwtService;
import com.cloudBasedStorageService.StorageContainer.service.MyUserDetailService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private MyUserDetailService userDetailsService;

    @Autowired
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String userEmail = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")){
            token=authHeader.substring(7);

            try{
                userEmail = jwtService.extractUserEmail(token);
                if (userEmail != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null){
                            UserDetails userDetails =
                            userDetailsService.loadUserByUsername(userEmail);
                            if(jwtService.validateToken(token,userDetails)){
                                UsernamePasswordAuthenticationToken authentication =
                                        new UsernamePasswordAuthenticationToken(
                                                userDetails,
                                                null,
                                                userDetails.getAuthorities()
                                        );
                                authentication.setDetails(
                                        new WebAuthenticationDetailsSource()
                                                .buildDetails(request)
                                );

                                SecurityContextHolder.getContext()
                                        .setAuthentication(authentication);

                                System.out.println(
                                        "JWT USER: " + userEmail
                                );

                                System.out.println(
                                        "JWT AUTHORITIES: " +
                                                userDetails.getAuthorities()
                                );

                            }
                }
            }
            catch(Exception e){
                System.out.println("JWT validation failed: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);

    }
}
