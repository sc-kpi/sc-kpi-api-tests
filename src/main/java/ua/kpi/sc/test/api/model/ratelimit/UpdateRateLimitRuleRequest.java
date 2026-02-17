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
public class UpdateRateLimitRuleRequest {
    private String description;
    private String endpointPattern;
    private String httpMethod;
    private Integer limitPerPeriod;
    private Integer periodSeconds;
    private Integer burstCapacity;
    private String scope;
    private Integer targetTier;
    private String targetUserId;
    private String timeWindowStart;
    private String timeWindowEnd;
    private Integer priority;
    private Boolean enabled;
}
