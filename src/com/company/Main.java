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
        Camera2D camera = new Camera2D();
        Texture tex = LoadTexture("Textures/awesomeface.png");
        tex.width(20);
        tex.height(20);
        WorldGenerator worldGenerator = new WorldGenerator(100, 100, 0.1, 0.05, 0.05, 8);
        Raylib.Vector2 target = new Jaylib.Vector2(0,0);
        float zoom = 1.0f;
        camera.zoom(zoom);
        camera.offset(target);
        camera.target(target);

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

            DrawTexture(tex, 100, 100, WHITE);
            worldGenerator.Draw();
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
