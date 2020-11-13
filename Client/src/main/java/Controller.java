import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;



public class Controller implements Initializable {

    private String clientPath; //"Client/Client_storage/"
    @FXML
    private AnchorPane authPanel;
    @FXML
    private AnchorPane workPanel;
    @FXML
    private TextField pathField;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passField;
    @FXML
    private ListView listClient;
    @FXML
    private ListView listSever;

    private Network network;
    private boolean authenticated;
    private FileList msg;
    private WatchService watcher;

    public Controller() {
        network = new Network();
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        workPanel.setVisible(authenticated);
        workPanel.setManaged(authenticated);
        authPanel.setVisible(!authenticated);
        authPanel.setManaged(!authenticated);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources){
        setAuthenticated(false);
        linkCallbacks();
    }

    public void linkCallbacks() {
        network.connect();
        network.setCallOnException(args -> {
                    Error error = (Error) args[0];
//                    showAlert(error.getMessage());
        });
        network.setCallOnCloseConnection(args -> setAuthenticated(false));
        network.setCallOnAuthenticated(args -> {
            Authentication auth = (Authentication) args[0];
            setAuthenticated(auth.getStatus());
            refreshLocalFilesList();
            listener();
        });
        network.setCallOnMsgReceived(args -> {
            FileList fileList = (FileList) args[0];
            Platform.runLater(()-> {
                listSever.getItems().setAll(fileList.getFilesList());
            });
        });

    }

//    public void showAlert(String msg) {
//        Platform.runLater(() -> {
//            Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
//            alert.showAndWait();
//        });
//    }

    private void listener(){
        try {
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(clientPath);
            path.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    System.out.println(
                            "Event kind:" + event.kind()
                                    + ". File affected: " + event.context() + ".");
                    refreshLocalFilesList();
                }
                key.reset();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refreshLocalFilesList(){
        Platform.runLater(()->{
            try {
                listClient.getItems().clear();
                Files.list(Paths.get(clientPath))
                        .filter(p -> !Files.isDirectory(p))
                        .map(p->p.getFileName().toString())
                        .forEach(o->listClient.getItems().add(o));
            } catch (IOException e){
                e.printStackTrace();
            }
        });
    }

    public void download(ActionEvent actionEvent) {
        FileRequest fileRequest = new FileRequest((String) listSever.getSelectionModel().getSelectedItem());
        network.sendMsg(fileRequest);
    }


    public void upload(ActionEvent actionEvent) {
        try {
            Path path = Paths.get(clientPath + listClient.getSelectionModel().getSelectedItem());
            FileMessage fileMessage = new FileMessage(path);
            network.sendMsg(fileMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendAuth(ActionEvent actionEvent){
        network.sendAuth(loginField.getText(), passField.getText());
        clientPath = pathField.getText();
        pathField.clear();
        loginField.clear();
        passField.clear();
    }

    public void disconnect(ActionEvent actionEvent) {
        network.stop();
        setAuthenticated(false);
    }

    public void deleteFileFromClient(ActionEvent actionEvent) {
        Path path = Paths.get(clientPath + listClient.getSelectionModel().getSelectedItem());
        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteFileFromServer(ActionEvent actionEvent) {
        DeleteFile deleteFile = new DeleteFile((String) listSever.getSelectionModel().getSelectedItem());
        network.sendMsg(deleteFile);
    }
}
