package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QueryUsers {
    private String search;
    private String tomsId;
    private String dmpId;

}
