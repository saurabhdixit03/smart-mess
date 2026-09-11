package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdatePaymentSettingsRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.MessSettings;

@Mapper(componentModel = "spring")
public interface MessSettingsMapper {

    MessSettings toEntity(
            CreateMessSettingsRequest request
    );

    void updateEntityFromRequest(
            UpdatePaymentSettingsRequest request,
            @MappingTarget MessSettings settings
    );

    MessSettingsResponse toResponse(
            MessSettings settings
    );

}