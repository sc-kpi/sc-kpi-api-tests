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
public class CreateOverrideRequest {
    private String overrideType;
    private Integer tierLevel;
    private String userId;
    private boolean enabled;
}
