package com.smartmess.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.smartmess.backend.config.SecurityConfig;
import com.smartmess.backend.dto.response.DishIntelligenceResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceAnalysisResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceOverviewResponse;
import com.smartmess.backend.dto.response.ai.MenuIntelligenceSuggestionResponse;
import com.smartmess.backend.enums.SuggestionPriority;
import com.smartmess.backend.security.JwtAuthenticationFilter;
import com.smartmess.backend.security.SecurityExceptionHandler;
import com.smartmess.backend.service.MenuIntelligenceService;

@WebMvcTest(MenuIntelligenceController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuIntelligenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MenuIntelligenceService menuIntelligenceService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private SecurityExceptionHandler securityExceptionHandler;

    @Test
    @WithMockUser(roles = "OWNER")
    void shouldReturnIntelligenceOverviewForOwner() throws Exception {

        MenuIntelligenceAnalysisResponse analysis =
                new MenuIntelligenceAnalysisResponse(
                        LocalDate.of(2026, 10, 25),
                        LocalDate.of(2026, 10, 31),
                        LocalDateTime.of(2026, 10, 31, 23, 5),
                        1
                );

        MenuIntelligenceSuggestionResponse suggestion =
                new MenuIntelligenceSuggestionResponse(
                        "Paneer Masala",
                        "Customers frequently report excessive oil.",
                        "Consider reducing the oil quantity.",
                        SuggestionPriority.HIGH
                );

        MenuIntelligenceOverviewResponse response =
                new MenuIntelligenceOverviewResponse(
                        analysis,
                        List.of(suggestion)
                );

        when(menuIntelligenceService.getIntelligenceOverview())
                .thenReturn(response);

        mockMvc.perform(get("/api/menu-intelligence/overview")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.periodStart")
                        .value("2026-10-25"))
                .andExpect(jsonPath("$.analysis.periodEnd")
                        .value("2026-10-31"))
                .andExpect(jsonPath("$.analysis.suggestionCount")
                        .value(1))
                .andExpect(jsonPath("$.suggestions.length()")
                        .value(1))
                .andExpect(jsonPath("$.suggestions[0].dishName")
                        .value("Paneer Masala"))
                .andExpect(jsonPath("$.suggestions[0].priority")
                        .value("HIGH"));

        verify(menuIntelligenceService).getIntelligenceOverview();
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void shouldReturnDishRatingAnalyticsForOwner() throws Exception {

        when(menuIntelligenceService.getDishRatingAnalytics())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/menu-intelligence/dish-ratings")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(menuIntelligenceService).getDishRatingAnalytics();
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void shouldReturnDishFeedbackAnalyticsForOwner() throws Exception {

        when(menuIntelligenceService.getDishFeedbackAnalytics())
                .thenReturn(List.of());

        mockMvc.perform(get("/api/menu-intelligence/dish-feedback")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(menuIntelligenceService).getDishFeedbackAnalytics();
    }

    @Test
    @WithMockUser(roles = "OWNER")
    void shouldReturnConsolidatedDishIntelligenceForOwner() throws Exception {

        DishIntelligenceResponse.AiSuggestion aiSuggestion =
                new DishIntelligenceResponse.AiSuggestion(
                        "Customers report excessive oil.",
                        "Reduce the oil quantity.",
                        SuggestionPriority.HIGH
                );

        DishIntelligenceResponse dish =
                new DishIntelligenceResponse(
                        101L,
                        "Paneer Masala",
                        true,
                        10L,
                        4.2,
                        0L,
                        1L,
                        1L,
                        4L,
                        4L,
                        3L,
                        aiSuggestion
                );

        when(menuIntelligenceService.getDishIntelligence())
                .thenReturn(List.of(dish));

        mockMvc.perform(get("/api/menu-intelligence/dishes")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].dishId").value(101))
                .andExpect(jsonPath("$[0].dishName").value("Paneer Masala"))
                .andExpect(jsonPath("$[0].active").value(true))
                .andExpect(jsonPath("$[0].totalRatings").value(10))
                .andExpect(jsonPath("$[0].averageRating").value(4.2))
                .andExpect(jsonPath("$[0].totalComments").value(3))
                .andExpect(jsonPath("$[0].aiSuggestion.priority")
                        .value("HIGH"))
                .andExpect(jsonPath("$[0].aiSuggestion.observation")
                        .value("Customers report excessive oil."));

        verify(menuIntelligenceService).getDishIntelligence();
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldDenyCustomerAccessToIntelligenceOverview() throws Exception {

        mockMvc.perform(get("/api/menu-intelligence/overview"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldDenyCustomerAccessToDishIntelligence() throws Exception {

        mockMvc.perform(get("/api/menu-intelligence/dishes"))
                .andExpect(status().isForbidden());
    }
}