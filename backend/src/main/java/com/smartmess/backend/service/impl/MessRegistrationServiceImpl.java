package com.smartmess.backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.smartmess.backend.dto.response.MessRegistrationInfoResponse;
import com.smartmess.backend.dto.response.MessRegistrationLinkResponse;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MessRegistrationService;

@Service
public class MessRegistrationServiceImpl
        implements MessRegistrationService {

    private final MessRepository messRepository;
    private final CustomerSecurity customerSecurity;
    private final String frontendUrl;

    public MessRegistrationServiceImpl(
            MessRepository messRepository,
            CustomerSecurity customerSecurity,
            @Value("${app.frontend.url}") String frontendUrl) {

        this.messRepository = messRepository;
        this.customerSecurity = customerSecurity;
        this.frontendUrl = frontendUrl;
    }

    /*
     * Returns the authenticated owner's existing registration link.
     *
     * The mess ID comes from the authenticated account.
     * The client cannot select another owner's mess.
     */
    @Override
    @PreAuthorize("hasRole('OWNER')")
    @Transactional(readOnly = true)
    public MessRegistrationLinkResponse getRegistrationLink() {

        Long messId =
                customerSecurity.getCurrentMessId();

        Mess mess =
                messRepository.findById(messId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess not found."
                                ));

        String registrationUrl =
                UriComponentsBuilder
                        .fromUriString(frontendUrl)
                        .replacePath("/customer/register")
                        .replaceQuery(null)
                        .fragment(null)
                        .queryParam(
                                "registrationCode",
                                mess.getRegistrationCode()
                        )
                        .build()
                        .encode()
                        .toUriString();

        return new MessRegistrationLinkResponse(
                mess.getMessId(),
                mess.getMessName(),
                mess.getRegistrationCode(),
                registrationUrl
        );
    }

    /*
     * Shows the mess name when a customer opens a registration link.
     *
     * The code selects the mess; it does not prove that the
     * person opening the link is an authorized customer.
     */
    @Override
    @Transactional(readOnly = true)
    public MessRegistrationInfoResponse getRegistrationInfo(
            String registrationCode) {

        Mess mess =
                messRepository
                        .findByRegistrationCode(registrationCode)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Invalid mess registration link."
                                ));

        return new MessRegistrationInfoResponse(
                mess.getMessName()
        );
    }
}