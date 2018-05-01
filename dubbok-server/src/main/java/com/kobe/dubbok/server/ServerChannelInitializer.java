package com.kobe.dubbok.server;


import java.util.Map;

import com.kobe.dubbok.codec.Decoder;
import com.kobe.dubbok.codec.Encoder;
import com.kobe.dubbok.payload.Request;
import com.kobe.dubbok.payload.Response;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ServerChannelInitializer extends ChannelInitializer<SocketChannel> {

	private Map<String, Object> classToObjectMap;

	public ServerChannelInitializer(Map<String, Object> classToObjectMap) {
		this.classToObjectMap = classToObjectMap;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		ChannelPipeline pipeline = ch.pipeline();
		pipeline.addLast(new Decoder(Request.class)); 
        pipeline.addLast(new Encoder(Response.class));
		pipeline.addLast(new ServerHandler(classToObjectMap));
			
        /*pipeline.addLast(new ProtobufVarint32FrameDecoder());           
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());   */  
	}

}
