package com.company.Components;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.*;
import static com.raylib.Raylib.KEY_W;

class OnlineProjectile
{
    public Transform transform;

    public OnlineProjectile(Transform transform)
    {
        this.transform = transform;
    }
}

class OnlinePlayer
{
    public Transform transform;
    public Collision collision;
    public int playerNumber;

    public OnlinePlayer(Transform transform, int playerNumber)
    {
        this.transform = new Transform(transform.x, transform.y, transform.width, transform.height);
        //texture = LoadTexture(path);
        collision = new Collision(this.transform);
        this.playerNumber = playerNumber;
        //texture.width(this.transform.width);
        //texture.height(this.transform.height);
    }
}

enum EnemyDir
{
    None,
    Up,
    Down,
    Left,
    Right
}

class OnlineEnemy
{
    List<int[]> path = new ArrayList<>();
    public Transform transform;
    public int enemyID;
    public Collision collision;
    public int health = 3;
    private float speed = 1;

    public boolean pickNext = true;
    private EnemyDir dir = EnemyDir.None;
    private int[] dst = new int[2];

    public float t = 0;
    private float cooldown = 10;

    public void Update()
    {
        t += 0.16666f;
    }

    public boolean CanDamage()
    {
        if(t >= cooldown)
        {
            return true;
        }

        return false;
    }

    public OnlineEnemy(Transform transform, int enemyID)
    {
        this.transform = transform;
        this.enemyID = enemyID;
        collision = new Collision(this.transform);
    }

    public void MoveAlongPath()
    {
        if(path.size() == 0)
        {
            Jaylib.Vector2 ourPos = new Jaylib.Vector2(transform.x, transform.y);
            Jaylib.Vector2 dest =  new Jaylib.Vector2(50 * 8, 50 * 8);
            if(Jaylib.Vector2Distance(ourPos, dest) > 3)
            {
                Raylib.Vector2 dirToHouse = Jaylib.Vector2Subtract(dest, ourPos);

                dirToHouse = Jaylib.Vector2Normalize(dirToHouse);

                transform.x += dirToHouse.x() * speed;
                transform.y += dirToHouse.y() * speed;
            }
            //transform.x += path.
        }
        else
        {
            /*
            dst = path.get(0);
            Jaylib.Vector2 ourPos = new Jaylib.Vector2(transform.x, transform.y);
            Jaylib.Vector2 dest =  new Jaylib.Vector2(dst[0] * 8, dst[1] * 8);
            if(Jaylib.Vector2Distance(ourPos, dest) > 3)
            {
                Raylib.Vector2 nextTileDir = Jaylib.Vector2Subtract(dest, ourPos);

                nextTileDir = Jaylib.Vector2Normalize(nextTileDir);

                transform.x += nextTileDir.x() * speed;
                transform.y += nextTileDir.y() * speed;
                System.out.println("DIR: " + (int)nextTileDir.x() * speed + " : " + (int)nextTileDir.y() * speed );
            }
            else
            {
                path.remove(0);
            }

            */
            //System.out.println("");

            int xOffset = 0;
            int yOffset = 0;
            if(dir == EnemyDir.None)
            {
                dst = path.get(0);
                path.remove(0);
                float deltaX = Math.abs(transform.x - dst[0]*8 + xOffset);
                float deltaY = Math.abs(transform.y - dst[1]*8 + yOffset);
                if(deltaX > deltaY)
                {
                    if(transform.x < dst[0] * 8)
                    {
                        dir = EnemyDir.Right;
                        //transform.x += speed;
                    }
                    else
                    {
                        dir = EnemyDir.Left;
                        //transform.x -= speed;
                    }
                }
                else
                {
                    if(transform.y < dst[1] * 8)
                    {
                        dir = EnemyDir.Up;
                        //transform.y += speed;
                    }
                    else
                    {
                        dir = EnemyDir.Down;
                        //transform.y -= speed;
                    }
                }
            }
            else if(dir == EnemyDir.Up)
            {
                if(transform.y > dst[1] * 8 + yOffset)
                    dir = EnemyDir.None;
            }
            else if(dir == EnemyDir.Down)
            {
                if(transform.y < dst[1] * 8 + yOffset)
                    dir = EnemyDir.None;
            }
            else if(dir == EnemyDir.Right)
            {
                if(transform.x > dst[0] * 8 + xOffset)
                    dir = EnemyDir.None;
            }
            else if(dir == EnemyDir.Left)
            {
                if(transform.x < dst[0] * 8 + xOffset)
                    dir = EnemyDir.None;
            }

            switch (dir)
            {
                case EnemyDir.Up:
                    transform.y += speed;
                    break;
                case EnemyDir.Down:
                    transform.y -= speed;
                    break;
                case EnemyDir.Right:
                    transform.x += speed;
                    break;
                case EnemyDir.Left:
                    transform.x -= speed;
                    break;
            }

        }
    }
}

enum GameState
{
    InProgress,
    Lost,
    Won
}

public class Player implements IUpdate, IUpdateUI
{
    public Transform transform;
    private List<Texture> playerTextures = new ArrayList<Texture>();
    private Texture enemyTexture;
    private Texture houseTexture;
    private Texture projectileTexture;

    private Collision collision;

    public ResourceUI wood;
    public ResourceUI rock;
    private WorldGenerator worldGenerator;
    private Camera2D camera;

    private List<OnlinePlayer> onlinePlayers = new ArrayList<>();
    private List<OnlineEnemy> onlineEnemies = new ArrayList<>();
    private List<OnlineProjectile> onlineProjectiles = new ArrayList<>();

    public GameClient gameClient;
    private BuildSystem buildSystem;

    static boolean generated = false;
    private float time = 0;
    private GameState gameState = GameState.InProgress;
    public boolean canExit = false;
    private float t = 0;
    private float timeBeforeShutdown = 10.0f;
    public int playerNumber;
    private int winCount;
    private String winCountFilePath;

    public Player(int playerNumber, Transform transform, WorldGenerator worldGenerator, Camera2D camera) throws Exception
    {
        this.playerNumber = playerNumber;
        enemyTexture = LoadTexture("Textures/enemy.png");
        enemyTexture.width(transform.width);
        enemyTexture.height(transform.height);
        houseTexture = LoadTexture("Textures/houseTest.png");
        houseTexture.width(8);
        houseTexture.height(8);
        projectileTexture = LoadTexture("Textures/boulderTest.png");
        projectileTexture.width(6);
        projectileTexture.height(6);
        this.transform = transform;
        collision = new Collision(this.transform);

        if(playerNumber == 1)
        {
            winCountFilePath = "wins1.txt";
        }
        else if(playerNumber == 2)
        {
            winCountFilePath = "wins2.txt";
        }
        else if(playerNumber == 3)
        {
            winCountFilePath = "wins3.txt";
        }


        playerTextures.add(LoadTexture("Textures/player1.png"));
        playerTextures.add(LoadTexture("Textures/player2.png"));
        playerTextures.add(LoadTexture("Textures/player2.png"));
        for(int i = 0; i < playerTextures.size(); i++)
        {
            playerTextures.get(i).width(transform.width);
            playerTextures.get(i).height(transform.height);
        }

        ComponentManager.updateComponents.add(this);
        ComponentManager.updateUIComponents.add(this);
        this.worldGenerator = worldGenerator;
        this.camera = camera;

        buildSystem = new BuildSystem(camera, this);

        wood = new ResourceUI("Textures/woodIcon.png",
                new Transform(1500, 60, 120, 120),
                new Transform(1650, 100, 0, 50),
                ResourceType.Wood);

        rock = new ResourceUI("Textures/rockIcon.png",
                new Transform(1100, 60, 120, 120),
                new Transform(1250, 100, 0, 50),
                ResourceType.Rock);

        gameClient = new GameClient("localhost", 8080, this);
        gameClient.start();
        while (!gameClient.seedReceived)
        {
            System.out.println("waiting for seed");
        }


        //Random rand = new Random();
        //int seed = rand.nextInt();

        if(!generated)
        {
            worldGenerator.Generate(gameClient.seed);
            generated = true;
        }

        Raylib.Vector2 pos = worldGenerator.GetRandomLandPositionWithinRadius(this.transform, 5);
        this.transform.x = (int) pos.x();
        this.transform.y = (int) pos.y();

        gameClient.RequestMapState();
        gameClient.RequestEnemies();

        GetWinCount();

        while (!gameClient.allPlayersConnected)
        {
            System.out.println("waiting for players");
        }
    }

    @Override
    public void Update()
    {
        for(int i = 0; i < onlinePlayers.size(); i++)
        {
            DrawTexture(playerTextures.get(onlinePlayers.get(i).playerNumber - 1),
                    onlinePlayers.get(i).transform.x - onlinePlayers.get(i).transform.width/2,
                    onlinePlayers.get(i).transform.y  - onlinePlayers.get(i).transform.height/2,
                    WHITE);
        }

        for(int i = 0; i < onlineProjectiles.size(); i++)
        {
            DrawTexture(projectileTexture,
                    onlineProjectiles.get(i).transform.x,
                    onlineProjectiles.get(i).transform.y,
                    WHITE);
        }

        for(int i = 0; i < onlineEnemies.size(); i++)
        {
            DrawTexture(enemyTexture,
                    onlineEnemies.get(i).transform.x,
                    onlineEnemies.get(i).transform.y,
                    WHITE);
        }

        DrawTexture(houseTexture, 50 * 8, 50 * 8, WHITE);
        DrawTexture(playerTextures.get(playerNumber - 1), transform.x - transform.width/2, transform.y - transform.height/2, WHITE);

        if(gameState != GameState.InProgress) return;

        int moveX = 0;
        int moveY = 0;
        if (IsKeyDown(KEY_A))
            moveX -= 1;
        if (IsKeyDown(KEY_D))
            moveX += 1;
        if (IsKeyDown(KEY_S))
            moveY += 1;
        if (IsKeyDown(KEY_W))
            moveY -= 1;

        if(IsKeyPressed(KEY_TAB))
        {
            if(buildSystem.state == BuildSystemState.Off)
                buildSystem.state = BuildSystemState.InMenu;
            else if(buildSystem.state == BuildSystemState.InMenu || buildSystem.state == BuildSystemState.StructureSelected)
            {
                buildSystem.state = BuildSystemState.Off;
            }
        }

        if(IsMouseButtonPressed(0))
        {
            if(buildSystem.state == BuildSystemState.StructureSelected)
                HandleBuilding();
            else
                HandleCollect();
        }

        //Also updates position
        HandleCollision(moveX, moveY);

        gameClient.SendPosition(playerNumber, transform.x, transform.y);
    }

    public void HandleCollision(int moveX, int moveY)
    {
        int prevX = transform.x;
        int prevY = transform.y;
        for (int i = 0; i < worldGenerator.resourceMap.size(); i++)
        {
            //tempX += moveX;
            collision.transform.x = prevX + moveX - transform.width/2; //(int)(tempX + moveX) - 10;
            collision.transform.y = prevY - transform.height/2;//(int)(tempY) - 10;

            if (collision.Collides(worldGenerator.resourceMap.get(i).collision))
            {
                Jaylib.Vector2 mtv = collision.GetMTV(worldGenerator.resourceMap.get(i).collision);
                moveX = 0;
                prevX -= mtv.x();
            }

            collision.transform.x = prevX - transform.width/2; //(int)(tempX + moveX) - 10;
            collision.transform.y = prevY + moveY - transform.height/2;

            if (collision.Collides(worldGenerator.resourceMap.get(i).collision))
            {
                Jaylib.Vector2 mtv = collision.GetMTV(worldGenerator.resourceMap.get(i).collision);
                moveY = 0;
                prevY -= mtv.y();
            }
        }

        for (int i = 0; i < worldGenerator.waterCollision.size(); i++)
        {
            //tempX += moveX;
            collision.transform.x = prevX + moveX - transform.width/2; //(int)(tempX + moveX) - 10;
            collision.transform.y = prevY - transform.height/2;//(int)(tempY) - 10;

            if (collision.Collides(worldGenerator.waterCollision.get(i)))
            {
                Jaylib.Vector2 mtv = collision.GetMTV(worldGenerator.waterCollision.get(i));
                moveX = 0;
                prevX -= mtv.x();
            }

            collision.transform.x = prevX - transform.width/2; //(int)(tempX + moveX) - 10;
            collision.transform.y = prevY + moveY - transform.height/2;

            if (collision.Collides(worldGenerator.waterCollision.get(i)))
            {
                Jaylib.Vector2 mtv = collision.GetMTV(worldGenerator.waterCollision.get(i));
                moveY = 0;
                prevY -= mtv.y();
            }
        }

        for (int i = 0; i < Structure.structures.size(); i++)
        {
            //tempX += moveX;
            collision.transform.x = prevX + moveX - transform.width/2; //(int)(tempX + moveX) - 10;
            collision.transform.y = prevY - transform.height/2;//(int)(tempY) - 10;

            if (collision.Collides(Structure.structures.get(i).collision))
            {
                Jaylib.Vector2 mtv = collision.GetMTV(Structure.structures.get(i).collision);
                moveX = 0;
                prevX -= mtv.x();
            }

            collision.transform.x = prevX - transform.width/2; //(int)(tempX + moveX) - 10;
            collision.transform.y = prevY + moveY - transform.height/2;

            if (collision.Collides(Structure.structures.get(i).collision))
            {
                Jaylib.Vector2 mtv = collision.GetMTV(Structure.structures.get(i).collision);
                moveY = 0;
                prevY -= mtv.y();
            }
        }

        transform.x = prevX + moveX;
        transform.y = prevY + moveY;
    }

    public void HandleCollect()
    {
        Vector2 mousePosR = GetMousePosition();
        mousePosR.x(mousePosR.x() + transform.width/2);
        mousePosR.y(mousePosR.y() + transform.height/2);
        mousePosR = GetScreenToWorld2D(mousePosR, camera);
        Jaylib.Vector2 mousePos = new Jaylib.Vector2(mousePosR.x(), mousePosR.y());

        for (int i = 0; i < worldGenerator.resourceMap.size(); i++)
        {
            if (worldGenerator.resourceMap.get(i).collision.Collides(mousePos))
            {
                if(worldGenerator.resourceMap.get(i).type == ResourceType.Wood)
                    wood.count++;
                else if(worldGenerator.resourceMap.get(i).type == ResourceType.Rock)
                    rock.count++;

                int x, y = 0;
                x = worldGenerator.resourceMap.get(i).collision.transform.x;
                y = worldGenerator.resourceMap.get(i).collision.transform.y;
                worldGenerator.resourceMap.remove(i);
                gameClient.SendResourceDestroy(playerNumber, i, x, y);
                break;
            }
        }
    }

    public void HandleBuilding()
    {
        for (int i = 0; i < worldGenerator.resourceMap.size(); i++)
        {
            if (worldGenerator.resourceMap.get(i).collision.Collides(buildSystem.currentStructureCollision))
                return;
        }

        for (int i = 0; i < worldGenerator.waterCollision.size(); i++)
        {
            if (worldGenerator.waterCollision.get(i).Collides(buildSystem.currentStructureCollision))
                return;
        }

        for(int i = 0; i < Structure.structures.size(); i++)
        {
            if(Structure.structures.get(i).collision.Collides(buildSystem.currentStructureCollision))
                return;
        }

        for(int i = 0; i < onlinePlayers.size(); i++)
        {
            if(buildSystem.currentStructureCollision.Collides(onlinePlayers.get(i).collision))
                return;
        }

        if(collision.Collides(buildSystem.currentStructureCollision)) return;

        Structure structure = new Structure(buildSystem.currentStructure);

        wood.count -= buildSystem.currentStructure.woodCost;
        rock.count -= buildSystem.currentStructure.rockCost;

        Structure.structures.add(structure);
        ComponentManager.updateComponents.add(structure);
        gameClient.SendStructure(
                playerNumber,
                structure.collision.transform.x,
                structure.collision.transform.y,
                structure.collision.transform.width,
                structure.collision.transform.height,
                structure.type.toString());

        buildSystem.state = BuildSystemState.Off;
    }

    public void OnNewPlayerJoined(int playerNumber)
    {
        if(this.playerNumber == playerNumber) return;

        for(int i = 0; i < onlinePlayers.size(); i++)
        {
            if(onlinePlayers.get(i).playerNumber == playerNumber)
            {
                return;
            }
        }

        onlinePlayers.add(new OnlinePlayer(new Transform(0, 0, transform.width, transform.height), playerNumber));
    }

    public void OnStructureReceived(int playerNumber, int x, int y, int w, int h, String type)
    {
        if(this.playerNumber == playerNumber) return;

        StructureType sType = null;

        if(type.equals("WoodWall"))
            sType = StructureType.WoodWall;
        if(type.equals("StoneWall"))
            sType = StructureType.StoneWall;
        if(type.equals("Tower"))
            sType = StructureType.Tower;


        Structure structure = new Structure(new Collision(new Transform(x, y, w, h)), sType);
        Structure.structures.add(structure);
        ComponentManager.updateComponents.add(structure); //needed to make it render itself
    }

    public void OnStructureDestroyed(int sID)
    {
        if(Structure.structures.size() == 0) return;;

        Structure structure = Structure.structures.get(sID);
        Structure.structures.remove(structure);
        ComponentManager.updateComponents.remove(structure);
    }

    public void UpdatePlayerPosition(int playerNumber, int x, int y)
    {
        for(int i = 0; i < onlinePlayers.size(); i++)
        {
            if(onlinePlayers.get(i).playerNumber == playerNumber)
            {
                onlinePlayers.get(i).transform.x = x;
                onlinePlayers.get(i).transform.y = y;
                return;
            }
        }
    }

    public void UpdatePlayers(int playerNumber)
    {
        int index = 0;
        for(int i = 0; i < onlinePlayers.size(); i++)
        {
            if(onlinePlayers.get(i).playerNumber == playerNumber)
            {
                index = i;
                break;
            }
        }

        onlinePlayers.remove(index);
    }

    public void UpdateResources(int playerNumber, int rID)
    {
        if(this.playerNumber == playerNumber) return;

        worldGenerator.resourceMap.remove(rID);
    }

    public void OnEnemySpawned(int enemyID, int x, int y)
    {
        onlineEnemies.add(new OnlineEnemy(new Transform(x,y, 8, 8), enemyID));
    }

    public void OnEnemyKilled(int enemyID)
    {
        int indexToRemove = -1;
        for(int i = 0; i < onlineEnemies.size(); i++)
        {
            if(onlineEnemies.get(i).enemyID == enemyID)
            {
                indexToRemove = i;
            }
        }
        if(indexToRemove != -1)
        {
            onlineEnemies.remove(indexToRemove);
        }
    }

    public void OnProjectileShot(int x, int y)
    {
        onlineProjectiles.add(new OnlineProjectile(new Transform(x,y, 6, 6)));
    }

    public void UpdateProjectilePosition(int pID, int x, int y)
    {
        onlineProjectiles.get(pID).transform.x = x;
        onlineProjectiles.get(pID).transform.y = y;
    }

    public void OnProjectileDestroyed(int pID)
    {
        onlineProjectiles.remove(pID);
    }

    public void UpdateEnemyPosition(int enemyID, int x, int y)
    {
        for(int i = 0; i < onlineEnemies.size(); i++)
        {
            if(enemyID == onlineEnemies.get(i).enemyID)
            {
                onlineEnemies.get(i).transform.x = x;
                onlineEnemies.get(i).transform.y = y;
                break;
            }
        }
    }

    public void OnGameOver(int state)
    {
        if(state == 1)
        {
            gameState = GameState.Won;
            SaveWinCount();
        }
        if(state == 0)
        {
            gameState = GameState.Lost;
        }
    }

    public void DrawWonScreen()
    {
        DrawText(Raylib.TextFormat( "YOU WON"), 1920/2, 1080/2, 100, WHITE);
    }

    public void DrawLostScreen()
    {
        DrawText(Raylib.TextFormat( "YOU LOST"), 1920/2, 1080/2, 100, WHITE);
    }

    public void UpdateTime(float time)
    {
        this.time = time;
    }

    public void GetWinCount()
    {
        try
        {
            File file = new File(winCountFilePath);
            Scanner fileReader = new Scanner(file);

            if (file.exists())
            {
                winCount = fileReader.nextInt();
                fileReader.close();
            }
            else
            {
                System.out.println("The file does not exist.");
            }
        }
        catch (FileNotFoundException e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

    }

    public void SaveWinCount()
    {
        try
        {
            FileWriter fileWriter = new FileWriter(winCountFilePath, false);
            fileWriter.write("" + (winCount+1));
            fileWriter.close();

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void UpdateUI()
    {

        if(t >= timeBeforeShutdown)
        {
            canExit = true;
        }

        if(gameState == GameState.Lost)
        {
            t += 0.016666f;
            DrawLostScreen();
        }
        else if(gameState == GameState.Won)
        {
            t += 0.016666f;
            DrawWonScreen();
        }

        int minutes = (int) (time / 60);
        int seconds = (int) (time - (minutes * 60));
        DrawText(Raylib.TextFormat( + minutes + ":" + seconds), 1920/2 - 100, 100, 100, WHITE);
        DrawText(Raylib.TextFormat( "Wins: " + winCount), 150, 100, 100, WHITE);
    }
}
