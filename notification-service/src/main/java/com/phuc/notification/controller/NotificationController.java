package com.phuc.notification.controller;

import com.phuc.notification.dto.request.EmailRequest;
import com.phuc.notification.dto.request.NotificationCreateRequest;
import com.phuc.notification.dto.request.NotificationUpdateRequest;
import com.phuc.notification.dto.response.NotificationResponse;
import com.phuc.notification.service.EmailService;
import com.phuc.notification.service.NotificationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;
    EmailService emailService;

    @PostMapping("/create")
    public NotificationResponse createNotification(@RequestBody NotificationCreateRequest request) {
        return notificationService.createNotification(request);
    }

    @GetMapping("/{id}")
    public NotificationResponse getNotificationById(@PathVariable Long id) {
        return notificationService.getNotificationById(id);
    }

    @GetMapping("/all")
    public List<NotificationResponse> getAllNotifications() {
        return notificationService.getAllNotifications();
    }

    @PutMapping("/update/{notificationId}")
    public NotificationResponse updateNotification(@RequestBody NotificationUpdateRequest request, @PathVariable Long notificationId){
        return notificationService.updateNotification(request, notificationId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }

    // Email endpoints
    @PostMapping("/send-email")
    public ResponseEntity<String> sendEmail(@RequestBody EmailRequest emailRequest) {
        try {
            emailService.sendEmail(emailRequest);
            return ResponseEntity.ok("Email sent successfully to: " + emailRequest.getTo());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }

    @PostMapping("/send-simple-email")
    public ResponseEntity<String> sendSimpleEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String content) {
        try {
            emailService.sendSimpleEmail(to, subject, content);
            return ResponseEntity.ok("Simple email sent successfully to: " + to);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to send email: " + e.getMessage());
        }
    }
}
