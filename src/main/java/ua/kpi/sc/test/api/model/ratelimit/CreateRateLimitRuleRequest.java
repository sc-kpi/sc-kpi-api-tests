package ua.kpi.sc.test.api.model.ratelimit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateRateLimitRuleRequest {
    private String name;
    private String description;
    private String endpointPattern;
    private String httpMethod;
    private int limitPerPeriod;
    private int periodSeconds;
    private int burstCapacity;
    private String scope;
    private Integer targetTier;
    private String targetUserId;
    private String timeWindowStart;
    private String timeWindowEnd;
    private int priority;
    private boolean enabled;
}
