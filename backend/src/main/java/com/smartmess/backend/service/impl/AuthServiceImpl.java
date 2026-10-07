package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.constant.AppConstants;
import com.smartmess.backend.dto.request.CustomerLoginRequest;
import com.smartmess.backend.dto.request.CustomerRegistrationRequest;
import com.smartmess.backend.dto.request.OwnerLoginRequest;
import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.dto.response.CustomerLoginResponse;
import com.smartmess.backend.dto.response.CustomerRegistrationResponse;
import com.smartmess.backend.dto.response.OwnerLoginResponse;
import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.CustomerStatus;
import com.smartmess.backend.enums.MessOwnerStatus;
import com.smartmess.backend.enums.UserRole;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.CustomerMapper;
import com.smartmess.backend.mapper.MessOwnerMapper;
import com.smartmess.backend.repository.CustomerRepository;
import com.smartmess.backend.repository.MessOwnerRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.security.JwtService;
import com.smartmess.backend.service.AuthService;
import com.smartmess.backend.service.MessConfigurationInitializer;

@Service
public class AuthServiceImpl implements AuthService {

    private final MessOwnerRepository messOwnerRepository;
    private final MessOwnerMapper messOwnerMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;
    private final Clock clock;
    private final MessRepository messRepository;
    private final MessConfigurationInitializer messConfigurationInitializer;

    public AuthServiceImpl(
            MessOwnerRepository messOwnerRepository,
            MessOwnerMapper messOwnerMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomerRepository customerRepository,
            CustomerMapper customerMapper,
            Clock clock,
            MessRepository messRepository,
            MessConfigurationInitializer messConfigurationInitializer) {

        this.messOwnerRepository = messOwnerRepository;
        this.messOwnerMapper = messOwnerMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
        this.clock = clock;
        this.messRepository = messRepository;
        this.messConfigurationInitializer = messConfigurationInitializer;
    }

    private OwnerLoginResponse buildLoginResponse(
            MessOwner owner,
            String accessToken) {

        return new OwnerLoginResponse(
                accessToken,
                AppConstants.TOKEN_TYPE_BEARER,
                owner.getMessOwnerId(),
                owner.getFullName(),
                owner.getMess().getMessName(),
                owner.getMess().getMessId()
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
                customer.getMobileNumber(),
                customer.getMess().getMessName()
        );
    }

    @Transactional
    @Override
    public OwnerLoginResponse registerOwner(
            OwnerRegistrationRequest request) {

        if (messOwnerRepository.existsByMobileNumber(
                request.mobileNumber())) {
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

        Mess mess = new Mess();
        mess.setMessName(request.messName());
        mess.setRegistrationCode(UUID.randomUUID().toString());

        Mess savedMess = messRepository.save(mess);

        owner.setMess(savedMess);

        MessOwner savedOwner = messOwnerRepository.save(owner);

        /*
         * Initialize operational configuration within the
         * same owner registration transaction.
         */
        messConfigurationInitializer.initialize(savedMess);

        String accessToken = jwtService.generateToken(
                savedOwner.getEmail(),
                UserRole.OWNER
        );

        return buildLoginResponse(savedOwner, accessToken);
    }

    @Transactional(readOnly = true)
    @Override
    public OwnerLoginResponse loginOwner(
            OwnerLoginRequest request) {

        MessOwner owner = messOwnerRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        "Invalid email or password."
                ));

        if (!passwordEncoder.matches(
                request.password(),
                owner.getPassword())) {
            throw new BusinessException(
                    "Invalid email or password."
            );
        }

        if (owner.getStatus() != MessOwnerStatus.ACTIVE) {
            throw new BusinessException(
                    "Your account is inactive. Please contact support."
            );
        }

        String accessToken = jwtService.generateToken(
                owner.getEmail(),
                UserRole.OWNER
        );

        return buildLoginResponse(owner, accessToken);
    }

    /*
     * Self-registration creates a pending account.
     * Approval is required before the customer's first login.
     * Registration does not issue an access token.
     */
    @Transactional
    @Override
    public CustomerRegistrationResponse registerCustomer(
            CustomerRegistrationRequest request) {

        Mess mess = messRepository
                .findByRegistrationCode(request.registrationCode())
                .orElseThrow(() -> new BusinessException(
                        "Invalid mess registration link."
                ));

        if (customerRepository.existsByMobileNumber(
                request.mobileNumber())) {
            throw new BusinessException(
                    "A customer with this mobile number already exists."
            );
        }

        if (customerRepository.existsByEmail(request.email())) {
            throw new BusinessException(
                    "A customer with this email already exists."
            );
        }

        Customer customer = customerMapper.toEntity(request);

        customer.setMess(mess);
        customer.setJoiningDate(LocalDate.now(clock));
        customer.setStatus(CustomerStatus.PENDING);
        customer.setPassword(
                passwordEncoder.encode(request.password())
        );

        Customer savedCustomer = customerRepository.save(customer);

        return new CustomerRegistrationResponse(
                savedCustomer.getCustomerId(),
                savedCustomer.getFullName(),
                savedCustomer.getMobileNumber(),
                savedCustomer.getStatus()
        );
    }

    @Transactional(readOnly = true)
    @Override
    public CustomerLoginResponse loginCustomer(
            CustomerLoginRequest request) {

        Customer customer = customerRepository
                .findByEmail(request.email())
                .orElseThrow(() -> new BusinessException(
                        "Invalid email or password."
                ));

        if (!passwordEncoder.matches(
                request.password(),
                customer.getPassword())) {
            throw new BusinessException(
                    "Invalid email or password."
            );
        }

        if (customer.getStatus() == CustomerStatus.PENDING) {
            throw new BusinessException(
                    "Your registration is awaiting approval from your mess owner."
            );
        }

        /*
         * Inactive customers can sign in to access their
         * profile, meal history, bills and payments.
         *
         * Daily participation requires ACTIVE status in the
         * respective operational services.
         */
        if (customer.getStatus() != CustomerStatus.ACTIVE
                && customer.getStatus() != CustomerStatus.INACTIVE) {
            throw new BusinessException(
                    "Your account is not available for login. Please contact your mess owner."
            );
        }

        String accessToken = jwtService.generateToken(
                customer.getEmail(),
                UserRole.CUSTOMER
        );

        return buildCustomerLoginResponse(customer, accessToken);
    }
}