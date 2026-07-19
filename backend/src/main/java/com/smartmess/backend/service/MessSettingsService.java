package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdateMessSettingsRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;

public interface MessSettingsService {

    MessSettingsResponse createSettings(
            CreateMessSettingsRequest request
    );

    MessSettingsResponse getSettings();

    MessSettingsResponse updateSettings(
            UpdateMessSettingsRequest request
    );

}