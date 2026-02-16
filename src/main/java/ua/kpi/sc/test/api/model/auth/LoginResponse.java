package ua.kpi.sc.test.api.model.auth;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private int capabilityTier;
    private List<Object> partnerRoles;
    private boolean twoFactorEnabled;
    private boolean twoFactorRequired;
}
