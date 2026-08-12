package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdateMessSettingsRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.MessSettings;
import org.mapstruct.MappingTarget;
@Mapper(componentModel = "spring")
public interface MessSettingsMapper {

    MessSettings toEntity(
            CreateMessSettingsRequest request
    );

    void updateEntityFromRequest(
            UpdateMessSettingsRequest request,
            @MappingTarget MessSettings settings
    );
    
    MessSettingsResponse toResponse(
            MessSettings settings
    );

}