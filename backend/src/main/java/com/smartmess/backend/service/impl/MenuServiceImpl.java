package com.smartmess.backend.service.impl;

import java.util.List;


import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.MenuMapper;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.service.MenuService;

import java.time.LocalDate;
import java.util.List;

@Service
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuRepository menuRepository,
                           MenuMapper menuMapper) {

        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
    }

    @Override
    public MenuResponse publishMenu(CreateMenuRequest request) {

        // Business Rule:
        // Only one menu may exist for a given date and meal session.
        if (menuRepository.existsByMenuDateAndMealSession(
                request.menuDate(),
                request.mealSession())) {

            throw new BusinessException(
                    "Menu already published for this date and meal session."
            );
        }

        Menu menu = menuMapper.toEntity(request);

        Menu savedMenu = menuRepository.save(menu);
        
        return menuMapper.toResponse(savedMenu);
    }

    @Override
    public List<MenuResponse> getTodayMenus() {

        List<Menu> menus = menuRepository
                .findByMenuDateOrderByMealSessionAsc(LocalDate.now());

        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    @Override
    public MenuResponse getMenuById(Long menuId) {

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new BusinessException(
                        "Menu not found with ID: " + menuId
                ));

        return menuMapper.toResponse(menu);
    }

    @Override
    public List<MenuResponse> getMenuHistory() {

        List<Menu> menus = menuRepository
                .findAllByOrderByMenuDateDescMealSessionAsc();

        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

}