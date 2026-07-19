package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.MessSettings;

@Mapper(componentModel = "spring")
public interface MessSettingsMapper {

    MessSettings toEntity(
            CreateMessSettingsRequest request
    );

    MessSettingsResponse toResponse(
            MessSettings settings
    );

}