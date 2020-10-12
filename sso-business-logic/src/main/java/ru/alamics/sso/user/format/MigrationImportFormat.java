package ru.alamics.sso.user.format;

import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.filetype.FileModel;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MigrationImportFormat implements ImportFormat {


    public void checkStructure(FileModel file) throws FileServiceException
    {
        // TODO
    }

    public List<ImportUsersDataEntity> getDataList(FileModel file) {

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

    private static ImportUsersDataEntity toUserImport(String[] row) {

        ImportUsersDataEntity userImport = new ImportUsersDataEntity();
        if (row.length > 0) userImport.setDmpId(row[0]);
        if (row.length > 1) userImport.setTomsId(row[1]);
        if (row.length > 2) userImport.setEmail(row[2]);
        if (row.length > 3) userImport.setFirstName(row[3]);
        if (row.length > 4) userImport.setPhone(row[4]);
        if (row.length > 5) userImport.setCleanPassword(row[5]);

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
