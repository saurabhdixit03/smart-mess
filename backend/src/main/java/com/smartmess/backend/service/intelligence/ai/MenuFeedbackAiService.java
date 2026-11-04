package com.smartmess.backend.service.intelligence.ai;

import com.smartmess.backend.service.intelligence.MenuFeedbackAiContext;
import com.smartmess.backend.service.intelligence.ai.dto.MenuFeedbackAiResponse;

public interface MenuFeedbackAiService {

    MenuFeedbackAiResponse generateSuggestions(MenuFeedbackAiContext context);

}