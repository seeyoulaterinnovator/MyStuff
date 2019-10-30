package ru.alamics.sso.user.model;

import com.opencsv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

public class CsvImpl implements FileModel {

    public static final String UTF8_BOM = "\uFEFF";

    private List<String[]> rows;
    private CSVReader csvReader;
    private CSVWriter csvWriter;
    private ByteArrayOutputStream byteArrayOutputStream;

    public CsvImpl(InputStream inputStream) throws IOException {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreLeadingWhiteSpace(true).build();
        csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).withCSVParser(parser).build();
        this.rows = csvReader.readAll();

        Optional.ofNullable(this.rows).orElseGet(Collections::emptyList)
                .forEach(row -> IntStream.range(0, row.length)
                        .forEach(index -> row[index] = removeUTF8BOM(row[index]))
                );
    }

    public CsvImpl() {
        byteArrayOutputStream = new ByteArrayOutputStream();
        csvWriter = new CSVWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8),';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);
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

    @Override
    public int getCountRows() {
        return rows.size();
    }


    private static String removeUTF8BOM(String s) {
        if (s.startsWith(UTF8_BOM)) {
            s = s.substring(1);
        }
        return s;
    }
}
