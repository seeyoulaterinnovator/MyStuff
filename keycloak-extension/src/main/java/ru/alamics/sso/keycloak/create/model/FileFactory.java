package ru.alamics.sso.keycloak.create.model;

import java.io.IOException;
import java.io.InputStream;

public class FileFactory {
    public static FileModel createFileModel(InputStream inputStream, String type) throws IOException {
          if (type.equalsIgnoreCase(".xlsx")){
              return new XlsxImpl(inputStream);
          } else if (type.equalsIgnoreCase(".csv")){
              return new CsvImpl(inputStream);
          }
          return null;
    }

    public static FileModel createFileModel(String type) throws IOException {
        if (type.equalsIgnoreCase(".xlsx")){
            return new XlsxImpl();
        } else if (type.equalsIgnoreCase(".csv")){
            return new CsvImpl();
        }
        return null;
    }
}
