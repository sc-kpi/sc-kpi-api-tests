package ua.kpi.sc.test.api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DepartmentRole {
    HEAD(4, "head"),
    CONTENT_MANAGER(3, "content_manager"),
    MEMBER(2, "member");

    private final int effectiveTier;
    private final String value;
}
