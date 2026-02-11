package ua.kpi.sc.test.api.client;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import ua.kpi.sc.test.api.config.Config;
import ua.kpi.sc.test.api.exception.ApiConnectionException;
import ua.kpi.sc.test.api.util.JsonHelper;
import ua.kpi.sc.test.api.util.LoggingFilter;

import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.function.Supplier;

import static io.restassured.RestAssured.given;

public class ApiClient {

    private static final Set<Class<? extends Throwable>> CONNECTION_EXCEPTIONS = Set.of(
            ConnectException.class,
            SocketTimeoutException.class,
            UnknownHostException.class,
            NoRouteToHostException.class
    );

    private final LoggingFilter loggingFilter = new LoggingFilter();

    protected RequestSpecification requestSpec() {
        return given()
                .config(JsonHelper.configWithJackson3())
                .baseUri(Config.baseUrl())
                .contentType("application/json")
                .accept("application/json")
                .filter(new AllureRestAssured())
                .filter(loggingFilter);
    }

    protected RequestSpecification requestSpec(String authToken) {
        return requestSpec()
                .header("Authorization", "Bearer " + authToken);
    }

    private Response execute(String method, String path, Supplier<Response> call) {
        try {
            return call.get();
        } catch (Exception e) {
            if (isConnectionException(e)) {
                throw new ApiConnectionException(method, Config.baseUrl() + path, e);
            }
            throw e;
        }
    }

    private boolean isConnectionException(Throwable throwable) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            for (Class<? extends Throwable> cls : CONNECTION_EXCEPTIONS) {
                if (cls.isInstance(current)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Response get(String path) {
        return execute("GET", path, () -> requestSpec().get(path));
    }

    public Response get(String path, String authToken) {
        return execute("GET", path, () -> requestSpec(authToken).get(path));
    }

    public Response post(String path, Object body) {
        return execute("POST", path, () -> requestSpec().body(body).post(path));
    }

    public Response post(String path, Object body, String authToken) {
        return execute("POST", path, () -> requestSpec(authToken).body(body).post(path));
    }

    public Response put(String path, Object body) {
        return execute("PUT", path, () -> requestSpec().body(body).put(path));
    }

    public Response put(String path, Object body, String authToken) {
        return execute("PUT", path, () -> requestSpec(authToken).body(body).put(path));
    }

    public Response patch(String path, Object body) {
        return execute("PATCH", path, () -> requestSpec().body(body).patch(path));
    }

    public Response patch(String path, Object body, String authToken) {
        return execute("PATCH", path, () -> requestSpec(authToken).body(body).patch(path));
    }

    public Response delete(String path) {
        return execute("DELETE", path, () -> requestSpec().delete(path));
    }

    public Response delete(String path, String authToken) {
        return execute("DELETE", path, () -> requestSpec(authToken).delete(path));
    }
}
