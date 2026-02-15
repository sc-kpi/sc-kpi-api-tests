package ua.kpi.sc.test.api.model.featureflag;

import java.util.List;

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
public class FeatureFlagResponse {
    private String id;
    private String key;
    private String name;
    private String description;
    private boolean enabled;
    private String environment;
    private int rolloutPercentage;
    private List<OverrideResponse> overrides;
    private String createdBy;
    private String createdAt;
    private String updatedAt;
}
