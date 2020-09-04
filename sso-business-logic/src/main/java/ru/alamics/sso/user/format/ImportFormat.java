package ru.alamics.sso.user.format;

import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.user.FileServiceException;
import ru.alamics.sso.user.filetype.FileModel;

import java.util.List;

public interface ImportFormat {

    public void checkStructure(FileModel file) throws FileServiceException;

    public List<ImportUsersDataEntity> getDataList(FileModel file);
}
