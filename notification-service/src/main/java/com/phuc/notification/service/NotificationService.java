package com.phuc.notification.service;

import com.phuc.notification.dto.request.NotificationCreateRequest;
import com.phuc.notification.dto.request.NotificationUpdateRequest;
import com.phuc.notification.dto.response.NotificationResponse;
import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationCreateRequest request);
    NotificationResponse updateNotification(NotificationUpdateRequest request, Long notificationId);
    NotificationResponse getNotificationById(Long id);
    List<NotificationResponse> getAllNotifications();
    void deleteNotification(Long id);
}
