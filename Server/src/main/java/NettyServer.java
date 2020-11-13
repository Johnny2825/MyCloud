import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;


public class NettyServer {
    private static BaseAuthService baseAuthService;

    public NettyServer(){
        this.baseAuthService = new BaseAuthService();
        baseAuthService.start();
    }

    public void run() throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel sc) throws Exception {
                           sc.pipeline().addLast(
                                   new ObjectEncoder(),
                                   new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
//                                   new AuthService(baseAuthService));
                                   new ServerHandler(baseAuthService));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            System.out.println("Server started");
            ChannelFuture f = b.bind(8980).sync();
            f.channel().closeFuture().sync();
            System.out.println("Server close");
        } finally {
            baseAuthService.stop();
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        try {
            new NettyServer().run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
