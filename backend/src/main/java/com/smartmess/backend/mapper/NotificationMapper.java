package com.smartmess.backend.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.response.NotificationResponse;
import com.smartmess.backend.entity.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(
            source = "customer.customerId",
            target = "customerId"
    )
    NotificationResponse toResponse(
            Notification notification
    );

    List<NotificationResponse> toResponseList(
            List<Notification> notifications
    );
}