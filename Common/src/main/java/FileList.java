import java.util.List;

public class FileList extends AbstractMessage {
    private List<String> filesList;

    public FileList(List<String> files) {
        this.filesList = files;
    }

    public FileList() {
    }

    public List<String> getFilesList() {
        return filesList;
    }

}
