package ua.kpi.sc.test.api.client.department;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;

public class DepartmentClient extends ApiClient {

    @Step("GET /departments — List all departments")
    public Response getDepartments(String authToken) {
        return get(Endpoint.DEPARTMENTS, authToken);
    }

    @Step("GET /departments/{id} — Get department by ID: {id}")
    public Response getDepartmentById(String id, String authToken) {
        return get(Endpoint.DEPARTMENTS + "/" + id, authToken);
    }

    @Step("GET /departments/{id}/members — Get department members")
    public Response getDepartmentMembers(String id, String authToken) {
        return get(Endpoint.DEPARTMENTS + "/" + id + "/members", authToken);
    }
}
