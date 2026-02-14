package ua.kpi.sc.test.api.model.mail;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MailpitSearchResponse {

    @JsonProperty("messages_count")
    private int messagesCount;

    private List<MessageSummary> messages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MessageSummary {

        @JsonProperty("ID")
        private String id;

        @JsonProperty("Subject")
        private String subject;

        @JsonProperty("Created")
        private String created;
    }
}
