package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.DishResponse;
import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.enums.DishCategory;
import com.smartmess.backend.mapper.DishMapper;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DishService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/dishes")
public class DishController {

    private final DishService dishService;
    private final CustomerSecurity customerSecurity;
    private final DishMapper dishMapper;

    public DishController(
            DishService dishService,
            CustomerSecurity customerSecurity,
            DishMapper dishMapper) {

        this.dishService = dishService;
        this.customerSecurity = customerSecurity;
        this.dishMapper = dishMapper;
    }

    /**
     * Searches existing active dishes for the authenticated owner's mess.
     *
     * Example:
     * GET /api/dishes/search?category=SABJI&query=bhindi
     *
     * If query is empty, active dishes for the selected category
     * are returned.
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<List<DishResponse>>> searchDishes(
            @RequestParam DishCategory category,
            @RequestParam(required = false) String query,
            HttpServletRequest request) {

        Long messId =
                customerSecurity.getCurrentMessId();

        List<Dish> dishes =
                dishService.searchActiveDishes(
                        messId,
                        category,
                        query
                );

        List<DishResponse> response =
                dishes.stream()
                        .map(dishMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dishes fetched successfully.",
                        request.getRequestURI(),
                        response
                )
        );
    }
}