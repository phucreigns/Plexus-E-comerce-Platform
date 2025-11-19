package com.phuc.notification.controller;

import com.phuc.notification.dto.request.SendEmailRequest;
import com.phuc.notification.dto.response.EmailResponse;
import com.phuc.notification.service.EmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

      EmailService emailService;

      @PostMapping("/send-email")
      public ResponseEntity<EmailResponse> sendEmail(@RequestBody SendEmailRequest request) {
            log.info("Received email send request for: {}", request.getTo().getEmail());
            EmailResponse response = emailService.sendEmail(request);
            return ResponseEntity.ok(response);
      }

}
