package com.smartmess.backend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.OwnerLoginRequest;
import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.dto.response.OwnerLoginResponse;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.MessOwnerStatus;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.MessOwnerMapper;
import com.smartmess.backend.repository.MessOwnerRepository;
import com.smartmess.backend.security.JwtService;
import com.smartmess.backend.service.AuthService;

import com.smartmess.backend.constant.AppConstants;

@Service
public class AuthServiceImpl implements AuthService {

    private final MessOwnerRepository messOwnerRepository;
    private final MessOwnerMapper messOwnerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(
            MessOwnerRepository messOwnerRepository,
            MessOwnerMapper messOwnerMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.messOwnerRepository = messOwnerRepository;
        this.messOwnerMapper = messOwnerMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private OwnerLoginResponse buildLoginResponse(
            MessOwner owner,
            String accessToken) {

        return new OwnerLoginResponse(
                accessToken,
                AppConstants.TOKEN_TYPE_BEARER,
                owner.getMessOwnerId(),
                owner.getFullName(),
                owner.getMessName()
        );
    }
    
    @Override
    public OwnerLoginResponse registerOwner(
            OwnerRegistrationRequest request) {

        if (messOwnerRepository.existsByMobileNumber(request.mobileNumber())) {
            throw new BusinessException(
                    "A mess owner with this mobile number already exists."
            );
        }

        if (messOwnerRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    "A mess owner with this email already exists."
            );
        }

        MessOwner owner = messOwnerMapper.toEntity(request);
        
        owner.setPassword(
                passwordEncoder.encode(request.password())
        );
        
        MessOwner savedOwner = messOwnerRepository.save(owner);
        
        String accessToken = jwtService.generateToken(
                owner.getMobileNumber(),
                UserRole.OWNER
        );

        return buildLoginResponse(owner, accessToken);
    }

    @Override
    public OwnerLoginResponse loginOwner(OwnerLoginRequest request) {

        MessOwner owner = messOwnerRepository.findByMobileNumber(
                request.mobileNumber())
                .orElseThrow(() ->
                        new BusinessException("Invalid mobile number or password."));

        if (!passwordEncoder.matches(
                request.password(),
                owner.getPassword())) {

            throw new BusinessException(
                    "Invalid mobile number or password."
            );
        }

        if (owner.getStatus() != MessOwnerStatus.ACTIVE) {
            throw new BusinessException(
                    "Your account is inactive. Please contact support."
            );
        }

        String accessToken = jwtService.generateToken(
                owner.getMobileNumber(),
                UserRole.OWNER
        );

        return buildLoginResponse(owner, accessToken);    }
}