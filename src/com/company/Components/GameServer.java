package com.company.Components;

import com.raylib.Jaylib;
import com.raylib.Raylib;
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

class OnlineServerProjectile
{
    public Transform transform;
    public Collision collision;
    private Raylib.Vector2 dir;
    private float speed = 4;
    private float timeToDestroy = 15f;
    private float t = 0;

    public OnlineServerProjectile(Transform transform, Raylib.Vector2 dir)
    {
        this.transform = transform;
        collision = new Collision(transform);
        this.dir = dir;
        this.dir = Jaylib.Vector2Normalize(this.dir);
        this.dir.x(this.dir.x() * speed);
        this.dir.y(this.dir.y() * speed);
    }

    public void Update(float deltaTime)
    {
        t += deltaTime;

        transform.x += dir.x();
        transform.y += dir.y();
    }

    public boolean RanOutOfTime()
    {
        if(t >= timeToDestroy)
            return true;

        return false;
    }
}

class OnlineStructure
{
    public Transform transform;
    public Collision collision;
    public StructureType type;
    public int health;

    public OnlineStructure(Transform transform, StructureType type)
    {
        this.transform = transform;
        collision = new Collision(this.transform);
        this.type = type;

        if(type == StructureType.WoodWall)
        {
            health = 10;
        }
        if(type == StructureType.StoneWall)
        {
            health = 20;
        }
        if(type == StructureType.Tower)
        {
            health = 10;
        }
    }
}

class OnlineTower
{
    public OnlineStructure structure;
    public Jaylib.Vector2 ourPos;
    private float radius = 500;
    private float cooldown = 5;
    private float t = 0;

    public OnlineTower(OnlineStructure structure)
    {
        this.structure = structure;
        ourPos = new Jaylib.Vector2(structure.transform.x + structure.transform.width/2, structure.transform.y + structure.transform.height/2);
    }

    public void Update(float deltaTime)
    {
        t += deltaTime;
    }

    public boolean IsInRadius(Raylib.Vector2 position)
    {
        return Jaylib.Vector2Distance(ourPos, position) < radius;
    }

    public boolean CanShoot()
    {
        if(t >= cooldown)
        {
            t = 0;
            return  true;
        }
        return false;
    }
}

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

class House
{
    public Collision collision;
    public int health = 20;

    public House(Collision collision)
    {
        this.collision = collision;
    }
}

public class GameServer {
    private final int port;
    private boolean hasReceivedMap = false;
    private int seed;
    List<OnlinePlayerServer> sockets = new ArrayList<>();
    List<OnlinePlayer> players = new ArrayList<>();
    List<OnlineEnemy> enemies = new ArrayList<>();
    List<OnlineStructure> onlineStructures = new ArrayList<>();
    List<OnlineServerProjectile> onlineServerProjectiles = new ArrayList<>();
    List<OnlineTower> onlineTowers = new ArrayList<>();
    List<Integer> removedResources = new ArrayList<>();
    private int startEnemyCount = 10;
    private int endEnemyCount = 100;
    private float timeBeforeEnemyWave = 60;
    private float timeToSurvive = 3 * 60;
    private float timeBeforeEnemySpawn = 0.0f;
    private boolean timeOnce = false;
    private float t = timeBeforeEnemyWave;
    private float eT = 0;
    private House house = new House(new Collision(new Transform(50 * 8, 50 * 8, 8, 8)));
    int tick = 0;
    int accumulator = 0;
    int playerCountRequired = 2;

    public int obstacleMap[][];

    public GameServer(int port)
    {
        Random rand = new Random();
        seed = rand.nextInt();
        this.port = port;

        obstacleMap = WorldGenerator.GetObstacleMap(100, 100, 0.05, 0.1,0.1, seed);
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
                            if(words[0].equals("mapState"))
                            {
                                for(int i = 0; i < removedResources.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer("resourceDestroy,0" + "," + removedResources.get(i), CharsetUtil.UTF_8),
                                            packet.sender()
                                    ));
                                }

                                for(int i = 0; i < onlineStructures.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer("structurePlaced,0" + "," +
                                                    onlineStructures.get(i).transform.x + "," +
                                                    onlineStructures.get(i).transform.y + "," +
                                                    onlineStructures.get(i).transform.width + "," +
                                                    onlineStructures.get(i).transform.height + "," +
                                                    onlineStructures.get(i).type.toString()
                                                    ,CharsetUtil.UTF_8),
                                            packet.sender()
                                    ));
                                }

                                for(int i = 0; i < enemies.size(); i++)
                                {
                                    String enemyData = "enemySpawned,"+
                                            enemies.get(i).enemyID + "," +
                                            enemies.get(i).transform.x + "," +
                                            enemies.get(i).transform.y;

                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer(enemyData, CharsetUtil.UTF_8),
                                            packet.sender()
                                    ));
                                }

                                for(int i = 0; i < onlineServerProjectiles.size(); i++)
                                {

                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer("projectileShot," +
                                                            onlineServerProjectiles.get(i).transform.x + "," +
                                                            onlineServerProjectiles.get(i).transform.y,
                                                    CharsetUtil.UTF_8),
                                            packet.sender()
                                    ));

                                }
                            }
                            if(words[0].equals("playerPosData"))
                            {
                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));

                                    for(int e = 0; e < enemies.size(); e++)
                                    {
                                        String enemyData = "enemyPosData," +
                                                enemies.get(e).enemyID + "," +
                                                enemies.get(e).transform.x + "," +
                                                enemies.get(e).transform.y;

                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer(enemyData, CharsetUtil.UTF_8),
                                                sockets.get(i).socket
                                        ));
                                    }
                                }
                                int ID = Integer.parseInt(words[1]);
                                for(int i = 0; i < players.size(); i++)
                                {
                                    if(ID == players.get(i).playerNumber)
                                    {
                                        players.get(i).transform.x = Integer.parseInt(words[2]);
                                        players.get(i).transform.y = Integer.parseInt(words[3]);
                                        break;
                                    }
                                }
                            }
                            if(words[0].equals("playerJoin"))
                            {
                                sockets.add(new OnlinePlayerServer(Integer.parseInt(words[1]),packet.sender()));
                                players.add(new OnlinePlayer(new Transform(0,0,100,100), Integer.parseInt(words[1])));
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

                                if(sockets.size() == playerCountRequired)
                                {
                                    for(int i = 0; i < sockets.size(); i++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer("allPlayersConnected", CharsetUtil.UTF_8),
                                                sockets.get(i).socket
                                        ));
                                    }
                                }
                            }
                            if(words[0].equals("enemiesList"))
                            {
                                for(int i = 0; i < enemies.size(); i++)
                                {
                                    String enemyData = "enemySpawned,"+
                                            enemies.get(i).enemyID + "," +
                                            enemies.get(i).transform.x + "," +
                                            enemies.get(i).transform.y;

                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer(enemyData, CharsetUtil.UTF_8),
                                            packet.sender()
                                    ));

                                }
                            }
                            if(words[0].equals("structurePlaced"))
                            {
                                int ID = Integer.parseInt(words[1]);
                                int x = Integer.parseInt(words[2]);
                                int y = Integer.parseInt(words[3]);
                                int w = Integer.parseInt(words[4]);
                                int h = Integer.parseInt(words[5]);
                                String type = words[6];
                                StructureType sType = null;

                                if(type.equals("WoodWall"))
                                    sType = StructureType.WoodWall;
                                if(type.equals("StoneWall"))
                                    sType = StructureType.StoneWall;
                                if(type.equals("Tower"))
                                    sType = StructureType.Tower;

                                onlineStructures.add(new OnlineStructure(new Transform(x,y,w,h), sType));
                                if(type.equals("Tower"))
                                {
                                    onlineTowers.add(new OnlineTower(onlineStructures.getLast()));
                                }
                                String structureData = "structurePlaced," + ID + "," + x + "," + y + "," + w + "," + h + "," + type;

                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer(structureData, CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));
                                }
                            }
                            if(words[0].equals("resourceDestroy"))
                            {
                                removedResources.add(Integer.parseInt(words[2]));
                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer("resourceDestroy," + words[1] + "," + words[2], CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));
                                }

                                int x = Integer.parseInt(words[3]);
                                int y = Integer.parseInt(words[4]);
                                System.out.println("X " + x + "   Y " +  y);
                                obstacleMap[x/8][y/8] = 1;
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

                            accumulator++;
                            if(accumulator >= sockets.size())
                            {
                                tick++;
                                t-=0.016666f;

                                if(timeOnce && t <= 0)
                                {
                                    for(int i = 0; i < sockets.size(); i++)
                                    {
                                        //1 means won
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer("gameOver," + 1, CharsetUtil.UTF_8),
                                                sockets.get(i).socket
                                        ));
                                    }
                                    group.shutdownGracefully();
                                }

                                eT+=0.016666f;

                                int maxEnemyCount = (int) WorldGenerator.lerp(startEnemyCount, endEnemyCount, 1.0f - (t / timeToSurvive));
                                if((t <= 0 || timeOnce) && eT >= timeBeforeEnemySpawn && enemies.size() < maxEnemyCount)
                                {
                                    if(!timeOnce)
                                    {
                                        t = timeToSurvive;
                                        timeOnce = true;
                                    }

                                    eT = 0;
                                    //Raylib.Vector2 pos = WorldGenerator.GetRandomLandPositionWithRadius(200, obstacleMap);
                                    List<int[]> path = null;

                                    int[] src = new int[2];
                                    int[] dst = new int[2];

                                    Random rand = new Random();

                                    dst[0] = 50;
                                    dst[1] = 50;

                                    boolean pathFound = false;
                                    while (!pathFound)
                                    {

                                        src[0] = rand.nextInt(90);
                                        src[1] = rand.nextInt(90);
                                        if(Raylib.Vector2Distance(new Jaylib.Vector2(src[0], src[1]), new Jaylib.Vector2(50, 50)) < 20)
                                        {
                                            continue;
                                        }
                                        path = AStarSearch.aStarSearch(obstacleMap, src,dst);

                                        if(path != null)
                                        {
                                            pathFound = true;
                                        }
                                    }

                                    enemies.add(new OnlineEnemy(new Transform(src[0] * 8, src[1] * 8,8,8), tick));
                                    //enemies.add(new OnlineEnemy(new Transform((int)pos.x(),(int)pos.y(),8,8), tick));
                                    int index = enemies.size() - 1;

                                    enemies.get(index).path = path;
                                    //enemies.get(index).path = AStarSearch.aStarSearch(obstacleMap, src,dst);

                                    String enemyData = "enemySpawned,"+
                                            enemies.get(index).enemyID + "," +
                                            enemies.get(index).transform.x + "," +
                                            enemies.get(index).transform.y;
                                    for(int i = 0; i < sockets.size(); i++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer(enemyData, CharsetUtil.UTF_8),
                                                sockets.get(i).socket
                                        ));
                                    }
                                }

                                for(int i = 0; i < sockets.size(); i++)
                                {
                                    ctx.writeAndFlush(new DatagramPacket(
                                            Unpooled.copiedBuffer("time,"+(int)t, CharsetUtil.UTF_8),
                                            sockets.get(i).socket
                                    ));
                                }

                                List<Integer> structuresToRemove = new ArrayList<>();

                                for(int i = 0; i < enemies.size(); i++)
                                {
                                    int pIndex = -1;
                                    enemies.get(i).MoveAlongPath();
                                    enemies.get(i).Update();

                                    for(int s = 0; s < onlineStructures.size(); s++)
                                    {
                                        if(enemies.get(i).collision.Collides(onlineStructures.get(s).collision))
                                        {
                                            Raylib.Vector2 offset = enemies.get(i).collision.GetMTV(onlineStructures.get(s).collision);

                                            enemies.get(i).transform.x -= offset.x();
                                            enemies.get(i).transform.y -= offset.y();

                                            if(enemies.get(i).CanDamage())
                                            {
                                                onlineStructures.get(s).health--;
                                                if (onlineStructures.get(s).health <= 0) {
                                                    structuresToRemove.add(s);
                                                }

                                                enemies.get(i).t = 0;
                                            }
                                        }
                                    }

                                    if(enemies.get(i).collision.Collides(house.collision) && enemies.get(i).CanDamage())
                                    {
                                        enemies.get(i).t = 0;

                                        house.health--;
                                        if(house.health <= 0)
                                        {
                                            for(int s = 0; s < sockets.size(); s++)
                                            {
                                                //0 means lost
                                                ctx.writeAndFlush(new DatagramPacket(
                                                        Unpooled.copiedBuffer("gameOver," + 0, CharsetUtil.UTF_8),
                                                        sockets.get(s).socket
                                                ));
                                            }
                                            group.shutdownGracefully();
                                        }

                                    }

                                }

                                for(int i = 0; i < onlineTowers.size(); i++)
                                {
                                    onlineTowers.get(i).Update(0.1666f);

                                    if(onlineTowers.get(i).CanShoot())
                                    {
                                        int closestEnemyIndex = -1;

                                        float min = 99999999;

                                        for(int e = 0; e < enemies.size(); e++)
                                        {
                                            int eX = enemies.get(e).transform.x + enemies.get(e).transform.height/2;
                                            int eY = enemies.get(e).transform.y + enemies.get(e).transform.height/2;
                                            Jaylib.Vector2 ePos = new Jaylib.Vector2(eX, eY);
                                            if(onlineTowers.get(i).IsInRadius(ePos))
                                            {
                                                float dst = Jaylib.Vector2Distance(ePos, onlineTowers.get(i).ourPos);

                                                if(dst < min)
                                                {
                                                    closestEnemyIndex = e;
                                                    min = dst;
                                                }
                                                //break;
                                            }
                                        }

                                        if(closestEnemyIndex != -1)
                                        {
                                            int eX = enemies.get(closestEnemyIndex).transform.x + enemies.get(closestEnemyIndex).transform.height/2;
                                            int eY = enemies.get(closestEnemyIndex).transform.y + enemies.get(closestEnemyIndex).transform.height/2;
                                            Jaylib.Vector2 ePos = new Jaylib.Vector2(eX, eY);
                                            OnlineServerProjectile newProjectile = new OnlineServerProjectile(new Transform(
                                                    (int) onlineTowers.get(i).ourPos.x(),
                                                    (int) onlineTowers.get(i).ourPos.y(),
                                                    6,6), Raylib.Vector2Subtract(ePos, onlineTowers.get(i).ourPos));
                                            onlineServerProjectiles.add(newProjectile);

                                            for(int s = 0; s < sockets.size(); s++)
                                            {
                                                ctx.writeAndFlush(new DatagramPacket(
                                                        Unpooled.copiedBuffer("projectileShot," +
                                                                        (int) onlineTowers.get(i).ourPos.x() + "," +
                                                                        (int) onlineTowers.get(i).ourPos.y(),
                                                                CharsetUtil.UTF_8),
                                                        sockets.get(s).socket
                                                ));
                                            }
                                        }
                                    }

                                }

                                List<Integer> projectilesToRemove = new ArrayList<>();
                                List<Integer> enemiesToRemove = new ArrayList<>();

                                for(int i = 0; i < onlineServerProjectiles.size(); i++)
                                {
                                    onlineServerProjectiles.get(i).Update(0.1666f);

                                    for(int s = 0; s < sockets.size(); s++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer("projectilePos," + i + "," +
                                                                onlineServerProjectiles.get(i).transform.x + "," +
                                                                onlineServerProjectiles.get(i).transform.y,
                                                        CharsetUtil.UTF_8),
                                                sockets.get(s).socket
                                        ));
                                    }

                                    if(onlineServerProjectiles.get(i).RanOutOfTime())
                                    {
                                        projectilesToRemove.add(i);
                                    }
                                    else
                                    {
                                        for(int e = 0; e < enemies.size(); e++)
                                        {
                                            if(enemies.get(e).collision.Collides(onlineServerProjectiles.get(i).collision))
                                            {
                                                projectilesToRemove.add(i);
                                                enemies.get(e).health--;
                                                if(enemies.get(e).health <= 0)
                                                {
                                                    if(!enemiesToRemove.contains(e))
                                                    {
                                                        enemiesToRemove.add(e);
                                                    }
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }

                                for(int i = 0; i < projectilesToRemove.size(); i++)
                                {
                                    int currentIndex = projectilesToRemove.get(i);
                                    onlineServerProjectiles.remove(currentIndex);

                                    for(int j = 0; j < projectilesToRemove.size(); j++)
                                    {
                                        if(projectilesToRemove.get(j) > currentIndex)
                                        {
                                            projectilesToRemove.set(j, projectilesToRemove.get(j) - 1);
                                        }
                                    }
                                    for(int s = 0; s < sockets.size(); s++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer("projectileDestroyed," + currentIndex, CharsetUtil.UTF_8),
                                                sockets.get(s).socket
                                        ));
                                    }
                                }

                                for(int i = 0; i < enemiesToRemove.size(); i++)
                                {
                                    int currentIndex = enemiesToRemove.get(i);


                                    for(int j = 0; j < enemiesToRemove.size(); j++)
                                    {
                                        if(enemiesToRemove.get(j) > currentIndex)
                                        {
                                            enemiesToRemove.set(j, enemiesToRemove.get(j) - 1);
                                        }
                                    }
                                    for(int s = 0; s < sockets.size(); s++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer("enemyKilled," + enemies.get(currentIndex).enemyID, CharsetUtil.UTF_8),
                                                sockets.get(s).socket
                                        ));

                                    }

                                    enemies.remove(currentIndex);
                                }

                                for(int i = 0; i < structuresToRemove.size(); i++)
                                {
                                    int currentIndex = structuresToRemove.get(i);


                                    for(int j = 0; j < structuresToRemove.size(); j++)
                                    {
                                        if(structuresToRemove.get(j) > currentIndex)
                                        {
                                            structuresToRemove.set(j, structuresToRemove.get(j) - 1);
                                        }
                                    }

                                    String structureData = "structureDestroyed,"+currentIndex;
                                    for(int so = 0; so < sockets.size(); so++)
                                    {
                                        ctx.writeAndFlush(new DatagramPacket(
                                                Unpooled.copiedBuffer(structureData, CharsetUtil.UTF_8),
                                                sockets.get(so).socket
                                        ));
                                    }

                                    int twIndex = -1;
                                    for(int tw = 0; tw < onlineTowers.size(); tw++)
                                    {
                                        if(onlineTowers.get(tw).structure == onlineStructures.get(currentIndex))
                                        {
                                            twIndex = tw;
                                            break;
                                        }
                                    }
                                    if(twIndex != -1)
                                    {
                                        onlineTowers.remove(twIndex);
                                    }
                                    onlineStructures.remove(currentIndex);
                                }

                                accumulator = 0;
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

    public void HandlePlayerJoin()
    {

    }

    public static void main(String[] args) throws Exception {
        new GameServer(8080).start();

    }
}