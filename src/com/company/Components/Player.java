package com.company.Components;

import com.raylib.Jaylib;
import com.raylib.Raylib;
import com.raylib.Raylib.*;

import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.*;
import static com.raylib.Raylib.KEY_W;

public class Player implements IUpdate
{
    public Transform transform;
    private Raylib.Texture texture;
    private Collision collision;

    private ResourceUI wood;
    private ResourceUI rock;
    private WorldGenerator worldGenerator;
    private Camera2D camera;

    private GameClient gameClient;

    public Player(String path, Transform transform, WorldGenerator worldGenerator, Camera2D camera) throws Exception {
        texture = LoadTexture(path);
        this.transform = transform;
        collision = new Collision(this.transform);
        texture.width(transform.width);
        texture.height(transform.height);

        ComponentManager.updateComponents.add(this);
        this.worldGenerator = worldGenerator;
        this.camera = camera;

        wood = new ResourceUI("Textures/woodIcon.png",
                new Transform(1500, 60, 120, 120),
                new Transform(1650, 100, 0, 50),
                ResourceType.Wood);

        rock = new ResourceUI("Textures/rockIcon.png",
                new Transform(1000, 60, 120, 120),
                new Transform(1150, 100, 0, 50),
                ResourceType.Rock);

        gameClient = new GameClient("localhost", 8080);
        gameClient.start();
    }

    @Override
    public void Update()
    {
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

        transform.x = prevX + moveX;
        transform.y = prevY + moveY;

        Raylib.Vector2 mousePosR = Jaylib.GetMousePosition();
        mousePosR.x(mousePosR.x() + transform.width/2);
        mousePosR.y(mousePosR.y() + transform.height/2);
        mousePosR = GetScreenToWorld2D(mousePosR, camera);
        Jaylib.Vector2 mousePos = new Jaylib.Vector2(mousePosR.x(), mousePosR.y());

        for (int i = 0; i < worldGenerator.resourceMap.size(); i++) {
            if (worldGenerator.resourceMap.get(i).collision.Collides(mousePos)) {

                if(worldGenerator.resourceMap.get(i).type == ResourceType.Wood)
                    wood.count++;
                else if(worldGenerator.resourceMap.get(i).type == ResourceType.Rock)
                    rock.count++;

                worldGenerator.resourceMap.remove(i);

                break;
            }
        }

        DrawTexture(texture, transform.x - transform.width/2, transform.y - transform.height/2, WHITE);
    }
}
