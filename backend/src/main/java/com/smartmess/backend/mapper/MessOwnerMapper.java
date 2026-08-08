package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.request.OwnerRegistrationRequest;
import com.smartmess.backend.entity.MessOwner;

@Mapper(componentModel = "spring")
public interface MessOwnerMapper {

    @Mapping(target = "messOwnerId", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    MessOwner toEntity(OwnerRegistrationRequest request);

}