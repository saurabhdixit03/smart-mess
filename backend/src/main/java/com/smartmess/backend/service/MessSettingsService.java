package com.smartmess.backend.service;

import com.smartmess.backend.dto.request.UpdateResponseWindowRequest;
import com.smartmess.backend.dto.request.UpdateWeeklyScheduleRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;

public interface MessSettingsService {

    MessSettingsResponse getSettings();

    MessSettingsResponse updateResponseWindow(
            UpdateResponseWindowRequest request
    );

    MessSettingsResponse updateWeeklySchedule(
            UpdateWeeklyScheduleRequest request
    );
}