package com.smartmess.backend.service.impl;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdateMessSettingsRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MessSettingsMapper;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.service.MessSettingsService;

@Service
public class MessSettingsServiceImpl
        implements MessSettingsService {

    private final MessSettingsRepository messSettingsRepository;

    private final MessSettingsMapper messSettingsMapper;

    public MessSettingsServiceImpl(
            MessSettingsRepository messSettingsRepository,
            MessSettingsMapper messSettingsMapper) {

        this.messSettingsRepository = messSettingsRepository;
        this.messSettingsMapper = messSettingsMapper;

    }

    @Override
    public MessSettingsResponse createSettings(
            CreateMessSettingsRequest request) {

        if (messSettingsRepository
                .findTopByOrderBySettingsIdAsc()
                .isPresent()) {

            throw new BusinessException(
                    "Mess settings already exist. Please update the existing settings."
            );

        }

        MessSettings settings =
                messSettingsMapper.toEntity(request);

        MessSettings savedSettings =
                messSettingsRepository.save(settings);

        return messSettingsMapper.toResponse(
                savedSettings
        );

    }

    @Override
    public MessSettingsResponse getSettings() {

        MessSettings settings =
                messSettingsRepository
                        .findTopByOrderBySettingsIdAsc()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess settings not found."
                                )
                        );

        return messSettingsMapper.toResponse(
                settings
        );

    }

    @Override
    public MessSettingsResponse updateSettings(
            UpdateMessSettingsRequest request) {

        MessSettings settings =
                messSettingsRepository
                        .findTopByOrderBySettingsIdAsc()
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess settings not found."
                                )
                        );

        settings.setUpiId(
                request.upiId()
        );

        settings.setReceiverName(
                request.receiverName()
        );

        MessSettings updatedSettings =
                messSettingsRepository.save(
                        settings
                );

        return messSettingsMapper.toResponse(
                updatedSettings
        );

    }

}