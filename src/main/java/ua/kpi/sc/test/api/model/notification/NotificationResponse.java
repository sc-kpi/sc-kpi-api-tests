package ua.kpi.sc.test.api.model.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationResponse {
    private String id;
    private String userId;
    private String titleKey;
    private String bodyKey;
    private String[] bodyArgs;
    private String category;
    private String sourceModule;
    private String relatedEntityId;
    private String relatedEntityType;
    private boolean read;
    private String readAt;
    private String createdAt;
}
