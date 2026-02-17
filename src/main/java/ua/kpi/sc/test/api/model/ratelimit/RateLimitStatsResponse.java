package ua.kpi.sc.test.api.model.ratelimit;

import java.util.Map;

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
public class RateLimitStatsResponse {
    private long totalViolations;
    private long activeBuckets;
    private Map<String, Long> violationsByEndpoint;
}
