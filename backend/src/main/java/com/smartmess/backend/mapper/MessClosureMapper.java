package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.smartmess.backend.dto.request.CreateMessClosureRequest;
import com.smartmess.backend.dto.request.UpdateMessClosureRequest;
import com.smartmess.backend.dto.response.MessClosureResponse;
import com.smartmess.backend.entity.MessClosure;

@Mapper(componentModel = "spring")
public interface MessClosureMapper {

    @Mapping(target = "closureId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MessClosure toEntity(
            CreateMessClosureRequest request
    );

    MessClosureResponse toResponse(
            MessClosure closure
    );

    @Mapping(target = "closureId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateClosureFromRequest(
            UpdateMessClosureRequest request,
            @MappingTarget MessClosure closure
    );
}