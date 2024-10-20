package com.company;
import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
//test
public class Main {

    public static void main(String[] args) {
        InitWindow(1600, 800, "Demo");
        SetTargetFPS(60);
        Camera2D camera = new Camera2D();
        Texture tex = LoadTexture("Textures/awesomeface.png");
        tex.width(20);
        tex.height(20);
        WorldGenerator worldGenerator = new WorldGenerator(100, 100, 0.1, 8);

        while (!WindowShouldClose()) {
            BeginDrawing();
            DrawTexture(tex, 100, 100, WHITE);
            ClearBackground(BLUE);
            worldGenerator.Draw();

            BeginMode2D(camera);
            EndMode2D();
            DrawText("Hello world", 190, 200, 20, VIOLET);
            DrawFPS(20, 20);

            EndDrawing();
        }
        CloseWindow();
    }
}
