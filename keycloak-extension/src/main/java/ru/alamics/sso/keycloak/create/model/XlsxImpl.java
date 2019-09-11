package ru.alamics.sso.keycloak.create.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class XlsxImpl implements FileModel {

    private byte[] arrayBytes;
    private XSSFWorkbook workbook;

    public XlsxImpl(InputStream inputStream) throws IOException {
        workbook = new XSSFWorkbook(inputStream);
    }

    @Override
    public LinkedList<String> getHeaders() {
        Row row = workbook.getSheetAt(0).getRow(0);
        Iterator<Cell> iterCell = row.cellIterator();
        LinkedList<String> result = new LinkedList<String>();
        while (iterCell.hasNext()) {
            result.add(getValueCell(iterCell.next()));
        }
        return result;
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

    public List<List<String>> getRows() {
        Iterator<Row> iter = workbook.getSheetAt(0).rowIterator();
        LinkedList<List<String>> rows = new LinkedList<List<String>>();

        while (iter.hasNext()) {
            Row row = iter.next();
            Iterator<Cell> iterCell = row.cellIterator();
            List<String> cells = new ArrayList<>();
            while (iterCell.hasNext()) {
                Cell cell = iterCell.next();
                CellType type = cell.getCellType();
                switch (type.toString()) {
                    case "NUMERIC":
                        cells.add(Integer.toString((int) cell.getNumericCellValue()));
                        break;
                    case "STRING":
                        cells.add(cell.getStringCellValue());
                        break;
                }
            }
            rows.add(cells);
        }
        return rows;
    }
/*
    public void addRows(LinkedList<String[]> values) {
        HSSFSheet sheet = workbook.getSheetAt(0);
        int i = 0;
        for (String[] cells : values) {
            HSSFRow row = sheet.createRow(i);
            int j = 0;
            for (String cell : cells) {
                row.createCell(j).setCellValue(cell);
                j++;
            }
            i++;
        }
    }

    public void addRow(String[] values) {
        HSSFSheet sheet = workbook.getSheetAt(0);
        HSSFRow row = sheet.createRow(sheet.getLastRowNum() + 1);
        int j = 0;
        for (String cell : values) {
            row.createCell(j).setCellValue(cell);
            j++;
        }
    }*/
}
