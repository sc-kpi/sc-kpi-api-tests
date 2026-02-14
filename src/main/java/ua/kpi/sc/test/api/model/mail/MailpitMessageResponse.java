package ua.kpi.sc.test.api.model.mail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailpitMessageResponse {

    @JsonProperty("ID")
    private String id;

    @JsonProperty("Subject")
    private String subject;

    @JsonProperty("Text")
    private String text;

    @JsonProperty("HTML")
    private String html;

    @JsonProperty("Created")
    private String created;
}
