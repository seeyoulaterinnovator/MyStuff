package ru.alamics.sso.user.format;

import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.model.ImportUsersDataModel;

import java.util.LinkedList;
import java.util.List;

public class MigrationImportFormat implements ImportFormat {


    private static ImportUsersDataModel toUserImport(String[] row) {

        ImportUsersDataModel userImport = new ImportUsersDataModel();
        if (row.length > 0) userImport.setDmpId(row[0]);
        if (row.length > 1) userImport.setTomsId(row[1]);
        if (row.length > 2) userImport.setEmail(row[2]);
        if (row.length > 3) userImport.setFirstName(row[3]);
        if (row.length > 4) userImport.setPhone(row[4]);
        if (row.length > 5) userImport.setCleanPassword(row[5]);
        if (row.length > 6) userImport.setPersonalAccount(row[6]);

        userImport.setCreated(false);
        return userImport;
    }

    private static List<ImportUsersDataModel> toUserRequestList(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<ImportUsersDataModel> userImports = new LinkedList<>();
        rows.forEach(o -> userImports.add(toUserImport(o)));
        return userImports;
    }

    public void checkStructure(FileModel file) throws FileServiceException {
    }

    public List<ImportUsersDataModel> getDataList(FileModel file) {

        // чищу весь хэдер
        /*
        for (Iterator<String[]> it = file.getRows().iterator(); it.hasNext();) {
            String[] line = it.next();

            if (line == null || line.length == 0)
                continue;

            if (line[0].equalsIgnoreCase("BEGINDATA")) {
                it.remove();
                break;
            }
            it.remove();
        }
        */

        return toUserRequestList(file.getRows());
    }
}
