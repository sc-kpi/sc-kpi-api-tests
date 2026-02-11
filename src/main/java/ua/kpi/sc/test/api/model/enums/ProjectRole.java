package ua.kpi.sc.test.api.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjectRole {
    LEAD(4, "lead"),
    MEMBER(2, "member");

    private final int effectiveTier;
    private final String value;
}
