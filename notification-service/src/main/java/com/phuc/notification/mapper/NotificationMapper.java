package com.phuc.notification.mapper;

import com.phuc.notification.dto.request.NotificationCreateRequest;
import com.phuc.notification.dto.request.NotificationUpdateRequest;
import com.phuc.notification.dto.response.NotificationResponse;
import com.phuc.notification.entity.Notification;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    Notification toNotification(NotificationCreateRequest request);

    NotificationResponse toNotificationResponse(Notification notification);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateNotification(@MappingTarget Notification notification, NotificationUpdateRequest request);

}
