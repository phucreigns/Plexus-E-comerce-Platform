package com.phuc.notification.service;

import com.phuc.notification.dto.request.EmailRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {

    JavaMailSender mailSender;
    TemplateEngine templateEngine;

    /**
     * Gửi email đơn giản (text)
     */
    public void sendSimpleEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    /**
     * Gửi email HTML với template Thymeleaf
     */
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            
            // Process template với variables
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {} with template: {}", to, templateName);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}, error: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Gửi email notification
     */
    public void sendNotificationEmail(String to, String title, String content) {
        Map<String, Object> variables = Map.of(
            "title", title,
            "content", content,
            "recipientEmail", to
        );
        
        sendHtmlEmail(to, title, "notification", variables);
    }

    /**
     * Gửi email với EmailRequest object
     */
    public void sendEmail(EmailRequest emailRequest) {
        if (emailRequest.getTemplateName() != null && !emailRequest.getTemplateName().isEmpty()) {
            sendHtmlEmail(
                emailRequest.getTo(),
                emailRequest.getSubject(),
                emailRequest.getTemplateName(),
                emailRequest.getVariables()
            );
        } else {
            sendSimpleEmail(
                emailRequest.getTo(),
                emailRequest.getSubject(),
                emailRequest.getContent()
            );
        }
    }
}

