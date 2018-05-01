package com.kobe.dubbok.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kobe.dubbok.codec.Decoder;
import com.kobe.dubbok.codec.Encoder;
import com.kobe.dubbok.payload.Request;
import com.kobe.dubbok.payload.Response;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client extends SimpleChannelInboundHandler<Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Client.class);

    private final String host;
    private final int port;

    private Response response;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Response response) throws Exception {
        this.response = response;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("exception caught: ", cause);
        ctx.close();
    }

    public Response send(Request request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast(new Encoder(Request.class)); 
                    pipeline.addLast(new Decoder(Response.class));
                    pipeline.addLast(Client.this); 
                }
            });
            bootstrap.option(ChannelOption.TCP_NODELAY, true);
            ChannelFuture future = bootstrap.connect(host, port).sync();
            Channel channel = future.channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();
            return response;
        } finally {
            group.shutdownGracefully();
        }
    }
}
