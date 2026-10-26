package com.smartmess.backend.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

@Component
public class DishNameNormalizer {

    public String normalize(String name) {

        if (name == null) {
            return null;
        }

        return name
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }
}