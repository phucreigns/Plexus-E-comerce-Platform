package com.example.Ecom;

import com.phuc.notification.service.EmailService;
import com.phuc.notification.dto.request.SendEmailRequest;
import com.phuc.notification.dto.response.EmailResponse;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import jakarta.mail.internet.MimeMessage;

import static org.mockito.Mockito.*;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return new EmailService() {
            @Override
            public EmailResponse sendEmail(SendEmailRequest request) {
                // Log the email request for testing purposes
                System.out.println("Mock Email Service - Sending email to: " + request.getTo().getEmail());
                System.out.println("Subject: " + request.getSubject());
                System.out.println("Content: " + request.getHtmlContent());
                
                return EmailResponse.builder()
                        .messageId("test-message-id-" + System.currentTimeMillis())
                        .build();
            }
        };
    }

    @Bean
    @Primary
    public JavaMailSender mockJavaMailSender() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Mock the send method to avoid actual email sending
        doNothing().when(mailSender).send(any(MimeMessage.class));
        
        return mailSender;
    }
}
