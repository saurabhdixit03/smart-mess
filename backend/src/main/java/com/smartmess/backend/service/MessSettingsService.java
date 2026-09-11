package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdatePaymentSettingsRequest;
import com.smartmess.backend.dto.request.UpdateResponseWindowRequest;
import com.smartmess.backend.dto.request.UpdateWeeklyScheduleRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;

public interface MessSettingsService {

    MessSettingsResponse createSettings(
            CreateMessSettingsRequest request
    );

    MessSettingsResponse getSettings();

    MessSettingsResponse updatePaymentSettings(
            UpdatePaymentSettingsRequest request
    );

    MessSettingsResponse updateResponseWindow(
            UpdateResponseWindowRequest request
    );

    MessSettingsResponse updateWeeklySchedule(
            UpdateWeeklyScheduleRequest request
    );

}