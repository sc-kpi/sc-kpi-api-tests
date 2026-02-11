package ua.kpi.sc.test.api.util;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);
    private static final String MASKED = "***";

    @Override
    public Response filter(FilterableRequestSpecification requestSpec,
                           FilterableResponseSpecification responseSpec,
                           FilterContext ctx) {
        logRequest(requestSpec);
        Response response = ctx.next(requestSpec, responseSpec);
        logResponse(response);
        return response;
    }

    private void logRequest(FilterableRequestSpecification requestSpec) {
        log.debug("Request: {} {}", requestSpec.getMethod(), requestSpec.getURI());
        requestSpec.getHeaders().forEach(header -> {
            String value = "Authorization".equalsIgnoreCase(header.getName()) ? MASKED : header.getValue();
            log.debug("  Header: {} = {}", header.getName(), value);
        });
        if (requestSpec.getBody() != null) {
            log.debug("  Body: {}", String.valueOf(requestSpec.getBody()));
        }
    }

    private void logResponse(Response response) {
        log.debug("Response: {} {} ({}ms)",
                response.getStatusCode(),
                response.getStatusLine(),
                response.getTime());
    }
}
