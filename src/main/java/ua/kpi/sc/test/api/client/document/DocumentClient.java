package ua.kpi.sc.test.api.client.document;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import ua.kpi.sc.test.api.client.ApiClient;
import ua.kpi.sc.test.api.config.Endpoint;
import ua.kpi.sc.test.api.model.document.DocumentRequest;

public class DocumentClient extends ApiClient {

    @Step("GET /documents — List all documents")
    public Response getDocuments(String authToken) {
        return get(Endpoint.DOCUMENTS, authToken);
    }

    @Step("POST /documents — Create document: {request.title}")
    public Response createDocument(DocumentRequest request, String authToken) {
        return post(Endpoint.DOCUMENTS, request, authToken);
    }

    @Step("GET /documents/{id} — Get document by ID: {id}")
    public Response getDocumentById(String id, String authToken) {
        return get(Endpoint.DOCUMENTS + "/" + id, authToken);
    }

    @Step("PUT /documents/{id} — Update document: {id}")
    public Response updateDocument(String id, DocumentRequest request, String authToken) {
        return put(Endpoint.DOCUMENTS + "/" + id, request, authToken);
    }

    @Step("DELETE /documents/{id} — Delete document: {id}")
    public Response deleteDocument(String id, String authToken) {
        return delete(Endpoint.DOCUMENTS + "/" + id, authToken);
    }
}
