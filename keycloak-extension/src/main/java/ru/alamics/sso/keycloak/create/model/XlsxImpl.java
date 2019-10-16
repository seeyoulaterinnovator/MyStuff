package ru.alamics.sso.keycloak.create.model;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class XlsxImpl implements FileModel {

    private XSSFWorkbook workbook;

    public XlsxImpl(InputStream inputStream) throws IOException {
        workbook = new XSSFWorkbook(inputStream);
    }

    public XlsxImpl() {
        workbook = new XSSFWorkbook();
        workbook.createSheet();
    }

    @Override
    public String[] getHeaders() {
        Row row = workbook.getSheetAt(0).getRow(0);
        Iterator<Cell> iterCell = row.cellIterator();
        LinkedList<String> result = new LinkedList<String>();
        DataFormatter formatter = new DataFormatter();
        while (iterCell.hasNext()) {
            result.add(formatter.formatCellValue(iterCell.next()));
        }
        return result.toArray(new String[result.size()]);
    }

    @Override
    public void addRow(List<String> cells) {
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = sheet.getLastRowNum();
        if (sheet.getRow(rowNum) != null) {
            rowNum++;
        }
        Row row = sheet.createRow(rowNum);
        int i = 0;
        for (String cell : cells) {
            row.createCell(i).setCellValue(cell);
            i++;
        }
    }

    private String getValueCell(Cell cell) {
        CellType type = cell.getCellType();
        switch (type.toString()) {
            case "NUMERIC":
                return Integer.toString((int) cell.getNumericCellValue());
            case "STRING":
                return cell.getStringCellValue();
        }
        return "";
    }

    @Override
    public List<String[]> getRows() {
        Iterator<Row> iter = workbook.getSheetAt(0).rowIterator();
        List<String[]> rows = new LinkedList<String[]>();
        DataFormatter formatter = new DataFormatter();
        while (iter.hasNext()) {
            Row row = iter.next();
            Iterator<Cell> iterCell = row.cellIterator();
            List<String> cells = new ArrayList<>();
            while (iterCell.hasNext()) {
                cells.add(formatter.formatCellValue(iterCell.next()));
            }
            rows.add(cells.toArray(new String[cells.size()]));
        }
        return rows;
    }

    @Override
    public byte[] save() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public int getCountRows() {
        return workbook.getSheetAt(0).getLastRowNum() + 1;
    }
}
