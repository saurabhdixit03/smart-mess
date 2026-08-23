package com.smartmess.backend.service;

import com.smartmess.backend.enums.UserRole;

public interface EmailService {

	void sendPasswordResetEmail(
	        String recipientEmail,
	        String resetToken,
	        UserRole userRole
	);
}