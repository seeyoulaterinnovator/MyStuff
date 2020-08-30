package ru.alamics.sso.user.format;

import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.model.UserParameter;

import java.util.LinkedList;
import java.util.List;

import static ru.alamics.sso.user.model.UserParameter.*;

public class StandartImportFormat implements ImportFormat {

    public void checkStructure(FileModel file) throws FileServiceException {
        String[] headers = file.getHeaders();
        if (headers.length != 7 || file.getCountRows() < 2) {
            throw new FileServiceException("File Structure is not valid! Count columns not valid or data is empty!");
        }
        checkHeaders(headers);
    }

    private void checkHeaders(String[] headers) throws FileServiceException {
        for (int i = 0; i < headers.length; i++) {
            switch (i) {
                case 0:
                    checkHeader(headers[i], FIRST_NAME);
                    break;
                case 1:
                    checkHeader(headers[i], EMAIL);
                    break;
                case 2:
                    checkHeader(headers[i], PHONE);
                    break;
                case 3:
                    checkHeader(headers[i], TOMS_ID);
                    break;
                case 4:
                    checkHeader(headers[i], DMP_ID);
                    break;
                case 5:
                    checkHeader(headers[i], ROLE);
                    break;
                case 6:
                    checkHeader(headers[i], SYSTEM);
                    break;
            }
        }
    }

    private void checkHeader(String head, UserParameter userParameter) throws FileServiceException {
        if (!head.equalsIgnoreCase(userParameter.getDesc())) {
            throw new FileServiceException("File Structure is not valid! Header is not valid");
        }
    }

    public List<ImportUsersDataEntity> getDataList(FileModel file) {

        List<String[]> rows = file.getRows();
        rows.remove(0);

        return toUserRequestList(rows);
    }

    private static ImportUsersDataEntity toUserImport(String[] row) {
        ImportUsersDataEntity userImport = new ImportUsersDataEntity();
        for (int i = 0; i < row.length; i++) {
            switch (i) {
                case 0:
                    userImport.setFirstName(row[i]);
                    break;
                case 1:
                    userImport.setEmail(row[i]);
                    break;
                case 2:
                    userImport.setPhone(row[i]);
                    break;
                case 3:
                    userImport.setTomsId(row[i]);
                    break;
                case 4:
                    userImport.setDmpId(row[i]);
                    break;
                case 5:
                    userImport.setRole(row[i]);
                    break;
                case 6:
                    //userImport.setSystemNames(Arrays.asList(row[i].replaceAll("\\s", "").split(",")));
                    userImport.setSystems(row[i]);
                    break;
            }
        }
        userImport.setCreated(false);
        return userImport;
    }

    private static List<ImportUsersDataEntity> toUserRequestList(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<ImportUsersDataEntity> userImports = new LinkedList<>();
        rows.forEach(o -> userImports.add(toUserImport(o)));
        return userImports;
    }

}
