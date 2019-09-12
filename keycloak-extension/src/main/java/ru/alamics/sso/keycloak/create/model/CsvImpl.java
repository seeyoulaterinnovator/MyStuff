package ru.alamics.sso.keycloak.create.model;

import com.opencsv.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class CsvImpl implements FileModel{

    CSVReader csvReader;

    public CsvImpl(InputStream inputStream){
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream)).withCSVParser(parser).build();
    }

    public CsvImpl(){
    }

    @Override
    public String[] getHeaders() throws IOException {
        return csvReader.readNext();
    }

    @Override
    public List<String[]> getRows() throws IOException {
        return csvReader.readAll();
    }
}
