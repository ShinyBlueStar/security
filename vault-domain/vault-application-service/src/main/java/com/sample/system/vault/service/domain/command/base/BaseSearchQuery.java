package com.sample.system.vault.service.domain.command.base;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Base search DTO carrying a simple string-to-string parameter map for
 * pagination and filtering. Kept minimal to match existing controller usage.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseSearchQuery {

    private Map<String, String> parameterMap;

    /**
     * Returns a non-null map to simplify controller logic.
     */
    public Map<String, String> getMap() {
        return parameterMap != null ? parameterMap : new HashMap<>();
    }
}

