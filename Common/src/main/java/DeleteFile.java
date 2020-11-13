public class DeleteFile extends AbstractMessage {
    private String fileName;

    public DeleteFile(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
