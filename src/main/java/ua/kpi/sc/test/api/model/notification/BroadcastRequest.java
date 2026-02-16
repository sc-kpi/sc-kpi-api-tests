package ua.kpi.sc.test.api.model.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BroadcastRequest {
    private String titleKey;
    private String bodyKey;
    private String[] bodyArgs;
    private String category;
    private String targetTier;
}
