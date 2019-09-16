package ru.alamics.sso.keycloak.create.model;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public interface FileModel {
    String[] getHeaders();

    List<String[]> getRows();

    void addRow(List<String> cells);

    byte[] save() throws IOException;
}
