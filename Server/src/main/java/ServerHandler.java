
import io.netty.channel.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public class ServerHandler extends ChannelInboundHandlerAdapter {

    private String serverPath = "Server/Server_storage/";
    private BaseAuthService baseAuthService;
    private File dir;

    public ServerHandler(BaseAuthService baseAuthService){
        this.baseAuthService = baseAuthService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client connected");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
        System.out.println(msg.toString());
        if (msg instanceof Authentication){
            Authentication auth = (Authentication) msg;
            String login = baseAuthService.getAccessByLoginPass(auth.getLogin(), auth.getPassword());
            if (login != null){
                auth = new Authentication(true);
                ctx.writeAndFlush(auth);
                serverPath = serverPath + login + "/";
                dir = new File(serverPath);
                dir.mkdir();
                listUpdate(ctx);
            } else {
                Error error = new Error("Не правильный логин/пароль");
                ctx.writeAndFlush(error);
            }
        }
        if (msg instanceof FileRequest){
            FileRequest fr = (FileRequest) msg;
            if(Files.exists(Paths.get(serverPath + fr.getName()))){
                FileMessage fm = new FileMessage(Paths.get(serverPath + fr.getName()));
                ctx.writeAndFlush(fm);
            }
        }
        if (msg instanceof FileMessage) {
            FileMessage fm = (FileMessage) msg;
            Files.write(Paths.get(serverPath + fm.getFileName()),
                    fm.getData(),
                    StandardOpenOption.CREATE);
            listUpdate(ctx);
        }
        if (msg instanceof DeleteFile){
            DeleteFile deleteFile = (DeleteFile) msg;
            Path path = Paths.get(serverPath + deleteFile.getFileName());
            Files.delete(path);
            listUpdate(ctx);
        }
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception{
        cause.printStackTrace();
        ctx.close();
    }

    private void listUpdate(ChannelHandlerContext ctx){
        ctx.writeAndFlush(new FileList(Arrays.asList(dir.list())));
    }

}
