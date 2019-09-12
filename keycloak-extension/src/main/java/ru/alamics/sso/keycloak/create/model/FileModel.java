package ru.alamics.sso.keycloak.create.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public interface FileModel {
    String[] getHeaders() throws IOException;

    List<String[]> getRows() throws IOException;
}
