package com.cloudBasedStorageService.StorageContainer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResetEmailService {

    private final JavaMailSender mailSender;
    private final String from;
    private final String mailHost;

    public ResetEmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.host:}") String mailHost,
            @Value("${app.auth.reset-email-from}") String from) {

        this.mailSender = mailSender;
        this.mailHost = mailHost;
        this.from = from;
    }

    public void sendResetLink(String to, String resetLink) {

        if (mailHost == null || mailHost.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Password reset emails are not configured on this server. Set SMTP_HOST and restart.");
        }

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Reset your StorageContainer password");
        message.setText("""
                Someone asked to reset the password for %s.

                Open this link to choose a new password:

                %s

                The link works once and expires after 15 minutes.
                If you did not request it, you can ignore this email.
                """.formatted(to, resetLink));

        try {
            mailSender.send(message);
        } catch (MailException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Could not send the reset email. Please try again later.");
        }
    }
}
