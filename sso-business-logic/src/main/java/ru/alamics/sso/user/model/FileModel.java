package ru.alamics.sso.user.model;

import java.io.IOException;
import java.util.List;

public interface FileModel {
    String[] getHeaders();

    List<String[]> getRows();

    void addRow(List<String> cells);

    byte[] save() throws IOException;

    int getCountRows();
}
