import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthService extends ChannelInboundHandlerAdapter{
    private String serverPath = "Server/Server_storage/";
    private BaseAuthService baseAuthService;

    public AuthService(BaseAuthService baseAuthService){
        this.baseAuthService = baseAuthService;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Authentication){
            Authentication auth = (Authentication) msg;
            String login = baseAuthService.getAccessByLoginPass(auth.getLogin(), auth.getPassword());
            if (login != null){
                auth = new Authentication(true);
//                ctx.pipeline().addLast(new ServerHandler(serverPath + login + "/"));
                ctx.writeAndFlush(auth);
                ctx.pipeline().remove(this);
            } else {
                Error error = new Error("Не правильный логин/пароль");
                ctx.writeAndFlush(error);
            }

        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Client disconnected, auth");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
