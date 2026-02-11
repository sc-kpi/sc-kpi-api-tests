package ua.kpi.sc.test.api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CapabilityTier {
    GUEST(0, "Guest"),
    BASIC(1, "Basic"),
    INTERNAL(2, "Internal Access"),
    ADVANCED(3, "Advanced"),
    SENIOR(4, "Senior"),
    ADMIN(5, "Administrator");

    private final int level;
    private final String displayName;

    public boolean isAtLeast(CapabilityTier required) {
        return this.level >= required.level;
    }
}
