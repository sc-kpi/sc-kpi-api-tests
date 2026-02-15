package ua.kpi.sc.test.api.model.audit;

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
public class AuditEventResponse {
    private String id;
    private String actorId;
    private String actorEmail;
    private String action;
    private String entityType;
    private String entityId;
    private String entityName;
    private String fieldName;
    private String oldValue;
    private String newValue;
    private String details;
    private String sourceModule;
    private String ipAddress;
    private String createdAt;
}
