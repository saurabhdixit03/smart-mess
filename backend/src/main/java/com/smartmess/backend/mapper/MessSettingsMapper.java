package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdatePaymentSettingsRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.MessSettings;

@Mapper(componentModel = "spring")
public interface MessSettingsMapper {

    /*
     * Maps payment details when creating settings.
     *
     * The service assigns mess ownership.
     * Other configuration keeps its existing entity defaults.
     */
    @Mapping(target = "settingsId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "lunchResponseCutoff", ignore = true)
    @Mapping(target = "dinnerResponseCutoff", ignore = true)
    @Mapping(target = "weeklyClosedDay", ignore = true)
    @Mapping(target = "weeklyLunchClosed", ignore = true)
    @Mapping(target = "weeklyDinnerClosed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MessSettings toEntity(
            CreateMessSettingsRequest request
    );

    /*
     * Updates payment details only.
     *
     * Tenant ownership, response cutoffs, weekly schedule,
     * identity and auditing fields remain protected.
     */
    @Mapping(target = "settingsId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "lunchResponseCutoff", ignore = true)
    @Mapping(target = "dinnerResponseCutoff", ignore = true)
    @Mapping(target = "weeklyClosedDay", ignore = true)
    @Mapping(target = "weeklyLunchClosed", ignore = true)
    @Mapping(target = "weeklyDinnerClosed", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(
            UpdatePaymentSettingsRequest request,
            @MappingTarget MessSettings settings
    );

    MessSettingsResponse toResponse(
            MessSettings settings
    );
}