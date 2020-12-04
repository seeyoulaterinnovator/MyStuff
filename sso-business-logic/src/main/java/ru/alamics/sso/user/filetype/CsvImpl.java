package ru.alamics.sso.user.filetype;

import com.opencsv.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CsvImpl implements FileModel {

    private static final int COUNT_ROW_INDENT = 2;
    public static final String UTF8_BOM = "\uFEFF";

    private List<String[]> rows;
    private CSVReader csvReader;
    private CSVWriter csvWriter;
    private ByteArrayOutputStream byteArrayOutputStream;

    private String ext;

    public CsvImpl(String fileExtension, InputStream inputStream) throws IOException {
        CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreLeadingWhiteSpace(true).build();
        csvReader = new CSVReaderBuilder(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).withCSVParser(parser).build();
        this.rows = csvReader.readAll();
        rows.removeAll(rows.stream().limit(COUNT_ROW_INDENT).skip(1).collect(Collectors.toList()));

        Optional.ofNullable(this.rows).orElseGet(Collections::emptyList)
                .forEach(row -> IntStream.range(0, row.length)
                        .forEach(index -> row[index] = removeUTF8BOM(row[index]))
                );

        ext = fileExtension;
    }

    public CsvImpl(String fileExtension) {
        byteArrayOutputStream = new ByteArrayOutputStream();
        csvWriter = new CSVWriter(new OutputStreamWriter(byteArrayOutputStream, StandardCharsets.UTF_8),';',
                CSVWriter.NO_QUOTE_CHARACTER,
                CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                CSVWriter.DEFAULT_LINE_END);

        ext = fileExtension;
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

    @Override
    public String getFileExtension() {
        return ext;
    }
}
