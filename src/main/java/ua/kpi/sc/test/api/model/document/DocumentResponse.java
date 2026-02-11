package ua.kpi.sc.test.api.model.document;

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
public class DocumentResponse {
    private String id;
    private String title;
    private String content;
    private String type;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
