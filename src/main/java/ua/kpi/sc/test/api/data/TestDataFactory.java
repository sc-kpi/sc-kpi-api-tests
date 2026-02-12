package ua.kpi.sc.test.api.data;

import net.datafaker.Faker;
import ua.kpi.sc.test.api.model.auth.LoginRequest;
import ua.kpi.sc.test.api.model.auth.RegisterRequest;
import ua.kpi.sc.test.api.model.club.ClubRequest;
import ua.kpi.sc.test.api.model.document.DocumentRequest;
import ua.kpi.sc.test.api.model.notification.NotificationSettingsRequest;
import ua.kpi.sc.test.api.model.project.ProjectRequest;
import ua.kpi.sc.test.api.model.user.UserUpdateRequest;

public final class TestDataFactory {

    private static final Faker faker = new Faker();

    private TestDataFactory() {}

    // Auth
    public static LoginRequest validLoginRequest() {
        return LoginRequest.builder()
                .email(faker.internet().emailAddress())
                .password("Test@" + faker.number().digits(6))
                .build();
    }

    public static RegisterRequest validRegisterRequest() {
        return RegisterRequest.builder()
                .email(faker.internet().emailAddress())
                .password("Test@" + faker.number().digits(6))
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();
    }

    public static LoginRequest invalidLoginRequest() {
        return LoginRequest.builder()
                .email("invalid-email")
                .password("")
                .build();
    }

    public static RegisterRequest invalidRegisterRequest() {
        return RegisterRequest.builder()
                .email("")
                .password("short")
                .firstName("")
                .lastName("")
                .build();
    }

    // User
    public static UserUpdateRequest validUserUpdateRequest() {
        return UserUpdateRequest.builder()
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();
    }

    // Club
    public static ClubRequest validClubRequest() {
        return ClubRequest.builder()
                .name(faker.team().name() + " Club")
                .description(faker.lorem().sentence(10))
                .build();
    }

    public static ClubRequest invalidClubRequest() {
        return ClubRequest.builder()
                .name("")
                .description("")
                .build();
    }

    // Project
    public static ProjectRequest validProjectRequest() {
        return ProjectRequest.builder()
                .name(faker.app().name() + " Project")
                .description(faker.lorem().sentence(10))
                .build();
    }

    public static ProjectRequest invalidProjectRequest() {
        return ProjectRequest.builder()
                .name("")
                .description("")
                .build();
    }

    // Document
    public static DocumentRequest validDocumentRequest() {
        return DocumentRequest.builder()
                .title(faker.book().title())
                .content(faker.lorem().paragraph())
                .type("REPORT")
                .build();
    }

    public static DocumentRequest invalidDocumentRequest() {
        return DocumentRequest.builder()
                .title("")
                .content("")
                .type("")
                .build();
    }

    // Notification Settings
    public static NotificationSettingsRequest validNotificationSettingsRequest() {
        return NotificationSettingsRequest.builder()
                .emailEnabled(true)
                .telegramEnabled(faker.bool().bool())
                .pushEnabled(faker.bool().bool())
                .build();
    }

    public static RegisterRequest registerRequestWithPassword(String password) {
        return RegisterRequest.builder()
                .email(faker.internet().emailAddress())
                .password(password)
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();
    }

    public static RegisterRequest registerRequestWithNames(String firstName, String lastName) {
        return RegisterRequest.builder()
                .email(faker.internet().emailAddress())
                .password("Test@" + faker.number().digits(6))
                .firstName(firstName)
                .lastName(lastName)
                .build();
    }

    public static RegisterRequest registerRequestWithEmail(String email) {
        return RegisterRequest.builder()
                .email(email)
                .password("Test@" + faker.number().digits(6))
                .firstName(faker.name().firstName())
                .lastName(faker.name().lastName())
                .build();
    }

    public static String longString(int length) {
        return "a".repeat(length);
    }

    // Utility
    public static String randomEmail() {
        return faker.internet().emailAddress();
    }

    public static String randomId() {
        return faker.internet().uuid();
    }
}
