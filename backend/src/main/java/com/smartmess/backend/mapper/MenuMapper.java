package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.entity.Menu;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    /*
     * Maps menu details using the existing Menu builder.
     *
     * The service assigns the authenticated mess.
     * Menu identity is generated during persistence.
     * Inherited timestamps are managed by JPA auditing
     * and are not fields of the Menu builder.
     */
    @Mapping(target = "menuId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    Menu toEntity(CreateMenuRequest request);

    MenuResponse toResponse(Menu menu);
}