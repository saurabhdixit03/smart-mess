package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuResponse;

public interface MenuService {

    MenuResponse publishMenu(CreateMenuRequest request);

    List<MenuResponse> getTodayMenus();

    MenuResponse getMenuById(Long menuId);

    List<MenuResponse> getMenuHistory();

}