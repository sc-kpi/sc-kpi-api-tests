package ua.kpi.sc.test.api.model.notification;

import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationStatsResponse {
    private long total;
    private long last24h;
    private Map<String, Long> byCategory;
}
