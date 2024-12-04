package ru.alamics.sso.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ApplicationScoped
public class ValidateService {
    @SuppressWarnings("resource")
    final Validator validator = Validation.byDefaultProvider()
            .configure()
            .messageInterpolator(new ParameterMessageInterpolator())
            .buildValidatorFactory()
            .getValidator();

    public void validate(Object object) {
        Set<ConstraintViolation<Object>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            throw new BadRequestException(Response.status(Response.Status.BAD_REQUEST)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_TYPE)
                    .entity(ValidationErrorDto.builder()
                            .violations(violations.stream()
                                    .map(violation -> ViolationDto.builder()
                                            .field(violation.getPropertyPath().toString())
                                            .message(violation.getMessage())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build())
                    .build());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationErrorDto {
        List<ViolationDto> violations;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ViolationDto {
        String field;

        String message;
    }
}
