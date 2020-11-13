import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileMessage extends AbstractMessage {
    private String fileName;
    private byte[] data;

    public String getFileName(){
        return fileName;
    }
    public byte[] getData(){
        return data;
    }

    public FileMessage(Path path) throws IOException {
        System.out.println("Path = " + path.toString());
        fileName = path.getFileName().toString();
        data = Files.readAllBytes(path);
    }
}
