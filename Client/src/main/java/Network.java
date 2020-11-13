import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Network {
    private Callback callOnMsgReceived;
    private Callback callOnAuthenticated;
    private Callback callOnException;
    private Callback callOnCloseConnection;

    private Socket socket;
    private ObjectEncoderOutputStream out;
    private ObjectDecoderInputStream in;
    private AbstractMessage am;

    public void setCallOnMsgReceived(Callback callOnMsgReceived) {
        this.callOnMsgReceived = callOnMsgReceived;
    }

    public void setCallOnAuthenticated(Callback callOnAuthenticated) {
        this.callOnAuthenticated = callOnAuthenticated;
    }

    public void setCallOnException(Callback callOnException) {
        this.callOnException = callOnException;
    }

    public void setCallOnCloseConnection(Callback callOnCloseConnection) {
        this.callOnCloseConnection = callOnCloseConnection;
    }

    public ObjectEncoderOutputStream getOut(){
        return out;
    }
    public ObjectDecoderInputStream getIn(){
        return in;
    }

    public void connect(){
        try{
            socket = new Socket("localhost",8980);
            out = new ObjectEncoderOutputStream(socket.getOutputStream());
            in = new ObjectDecoderInputStream(socket.getInputStream());
            System.out.println("Connect");
            listener();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendAuth(String login, String password) {
        if(socket.isClosed()) {
            connect();
        }
        if (!socket.isClosed() && out != null){
            System.out.println("/auth " + login + " " + password);
            Authentication auth = new Authentication(login, password);
            sendMsg(auth);
        } else {
            callOnException.callback("Соединение с сервером не установлено");
        }
    }

    private void listener() {
        Thread clientListenerThread = new Thread(() -> {
            try {
                while (true) {
                    am = (AbstractMessage) in.readObject();
                    System.out.println("Пришло сообщение = " + am);
                    if(am instanceof Authentication){
                        Thread thread1 = new Thread(() -> {
                            callOnAuthenticated.callback(am);
                        });
                        thread1.setDaemon(true);
                        thread1.start();
                    }
                    if(am instanceof Error){
                        Thread thread2 = new Thread(() -> {
                            callOnException.callback(am);
                        });
                        thread2.setDaemon(true);
                        thread2.start();
                    }
                    if (am instanceof FileList){
                            callOnMsgReceived.callback(am);
                    }
                    if (am instanceof FileMessage) {
                        Thread thread4 = new Thread(() -> {
                            FileMessage fm = (FileMessage) am;
                            try {
                                Files.write(Paths.get("Client/Client_storage/" + fm.getFileName()),
                                        fm.getData(),
                                        StandardOpenOption.CREATE);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            callOnMsgReceived.callback();
                        });
                        thread4.setDaemon(true);
                        thread4.start();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                stop();
            }
        });
        clientListenerThread.setDaemon(true);
        clientListenerThread.start();
    }

    public void stop(){
        try {
            out.close();
        } catch (IOException e){
            e.printStackTrace();
        }
        try {
            in.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void sendMsg(AbstractMessage msg){
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
