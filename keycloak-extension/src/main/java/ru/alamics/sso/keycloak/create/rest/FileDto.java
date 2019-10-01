package ru.alamics.sso.keycloak.create.rest;

import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.FormParam;

public class FileDto {

    private byte[] filedata;

    public byte[] getFileData() {
        return filedata;
    }

    @FormParam("file")
    @PartType("application/octet-stream")
    public void setFileData(final byte[] filedata) {
        this.filedata = filedata;
    }

}
