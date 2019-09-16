package ru.alamics.sso.keycloak.create.model;

import com.opencsv.*;

import java.io.*;
import java.util.List;

public class CsvImpl implements FileModel {

    private List<String[]> rows;
    private CSVReader csvReader;
    private CSVWriter csvWriter;
    private ByteArrayOutputStream byteArrayOutputStream;

    public CsvImpl(InputStream inputStream) throws IOException {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreLeadingWhiteSpace(true).build();
        csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(parser).build();
        rows = csvReader.readAll();
    }

    public CsvImpl() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        csvWriter = new CSVWriter(new OutputStreamWriter(byteArrayOutputStream));
    }

    @Override
    public String[] getHeaders() {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        return rows.get(0);
    }

    @Override
    public List<String[]> getRows() {
        return rows;
    }

    @Override
    public void addRow(List<String> cells) {
        csvWriter.writeNext(cells.toArray(new String[cells.size()]));
    }

    @Override
    public byte[] save() throws IOException {
        csvWriter.flush();
        return byteArrayOutputStream.toByteArray();
    }
}
