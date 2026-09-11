package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuAvailabilityResponse;
import com.smartmess.backend.dto.response.MenuResponse;

public interface MenuService {

    MenuResponse publishMenu(
            CreateMenuRequest request
    );

    List<MenuResponse> getTodayMenus();

    List<MenuAvailabilityResponse>
            getTodayMenuAvailability();

    MenuResponse getMenuById(
            Long menuId
    );

    List<MenuResponse> getMenuHistory();
}