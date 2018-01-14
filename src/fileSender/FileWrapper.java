package fileSender;

import java.io.Serializable;

public class FileWrapper implements Serializable {
    private String fileName;
    private byte[] fileData;
    private static final long serialVersionUID = 1L;

    public FileWrapper(String fileName, byte[] fileData){
        this.fileName = fileName;
        this.fileData = fileData;

    }
    public String getFileName() {
        return fileName;
    }

    public byte[] getFileData() {
        return fileData;
    }




}
