package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.service.MenuService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menus")
@Validated
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<MenuResponse> publishMenu(
            @Valid @RequestBody CreateMenuRequest request,
            HttpServletRequest httpRequest) {

        MenuResponse response = menuService.publishMenu(request);

        return ApiResponse.success(
                "Menu published successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/today")
    public ApiResponse<List<MenuResponse>> getTodayMenus(
            HttpServletRequest httpRequest) {

        List<MenuResponse> response = menuService.getTodayMenus();

        return ApiResponse.success(
                "Today's menus fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/history")
    public ApiResponse<List<MenuResponse>> getMenuHistory(
            HttpServletRequest httpRequest) {

        List<MenuResponse> response = menuService.getMenuHistory();

        return ApiResponse.success(
                "Menu history fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/{menuId}")
    public ApiResponse<MenuResponse> getMenuById(
            @PathVariable Long menuId,
            HttpServletRequest httpRequest) {

        MenuResponse response = menuService.getMenuById(menuId);

        return ApiResponse.success(
                "Menu fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

}