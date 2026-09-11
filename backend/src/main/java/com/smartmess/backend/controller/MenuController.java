package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.MenuAvailabilityResponse;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.service.MenuService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/menus")
@Validated
public class MenuController {

    private final MenuService menuService;

    public MenuController(
            MenuService menuService) {

        this.menuService = menuService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<MenuResponse> publishMenu(
            @Valid @RequestBody CreateMenuRequest request,
            HttpServletRequest httpRequest) {

        MenuResponse response =
                menuService.publishMenu(request);

        return ApiResponse.success(
                "Menu published successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('OWNER', 'CUSTOMER')")
    public ApiResponse<List<MenuResponse>> getTodayMenus(
            HttpServletRequest httpRequest) {

        List<MenuResponse> response =
                menuService.getTodayMenus();

        return ApiResponse.success(
                "Today's menus fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    /*
     * OWNER ONLY
     *
     * Returns whether today's Lunch and Dinner menus
     * can currently be published and, when blocked,
     * provides the reason for the owner UI.
     */
    @GetMapping("/today/availability")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<List<MenuAvailabilityResponse>>
            getTodayMenuAvailability(
                    HttpServletRequest httpRequest) {

        List<MenuAvailabilityResponse> response =
                menuService.getTodayMenuAvailability();

        return ApiResponse.success(
                "Today's menu availability fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<List<MenuResponse>> getMenuHistory(
            HttpServletRequest httpRequest) {

        List<MenuResponse> response =
                menuService.getMenuHistory();

        return ApiResponse.success(
                "Menu history fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }

    @GetMapping("/{menuId}")
    @PreAuthorize("hasRole('OWNER')")
    public ApiResponse<MenuResponse> getMenuById(
            @PathVariable Long menuId,
            HttpServletRequest httpRequest) {

        MenuResponse response =
                menuService.getMenuById(menuId);

        return ApiResponse.success(
                "Menu fetched successfully.",
                httpRequest.getRequestURI(),
                response
        );
    }
}