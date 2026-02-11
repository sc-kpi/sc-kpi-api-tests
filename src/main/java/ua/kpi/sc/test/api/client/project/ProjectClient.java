package ua.kpi.sc.test.api.client.project;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.project.ProjectRequest;

public class ProjectClient extends ApiClient {

    @Step("GET /projects — List all projects")
    public Response getProjects(String authToken) {
        return get(Endpoint.PROJECTS, authToken);
    }

    @Step("POST /projects — Create project: {request.name}")
    public Response createProject(ProjectRequest request, String authToken) {
        return post(Endpoint.PROJECTS, request, authToken);
    }

    @Step("GET /projects/{id} — Get project by ID: {id}")
    public Response getProjectById(String id, String authToken) {
        return get(Endpoint.PROJECTS + "/" + id, authToken);
    }

    @Step("PUT /projects/{id} — Update project: {id}")
    public Response updateProject(String id, ProjectRequest request, String authToken) {
        return put(Endpoint.PROJECTS + "/" + id, request, authToken);
    }

    @Step("DELETE /projects/{id} — Delete project: {id}")
    public Response deleteProject(String id, String authToken) {
        return delete(Endpoint.PROJECTS + "/" + id, authToken);
    }
}
