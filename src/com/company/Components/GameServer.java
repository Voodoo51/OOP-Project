package com.company.Components;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

class OnlinePlayerServer
{
    public int playerNumber;
    public InetSocketAddress socket;

    public OnlinePlayerServer(int playerNumber, InetSocketAddress socket)
    {
        this.playerNumber = playerNumber;
        this.socket = socket;
    }
}

//ADD A LIST OF REMOVED RESOURCES ID SO THAT IF A PLAYER DESTROYS A RESOURCE THEN A NEW PLAYER WILL ALREADY HAVE AN UPDATED MAP
//DO SOMETHING SIMILIAR FOR STRUCTURES

public class GameServer {
    private final int port;

    private int seed;
    List<OnlinePlayerServer> sockets;

    public GameServer(int port)
    {
        Random rand = new Random();
        seed = rand.nextInt();
        this.port = port;
        sockets = new ArrayList<>();
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
                            String[] words = msg.split(",");
                            System.out.println(packet.sender());
                            if(words[0].equals("seed"))
                            {
                                String seedData = "seed," + seed;
                                // Odpowiedź do klienta
                                ctx.writeAndFlush(new DatagramPacket(
                                        Unpooled.copiedBuffer(seedData, CharsetUtil.UTF_8),
                                        packet.sender()
                                ));
                            }
                            if(words[0].equals("playerPosData"))
                            {
                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));
                                }

                            }
                            if(words[0].equals("playerJoin"))
                            {
                                sockets.add(new OnlinePlayerServer(Integer.parseInt(words[1]),packet.sender()));

                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    for(int j = 0; j < sockets.size(); j++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer("playerJoin," + sockets.get(j).playerNumber, CharsetUtil.UTF_8),
                                                sockets.get(i).socket
                                        ));
                                    }
                                }
                            }
                            if(words[0].equals("resourceDestroy"))
                            {
                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer("resourceDestroy," + words[1] + "," + words[2], CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));
                                }
                            }
                            if(words[0].equals("shutdown"))
                            {
                                int playerNumber = Integer.parseInt(words[1]);
                                int index = 0;
                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    if(sockets.get(i).playerNumber == playerNumber)
                                    {
                                        index = i;
                                        break;
                                    }
                                }

                                sockets.remove(index);

                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));
                                }
                            }
                        }
                    });


            ChannelFuture future = bootstrap.bind(port).sync();
            System.out.println("UDP server started on port " + port);
            future.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }

    public void SendToAll(String data)
    {

    }

    public static void main(String[] args) throws Exception {
        new GameServer(8080).start();
    }
}