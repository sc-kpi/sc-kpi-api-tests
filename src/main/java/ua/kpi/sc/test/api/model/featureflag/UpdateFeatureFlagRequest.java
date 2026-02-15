package ua.kpi.sc.test.api.model.featureflag;

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
public class UpdateFeatureFlagRequest {
    private String name;
    private String description;
    private Boolean enabled;
    private String environment;
    private Integer rolloutPercentage;
}
