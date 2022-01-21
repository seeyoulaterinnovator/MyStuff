package ru.alamics.sso.keycloak.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class JsonResponse implements ResponseBuilder, Serializable {

    private static final long serialVersionUID = 4996520034649016766L;
    private ResponseStatus status;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> results;

    @JsonIgnore
    private Response.Status httpStatus;

    public JsonResponse() {
    }

    private JsonResponse(ResponseStatus status, Response.Status httpStatus) {
        this.status = status;
        this.httpStatus = httpStatus;
    }

    public static ResponseBuilder success() {
        return new JsonResponse(ResponseStatus.SUCCESS, Response.Status.OK);
    }

    public static ResponseBuilder fail() {
        return new JsonResponse(ResponseStatus.FAIL, Response.Status.BAD_REQUEST);
    }

    public static ResponseBuilder error(Response.Status status) {
        return new JsonResponse(ResponseStatus.ERROR, status);
    }


    public static ResponseBuilder ofResult(String key, Object data) {
        final ResponseBuilder responseBuilder = success();
        return responseBuilder.addResult(key, data);
    }

    @Override
    public ResponseBuilder addResult(String key, Object data) {
        if (getResults() == null) {
            setResults(new HashMap<>());
        }
        getResults().put(key, data);
        return this;
    }

    @Override
    public Response build() {
        return Response.status(httpStatus)
                .entity(this)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public Response buildResult() {
        return Response.status(httpStatus)
                .entity(results)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    @Override
    public ResponseBuilder message(String message) {
        this.message = message;
        return this;
    }

    @Override
    public ResponseBuilder httpStatus(Response.Status httpStatus) {
        this.httpStatus = httpStatus;
        return this;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public void setHttpStatus(Response.Status httpStatus) {
        this.httpStatus = httpStatus;
    }
}
