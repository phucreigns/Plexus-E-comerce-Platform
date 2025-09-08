package com.phuc.notification.controller;

import com.phuc.notification.dto.request.NotificationCreateRequest;
import com.phuc.notification.dto.request.NotificationUpdateRequest;
import com.phuc.notification.dto.response.NotificationResponse;
import com.phuc.notification.service.NotificationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {
    NotificationService notificationService;

    @PostMapping("/create")
    public NotificationResponse createNotification(NotificationCreateRequest request) {
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
    public NotificationResponse updateNotification(NotificationUpdateRequest request, @PathVariable Long notificationId){
        return notificationService.updateNotification(request, notificationId);
    }

    @DeleteMapping("/{id}")
    public void deleteUserById(@PathVariable Long id) {
        notificationService.deleteNotification(id);
    }



}
