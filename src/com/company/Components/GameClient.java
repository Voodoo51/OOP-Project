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

    public int otherPlayerX = 0;
    public int otherPlayerY = 0;
    private Channel channel;
    private int ID;
    public int seed;
    public boolean seedReceived = false;
    private InetSocketAddress socket;
    EventLoopGroup group;
    private Player player;

    public boolean allPlayersConnected = false;

    public GameClient(String host, int port, Player player) {
        this.host = host;
        this.port = port;
        this.player = player;
        socket = new InetSocketAddress(host, port);
        group = new NioEventLoopGroup();

    }

    public void start() throws Exception {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                        @Override
                        protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {

                            String msg = packet.content().toString(CharsetUtil.UTF_8);
                            String[] words = msg.split(",");
                            if(!words[0].equals("playerPosData"))
                            {
                                System.out.println("Server says: " + msg);
                            }
                            if(words[0].equals("allPlayersConnected"))
                            {
                                allPlayersConnected = true;
                            }
                            if(words[0].equals("seed"))
                            {
                                seed = Integer.parseInt(words[1]);
                                seedReceived = true;
                            }
                            if(words[0].equals("playerPosData"))
                            {
                                //System.out.println(ID + " : " + words[1]);
                                int id = Integer.parseInt(words[1]);
                                int x = Integer.parseInt(words[2]);
                                int y = Integer.parseInt(words[3]);
                                if(Integer.parseInt(words[1]) != player.playerNumber)
                                {
                                    //System.out.println("OTHER PLAYER DATA RECEIVED");
                                    player.UpdatePlayerPosition(id, x, y);
                                    //otherPlayerX = Integer.parseInt(words[2]);
                                    //otherPlayerY = Integer.parseInt(words[3]);
                                }
                            }
                            if(words[0].equals("playerJoin"))
                            {
                                player.OnNewPlayerJoined(Integer.parseInt(words[1]));
                            }
                            if(words[0].equals("enemySpawned"))
                            {
                                player.OnEnemySpawned(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                            }
                            if(words[0].equals("enemyPosData"))
                            {
                                player.UpdateEnemyPosition(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                            }
                            if(words[0].equals("enemyKilled"))
                            {
                                player.OnEnemyKilled(Integer.parseInt(words[1]));
                            }
                            if(words[0].equals("structurePlaced"))
                            {
                                int ID = Integer.parseInt(words[1]);
                                int x = Integer.parseInt(words[2]);
                                int y = Integer.parseInt(words[3]);
                                int w = Integer.parseInt(words[4]);
                                int h = Integer.parseInt(words[5]);
                                String type = words[6];
                                player.OnStructureReceived(ID, x, y, w, h, type);
                            }
                            if(words[0].equals("gameOver"))
                            {
                                player.OnGameOver(Integer.parseInt(words[1]));
                            }
                            if(words[0].equals("structureDestroyed"))
                            {
                                player.OnStructureDestroyed(Integer.parseInt(words[1]));
                            }
                            if(words[0].equals("time"))
                            {
                                player.UpdateTime(Integer.parseInt(words[1]));
                            }
                            if(words[0].equals("resourceDestroy"))
                            {
                                player.UpdateResources(Integer.parseInt(words[1]), Integer.parseInt(words[2]));
                            }
                            if(words[0].equals("projectileShot"))
                            {
                                player.OnProjectileShot(Integer.parseInt(words[1]), Integer.parseInt(words[2]));
                            }
                            if(words[0].equals("projectilePos"))
                            {
                                player.UpdateProjectilePosition(Integer.parseInt(words[1]), Integer.parseInt(words[2]), Integer.parseInt(words[3]));
                            }
                            if(words[0].equals("projectileDestroyed"))
                            {
                                player.OnProjectileDestroyed(Integer.parseInt(words[1]));
                            }
                            if(words[0].equals("shutdown"))
                            {
                                if(Integer.parseInt(words[1]) != player.playerNumber)
                                {
                                    player.UpdatePlayers(Integer.parseInt(words[1]));
                                }
                            }
                        }
                    });

            channel = bootstrap.bind(0).sync().channel();

            // Wysyłanie pakietów do serwera
            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("seed", CharsetUtil.UTF_8),
                    socket
            )).sync();

            channel.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("playerJoin," + player.playerNumber, CharsetUtil.UTF_8),
                    socket
            )).sync();

            channel.closeFuture().addListener(future1 ->
            {
                if (future1.isSuccess()) {
                    System.out.println("UDP packet sent successfully!");
                    channel.closeFuture().sync();
                }
            });

        } finally {
           // group.shutdownGracefully();
        }
    }

    public void RequestMapState()
    {
        String posData = "mapState";
        ChannelFuture future = channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(posData, CharsetUtil.UTF_8),
                socket
        ));
    }

    public void RequestEnemies()
    {
        String data = "enemiesList";
        ChannelFuture future = channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(data, CharsetUtil.UTF_8),
                socket
        ));
    }

    public void SendPosition(int ID, int x, int y)
    {
        String posData = "playerPosData," + ID + "," + x + "," + y;
        ChannelFuture future = channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(posData, CharsetUtil.UTF_8),
                socket
        ));
    }


    public void SendResourceDestroy(int ID, int rID, int x, int y)
    {
        String posData = "resourceDestroy," + ID + "," + rID + "," + x + "," + y;
        ChannelFuture future = channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(posData, CharsetUtil.UTF_8),
                socket
        ));
    }

    public void SendStructure(int ID, int x, int y, int w, int h, String type)
    {
        String structureData = "structurePlaced," + ID + "," + x + "," + y + "," + w + "," + h + "," + type;
        ChannelFuture future = channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(structureData, CharsetUtil.UTF_8),
                socket
        ));
    }

    public void ShutDown()
    {
        String data = "shutdown," + player.playerNumber;
        ChannelFuture future = channel.writeAndFlush(new DatagramPacket(
                Unpooled.copiedBuffer(data, CharsetUtil.UTF_8),
                socket
        ));

        group.shutdownGracefully();
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