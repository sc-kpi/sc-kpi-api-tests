package ua.kpi.sc.test.api.client.club;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.club.ClubRequest;

public class ClubClient extends ApiClient {

    @Step("GET /clubs — List all clubs")
    public Response getClubs(String authToken) {
        return get(Endpoint.CLUBS, authToken);
    }

    @Step("POST /clubs — Create club: {request.name}")
    public Response createClub(ClubRequest request, String authToken) {
        return post(Endpoint.CLUBS, request, authToken);
    }

    @Step("GET /clubs/{id} — Get club by ID: {id}")
    public Response getClubById(String id, String authToken) {
        return get(Endpoint.CLUBS + "/" + id, authToken);
    }

    @Step("PUT /clubs/{id} — Update club: {id}")
    public Response updateClub(String id, ClubRequest request, String authToken) {
        return put(Endpoint.CLUBS + "/" + id, request, authToken);
    }

    @Step("DELETE /clubs/{id} — Delete club: {id}")
    public Response deleteClub(String id, String authToken) {
        return delete(Endpoint.CLUBS + "/" + id, authToken);
    }
}
