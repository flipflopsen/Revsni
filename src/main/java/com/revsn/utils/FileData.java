package com.revsn.utils;

import java.io.Serializable;

public class FileData implements Serializable {
    private String destinationDirectory;
    private String sourceDirectory;
    private String filename;
    private long fileSize;
    private byte[] fileData;
    private Status status;

    public FileData() { }

    /**
     * getter
     * @return the destination directory
     */
    public String getDestinationDirectory() {
        return destinationDirectory;
    }

    /**
     * setter
     * @param destinationDirectory - desired directory, where the file is to be saved
     */
    public void setDestinationDirectory(String destinationDirectory) {
        this.destinationDirectory = destinationDirectory;
    }

    /**
     * getter
     * @return the source directory
     */
    public String getSourceDirectory() {
        return sourceDirectory;
    }

    /**
     * setter
     * @param sourceDirectory - source directory, where the file comes from
     */
    public void setSourceDirectory(String sourceDirectory) {
        this.sourceDirectory = sourceDirectory;
    }

    /**
     * getter
     * @return the file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * setter
     * @param filename - filename (similar to File Class)
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * getter
     * @return the size of the file
     */
    public long getFileSize() {
        return fileSize;
    }

    /**
     * setter
     * @param fileSize upon creation set the file size (data)
     */
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    /**
     * getter
     * @return the actual content of the file as a byte array
     */
    public byte[] getFileData() {
        return fileData;
    }

    /**
     * setter
     * @param fileData - upon creation the byte array of the actual data
     */
    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    /**
     * getter
     * @return the status of the file, upon creation if file was
     * created with all its data Status is - SUCCESS, or ERROR for any other reason
     */
    public Status getStatus() {
        return status;
    }

    /**
     * setter
     * @param status - the SUCCESS/ERROR flags
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * Status options
     */
    public enum Status {
        SUCCESS,ERROR
    }
}
