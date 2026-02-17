package com.dnd.moyeolak.domain.location.enums;

import com.dnd.moyeolak.global.exception.BusinessException;
import com.dnd.moyeolak.global.response.ErrorCode;
import org.springframework.util.StringUtils;

public enum RouteMode {
    TRANSIT(true, false),
    DRIVING(false, true),
    BOTH(true, true);

    private final boolean includeTransit;
    private final boolean includeDriving;

    RouteMode(boolean includeTransit, boolean includeDriving) {
        this.includeTransit = includeTransit;
        this.includeDriving = includeDriving;
    }

    public boolean includeTransit() {
        return includeTransit;
    }

    public boolean includeDriving() {
        return includeDriving;
    }

    public static RouteMode from(String value) {
        if (!StringUtils.hasText(value)) {
            return BOTH;
        }
        try {
            return RouteMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER);
        }
    }
}
