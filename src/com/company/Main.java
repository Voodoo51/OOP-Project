package com.company;
import com.raylib.Jaylib;
import com.raylib.Raylib;

import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
//test
public class Main {

    public static void main(String[] args) {
        InitWindow(800, 800, "Demo");
        SetTargetFPS(60);
        //ToggleBorderlessWindowed();
        Camera2D camera = new Camera2D();
        Texture tex = LoadTexture("Textures/Glade1.png");
        tex.width(20);
        tex.height(20);
        WorldGenerator worldGenerator = new WorldGenerator(100, 100, 0.05, 0.1, 0.1, 8);
        Raylib.Vector2 target = new Jaylib.Vector2(0,0);
        float zoom = 1.0f;
        camera.zoom(zoom);
        camera.offset(new Jaylib.Vector2(GetScreenWidth()/2, GetScreenHeight()/2));
        camera.target(target);

        Collision playerCollider = new Collision(new Transform(0,0,20,20));

        float x = 0;
        float y = 0;

        while (!WindowShouldClose()) {
            BeginDrawing();

            BeginMode2D(camera);
            ClearBackground(BLUE);

            if(IsKeyDown(KEY_A))
            {
                x -= 5;
            }
            if(IsKeyDown(KEY_D))
            {
                x += 5;
            }
            if(IsKeyDown(KEY_S))
            {
                y += 5;
            }
            if(IsKeyDown(KEY_W))
            {
                y -= 5;
            }

            if(GetMouseWheelMove() > 0)
            {
                zoom += 0.1f;
            }
            if(GetMouseWheelMove() < 0)
            {
                zoom -= 0.1f;
            }
            playerCollider.transform.x = (int)x - 10;
            playerCollider.transform.y = (int)y - 10;

            for(int i = 0; i < Collision.trees.size(); i++)
            {
                if(playerCollider.Collides(Collision.trees.get(i)))
                {
                    System.out.println("COLLIDES");
                    break;
                }
            }
            worldGenerator.Draw();
            DrawTexture(tex, (int)target.x() - 10, (int)target.y() - 10, WHITE);

            target.x(x);
            target.y(y);
            camera.target(target);
            camera.zoom(zoom);
            EndMode2D();
            //DrawText("Hello world", 190, 200, 20, VIOLET);
            //DrawFPS(20, 20);

            EndDrawing();
        }
        CloseWindow();
    }
}
