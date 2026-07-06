package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.entity.Menu;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    @Mapping(target = "menuId", ignore = true)
    Menu toEntity(CreateMenuRequest request);

    MenuResponse toResponse(Menu menu);

}