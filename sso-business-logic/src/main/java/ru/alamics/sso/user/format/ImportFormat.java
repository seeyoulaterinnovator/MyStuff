package ru.alamics.sso.user.format;

import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.model.ImportUsersDataModel;

import java.util.List;

public interface ImportFormat {

    public void checkStructure(FileModel file) throws FileServiceException;

    public List<ImportUsersDataModel> getDataList(FileModel file);
}
