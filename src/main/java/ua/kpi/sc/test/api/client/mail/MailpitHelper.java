package ua.kpi.sc.test.api.client.mail;

import io.qameta.allure.Step;
import ua.kpi.sc.test.api.client.auth.AuthClient;
import ua.kpi.sc.test.api.data.TestDataFactory;
import ua.kpi.sc.test.api.model.auth.ForgotPasswordRequest;
import ua.kpi.sc.test.api.model.mail.MailpitMessageResponse;
import ua.kpi.sc.test.api.model.mail.MailpitSearchResponse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

public class MailpitHelper {

    private static final Pattern RESET_TOKEN_PATTERN = Pattern.compile("token=([a-f0-9-]+)");

    private final AuthClient authClient;
    private final MailpitClient mailpitClient;

    public MailpitHelper(AuthClient authClient, MailpitClient mailpitClient) {
        this.authClient = authClient;
        this.mailpitClient = mailpitClient;
    }

    @Step("Request password reset token via Mailpit for: {email}")
    public String requestResetToken(String email) {
        ForgotPasswordRequest forgotRequest = TestDataFactory.validForgotPasswordRequest(email);
        authClient.forgotPassword(forgotRequest);

        MailpitSearchResponse searchResponse = mailpitClient.waitForMessageTyped(email);
        String messageId = searchResponse.getMessages().getFirst().getId();
        MailpitMessageResponse message = mailpitClient.getMessageTyped(messageId);

        Matcher matcher = RESET_TOKEN_PATTERN.matcher(message.getText());
        assertThat(matcher.find()).as("Reset token should be present in email body").isTrue();
        return matcher.group(1);
    }
}
