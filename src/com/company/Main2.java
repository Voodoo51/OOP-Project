package com.company;
import com.company.Components.*;
import com.company.Components.Transform;
import com.raylib.Jaylib;
import com.raylib.Raylib;
import org.bytedeco.javacpp.BytePointer;

import java.util.ArrayList;
import java.util.Random;

import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
//test
public class Main2 {

    public static Camera2D camera;

    public static void InitGame() throws Exception {


        InitWindow(1920, 1080, "Demo");
        SetTargetFPS(60);
        //ToggleBorderlessWindowed();
        WorldGenerator worldGenerator = new WorldGenerator(100, 100, 0.05, 0.1, 0.1, 8);


        camera = new Camera2D();
        camera.offset(new Jaylib.Vector2(GetScreenWidth()/2.0f, GetScreenHeight()/2.0f));

        Player player = new Player(2,"Textures/human.png", new Transform(0,0,20,20), worldGenerator, camera);
        Player player2 = new Player(1,"Textures/human.png", new Transform(0,0,20,20), worldGenerator, camera);
        CameraFollow cameraFollow = new CameraFollow(player.transform, camera);

        ComponentManager componentManager = new ComponentManager();

        while (!WindowShouldClose()) {
            BeginDrawing();

            BeginMode2D(camera);
            ClearBackground(BLUE);
            componentManager.Update();
            EndMode2D();
            componentManager.UpdateUI();

            //DrawFPS(20, 20);
            EndDrawing();
        }
        CloseWindow();
    }

    public static void main(String[] args) throws Exception
    {



        InitGame();
        /*
        new Thread(() -> {
            try {
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //Do whatever
        }).start();
        */
    }
}