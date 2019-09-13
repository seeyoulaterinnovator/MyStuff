package ru.alamics.sso.keycloak.create.model;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CsvImpl implements FileModel{

    private List<String[]> rows;
    private CSVReader csvReader;

    public CsvImpl(InputStream inputStream) throws IOException {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreLeadingWhiteSpace(true).build();
        csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(parser).build();
        rows = csvReader.readAll();
    }

    public CsvImpl(){
    }

    @Override
    public String[] getHeaders() {
        if (rows == null || rows.isEmpty()){
            return null;
        }
        return rows.get(0);
    }

    @Override
    public List<String[]> getRows() throws IOException {
        return rows;
    }
}
