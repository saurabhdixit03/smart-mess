package com.smartmess.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartmess.backend.dto.response.ApiResponse;
import com.smartmess.backend.dto.response.NotificationResponse;
import com.smartmess.backend.service.NotificationService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService) {

        this.notificationService = notificationService;
    }

    /*
     * CUSTOMER ONLY
     *
     * Returns all notifications for
     * the currently authenticated customer.
     */
    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
            getMyNotifications(
                    HttpServletRequest httpRequest) {

        List<NotificationResponse> response =
                notificationService.getMyNotifications();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notifications fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * CUSTOMER ONLY
     *
     * Returns only unread notifications for
     * the currently authenticated customer.
     */
    @GetMapping("/unread")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>>
            getMyUnreadNotifications(
                    HttpServletRequest httpRequest) {

        List<NotificationResponse> response =
                notificationService
                        .getMyUnreadNotifications();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Unread notifications fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * CUSTOMER ONLY
     *
     * Returns the unread notification count
     * for the currently authenticated customer.
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Long>>
            getMyUnreadCount(
                    HttpServletRequest httpRequest) {

        long response =
                notificationService.getMyUnreadCount();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Unread notification count fetched successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * CUSTOMER ONLY
     *
     * Marks one notification as read.
     *
     * The service verifies that the notification
     * belongs to the authenticated customer.
     */
    @PatchMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<NotificationResponse>>
            markAsRead(
                    @PathVariable Long notificationId,
                    HttpServletRequest httpRequest) {

        NotificationResponse response =
                notificationService.markAsRead(
                        notificationId
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Notification marked as read successfully.",
                        httpRequest.getRequestURI(),
                        response
                )
        );
    }

    /*
     * CUSTOMER ONLY
     *
     * Marks all unread notifications for
     * the authenticated customer as read.
     */
    @PatchMapping("/read-all")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<Void>>
            markAllAsRead(
                    HttpServletRequest httpRequest) {

        notificationService.markAllAsRead();

        return ResponseEntity.ok(
                ApiResponse.success(
                        "All notifications marked as read successfully.",
                        httpRequest.getRequestURI(),
                        null
                )
        );
    }
}