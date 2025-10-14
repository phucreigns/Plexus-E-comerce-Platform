package com.phuc.notification.service.Impl;

import com.phuc.notification.dto.request.NotificationCreateRequest;
import com.phuc.notification.dto.request.NotificationUpdateRequest;
import com.phuc.notification.dto.response.NotificationResponse;
import com.phuc.notification.entity.Notification;
import com.phuc.notification.exception.ResourceNotFoundException;
import com.phuc.notification.mapper.NotificationMapper;
import com.phuc.notification.repository.NotificationRepository;
import com.phuc.notification.service.EmailService;
import com.phuc.notification.service.NotificationService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    NotificationMapper notificationMapper;
    EmailService emailService;

    @Override
    public NotificationResponse createNotification(NotificationCreateRequest request) {
        Notification notification = notificationMapper.toNotification(request);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setUpdatedAt(LocalDateTime.now());
        
        // Save notification to database
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send email notification
        try {
            if (savedNotification.getEmail() != null && !savedNotification.getEmail().isEmpty()) {
                emailService.sendNotificationEmail(
                    savedNotification.getEmail(),
                    savedNotification.getTitle(),
                    savedNotification.getContent()
                );
                log.info("Email notification sent successfully to: {}", savedNotification.getEmail());
            } else {
                log.warn("No email address provided for notification ID: {}", savedNotification.getNotificationId());
            }
        } catch (Exception e) {
            log.error("Failed to send email notification for ID: {}, error: {}", 
                savedNotification.getNotificationId(), e.getMessage(), e);
            // Don't throw exception - notification is still saved in database
        }
        
        return notificationMapper.toNotificationResponse(savedNotification);
    }

    @Override
    public NotificationResponse updateNotification(NotificationUpdateRequest request, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        notificationMapper.updateNotification(notification, request);
        notification.setUpdatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    public NotificationResponse getNotificationById(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        return notificationMapper.toNotificationResponse(notification);
    }

    @Override
    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAll()
                .stream()
                .map(notificationMapper::toNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteNotification(Long id) {
        notificationRepository.deleteById(id);
    }
}