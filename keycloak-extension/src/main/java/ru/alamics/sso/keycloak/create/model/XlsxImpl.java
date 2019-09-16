package ru.alamics.sso.keycloak.create.model;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.ws.rs.FormParam;
import java.io.*;
import java.util.*;

public class XlsxImpl implements FileModel {

    private byte[] arrayBytes;
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
        while (iterCell.hasNext()) {
            result.add(getValueCell(iterCell.next()));
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
            rows.add(cells.toArray(new String[cells.size()]));
        }
        return rows;
    }

    @Override
    public OutputStream save() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream;
    }
}
