package com.smartmess.backend.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.request.OwnerLoginRequest;
import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.dto.response.CustomerLoginResponse;
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

import com.smartmess.backend.dto.request.CustomerLoginRequest;
import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.response.CustomerLoginResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.mapper.CustomerMapper;
import com.smartmess.backend.repository.CustomerRepository;


@Service
public class AuthServiceImpl implements AuthService {

    private final MessOwnerRepository messOwnerRepository;
    private final MessOwnerMapper messOwnerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    public AuthServiceImpl(
            MessOwnerRepository messOwnerRepository,
            MessOwnerMapper messOwnerMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomerRepository customerRepository,
            CustomerMapper customerMapper) {

        this.messOwnerRepository = messOwnerRepository;
        this.messOwnerMapper = messOwnerMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
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
    
    private CustomerLoginResponse buildCustomerLoginResponse(
            Customer customer,
            String accessToken) {

        return new CustomerLoginResponse(
                accessToken,
                AppConstants.TOKEN_TYPE_BEARER,
                customer.getCustomerId(),
                customer.getFullName(),
                customer.getMobileNumber()
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

        return buildLoginResponse(owner, accessToken); 
        
    }

    @Override
    public CustomerLoginResponse registerCustomer(
            CustomerRegistrationRequest request) {

        if (customerRepository.existsByMobileNumber(
                request.mobileNumber())) {

            throw new BusinessException(
                    "A customer with this mobile number already exists."
            );
        }

        Customer customer =
                customerMapper.toEntity(request);

        customer.setPassword(
                passwordEncoder.encode(request.password())
        );

        Customer savedCustomer =
                customerRepository.save(customer);

        String accessToken =
                jwtService.generateToken(
                        savedCustomer.getMobileNumber(),
                        UserRole.CUSTOMER
                );

        return buildCustomerLoginResponse(
                savedCustomer,
                accessToken
        );
    }

    @Override
    public CustomerLoginResponse loginCustomer(
            CustomerLoginRequest request) {

        Customer customer =
                customerRepository.findByMobileNumber(
                        request.mobileNumber()
                )
                .orElseThrow(() ->
                        new BusinessException(
                                "Invalid mobile number or password."
                        ));

        if (!passwordEncoder.matches(
                request.password(),
                customer.getPassword())) {

            throw new BusinessException(
                    "Invalid mobile number or password."
            );
        }

        if (customer.getStatus() != CustomerStatus.ACTIVE) {

            throw new BusinessException(
                    "Your account is inactive. Please contact your mess owner."
            );
        }

        String accessToken =
                jwtService.generateToken(
                        customer.getMobileNumber(),
                        UserRole.CUSTOMER
                );

        return buildCustomerLoginResponse(
                customer,
                accessToken
        );
    }
}