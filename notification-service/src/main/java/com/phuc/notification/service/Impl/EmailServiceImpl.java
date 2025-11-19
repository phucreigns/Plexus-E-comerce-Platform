package com.phuc.notification.service.Impl;

import com.phuc.notification.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.phuc.notification.dto.request.SendEmailRequest;
import com.phuc.notification.dto.response.EmailResponse;
import com.phuc.notification.exception.AppException;
import com.phuc.notification.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {

      JavaMailSender mailSender;

      @Value("${spring.mail.username}")
      @NonFinal
      String fromEmail;

      @Value("${notification.email.from-name}")
      @NonFinal
      String fromName;

      public EmailResponse sendEmail(SendEmailRequest request) {
            try {
                  MimeMessage mimeMessage = mailSender.createMimeMessage();
                  MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                  
                  helper.setFrom(fromEmail, fromName);
                  helper.setTo(request.getTo().getEmail());
                  helper.setSubject(request.getSubject());
                  helper.setText(request.getHtmlContent(), true); // true indicates HTML content
                  
                  mailSender.send(mimeMessage);
                  
                  log.info("Email sent successfully to: {}", request.getTo().getEmail());
                  
                  return EmailResponse.builder()
                          .messageId("email-" + System.currentTimeMillis())
                          .build();
                          
            } catch (MessagingException e) {
                  log.error("Error sending email to {}: {}", request.getTo().getEmail(), e.getMessage(), e);
                  throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            } catch (Exception e) {
                  log.error("Unexpected error sending email to {}: {}", request.getTo().getEmail(), e.getMessage(), e);
                  throw new AppException(ErrorCode.SERVICE_UNAVAILABLE);
            }
      }

}