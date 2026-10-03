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

    /*
     * Maps closure details.
     * The service assigns the authenticated mess.
     */
    @Mapping(target = "closureId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MessClosure toEntity(
            CreateMessClosureRequest request
    );

    MessClosureResponse toResponse(
            MessClosure closure
    );

    /*
     * Updates closure details without changing tenant ownership,
     * record identity or auditing fields.
     */
    @Mapping(target = "closureId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateClosureFromRequest(
            UpdateMessClosureRequest request,
            @MappingTarget MessClosure closure
    );
}