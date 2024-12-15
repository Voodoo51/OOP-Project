package com.company.Components;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;


public class GameServer {
    private final int port;

    public GameServer(int port) {
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
                            System.out.println("Received: " + msg);

                            // Odpowiedź do klienta
                            ctx.writeAndFlush(new DatagramPacket(
                                    Unpooled.copiedBuffer("ACK: " + msg, CharsetUtil.UTF_8),
                                    packet.sender()
                            ));

                        }
                    });

            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("UDP server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {
        new GameServer(8080).start();
    }
}