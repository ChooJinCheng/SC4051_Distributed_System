package models;

public class Metadata {
    private String fileLocation;
    private String modifiedDate;

    // Constructors, getters, setters, and other methods
    public Metadata() {

    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(String modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}
