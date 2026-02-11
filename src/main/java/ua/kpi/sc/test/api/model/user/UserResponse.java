package ua.kpi.sc.test.api.model.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserResponse {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String capabilityTier;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
