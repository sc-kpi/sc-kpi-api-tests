package ua.kpi.sc.test.api.config;

public final class Endpoint {
    // Auth
    public static final String AUTH = "/api/v1/auth";
    public static final String AUTH_LOGIN = AUTH + "/login";
    public static final String AUTH_REGISTER = AUTH + "/register";
    public static final String AUTH_REFRESH = AUTH + "/refresh";
    public static final String AUTH_LOGOUT = AUTH + "/logout";
    public static final String AUTH_ME = AUTH + "/me";

    // Users
    public static final String USERS = "/api/v1/users";
    public static final String USER_BY_ID = USERS + "/{id}";
    public static final String USER_ME = USERS + "/me";

    // Clubs
    public static final String CLUBS = "/api/v1/clubs";
    public static final String CLUB_BY_ID = CLUBS + "/{id}";
    public static final String CLUB_MEMBERS = CLUB_BY_ID + "/members";

    // Projects
    public static final String PROJECTS = "/api/v1/projects";
    public static final String PROJECT_BY_ID = PROJECTS + "/{id}";
    public static final String PROJECT_MEMBERS = PROJECT_BY_ID + "/members";

    // Departments
    public static final String DEPARTMENTS = "/api/v1/departments";
    public static final String DEPARTMENT_BY_ID = DEPARTMENTS + "/{id}";
    public static final String DEPARTMENT_MEMBERS = DEPARTMENT_BY_ID + "/members";

    // Documents
    public static final String DOCUMENTS = "/api/v1/documents";
    public static final String DOCUMENT_BY_ID = DOCUMENTS + "/{id}";

    // Notification Settings
    public static final String NOTIFICATION_SETTINGS = "/api/v1/notifications/settings";

    // Telegram Webhook
    public static final String TELEGRAM_WEBHOOK = "/api/v1/webhooks/telegram";

    // Audit
    public static final String AUDIT_LOGS = "/api/v1/admin/audit-logs";

    // Actuator & Swagger
    public static final String HEALTH = "/actuator/health";
    public static final String SWAGGER_UI = "/swagger-ui.html";
    public static final String API_DOCS = "/v3/api-docs";

    private Endpoint() {}
}
