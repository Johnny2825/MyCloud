public class FileRequest extends AbstractMessage{
    private String fileName;

    public String getName(){
        return fileName;
    }

    public FileRequest(String fileName){
        this.fileName = fileName;
    }
}
