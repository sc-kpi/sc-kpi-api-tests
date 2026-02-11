package ua.kpi.sc.test.api.model.notification;

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
public class NotificationSettingsRequest {
    private boolean emailEnabled;
    private boolean telegramEnabled;
    private boolean pushEnabled;
}
