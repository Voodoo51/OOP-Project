package com.company.Components;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import java.net.InetSocketAddress;

public class GameClient {
    private final String host;
    private final int port;

    public int seed;
    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
                            String msg = packet.content().toString(CharsetUtil.UTF_8);
                            seed = Integer.parseInt(msg);
                            System.out.println("Server says: " + msg);
                        }
                    });

            Channel channel = bootstrap.bind(0).sync().channel();

            // Wysyłanie pakietów do serwera
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("Seeden Machen", CharsetUtil.UTF_8),
                    new InetSocketAddress(host, port)
            )).sync();



            //channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void SendPositionData() throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
                            String msg = packet.content().toString(CharsetUtil.UTF_8);
                            seed = Integer.parseInt(msg);
                            System.out.println("Server says: " + msg);
                        }
                    });

            Channel channel = bootstrap.bind(0).sync().channel();

            // Wysyłanie pakietów do serwera
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("Seeden Machen", CharsetUtil.UTF_8),
                    new InetSocketAddress(host, port)
            )).sync();



            //channel.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}