package com.smartmess.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.menu-intelligence")
@Getter
@Setter
public class MenuIntelligenceProperties {

    private int dishRatingThreshold = 5;

    private int overallCommentThreshold = 5;
}
