package com.smartmess.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.entity.Menu;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    /*
     * Dish fields are intentionally ignored here.
     *
     * The service is responsible for resolving the submitted
     * dish names into tenant-scoped Dish entities.
     *
     * The service then assigns those Dish entities to Menu.
     */
    @Mapping(target = "menuId", ignore = true)
    @Mapping(target = "mess", ignore = true)
    @Mapping(target = "sabjiOne", ignore = true)
    @Mapping(target = "sabjiTwo", ignore = true)
    @Mapping(target = "dal", ignore = true)
    @Mapping(target = "rice", ignore = true)
    @Mapping(target = "sweet", ignore = true)
    Menu toEntity(CreateMenuRequest request);

    /*
     * The API continues to expose dish names rather than
     * exposing the internal Dish entity structure.
     *
     * Example:
     *
     * Menu.sabjiOne -> Dish("Bhindi Masala")
     *                         ↓
     * MenuResponse.sabjiOne -> "Bhindi Masala"
     */
    @Mapping(
            target = "sabjiOne",
            source = "sabjiOne.name"
    )
    @Mapping(
            target = "sabjiTwo",
            source = "sabjiTwo.name"
    )
    @Mapping(
            target = "dal",
            source = "dal.name"
    )
    @Mapping(
            target = "rice",
            source = "rice.name"
    )
    @Mapping(
            target = "sweet",
            source = "sweet.name"
    )
    MenuResponse toResponse(Menu menu);
}