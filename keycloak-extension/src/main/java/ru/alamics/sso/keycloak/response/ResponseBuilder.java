package ru.alamics.sso.keycloak.response;

import javax.ws.rs.core.Response;

public interface ResponseBuilder {

    <T> ResponseBuilder addResult(String key, T data);

    ResponseBuilder message(String message);

    ResponseBuilder httpStatus(Response.Status httpStatus);


    Response build();

}
