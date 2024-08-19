package ru.alamics.sso.user.filetype;

import com.opencsv.exceptions.CsvException;
import jakarta.activation.UnsupportedDataTypeException;
import ru.alamics.sso.user.format.ImportFormat;
import ru.alamics.sso.user.format.MigrationImportFormat;
import ru.alamics.sso.user.format.StandartImportFormat;

import java.io.IOException;
import java.io.InputStream;

public class FileFactory {

    public static final String XLSX = "xlsx";
    public static final String CSV = "csv";
    public static final String CTL = "ctl";

    public static FileModel createFileModel(InputStream inputStream, String type) throws IOException {

        if (XLSX.equalsIgnoreCase(type)){
            return new XlsxImpl(type, inputStream);
        }
        if (CSV.equalsIgnoreCase(type)){
            return new CsvImpl(type, inputStream);
        }
        if (CTL.equalsIgnoreCase(type)){
            return new CtlImpl(type, inputStream);
        }
        throw new UnsupportedDataTypeException("Unsupported file format!");
    }

    public static FileModel createFileModel(String type) throws IOException {

        if (XLSX.equalsIgnoreCase(type)){
            return new XlsxImpl(type);
        }
        if (CSV.equalsIgnoreCase(type)){
            return new CsvImpl(type);
        }
        if (CTL.equalsIgnoreCase(type)){
            return new CtlImpl(type);
        }
        throw new UnsupportedDataTypeException("Unsupported file format!");
    }

    public static ImportFormat getImportFormat(FileModel model) throws UnsupportedDataTypeException {

        if (XLSX.equalsIgnoreCase(model.getFileExtension())
            || CSV.equalsIgnoreCase(model.getFileExtension())) {
            return new StandartImportFormat();
        }
        if (CTL.equalsIgnoreCase(model.getFileExtension())) {
            return new MigrationImportFormat();
        }
        throw new UnsupportedDataTypeException("Unsupported file format!");
    }
}
