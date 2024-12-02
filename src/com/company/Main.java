package com.company;
import com.raylib.Jaylib;
import com.raylib.Raylib;
import org.bytedeco.javacpp.BytePointer;
import org.w3c.dom.Text;

import java.util.ArrayList;

import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
//test
public class Main {

    public static void main(String[] args) {
        InitWindow(1920, 1080, "Demo");
        SetTargetFPS(60);
        //ToggleBorderlessWindowed();
        int woodCount = 0;
        Camera2D camera = new Camera2D();
        Texture tex = LoadTexture("Textures/Glade1.jpg");
        Texture woodIcon = LoadTexture("Textures/woodIcon.png");

        tex.width(20);
        tex.height(20);
        woodIcon.width(120);
        woodIcon.height(120);

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
            float moveX = 0;
            float moveY = 0;
            BeginDrawing();

            BeginMode2D(camera);
            ClearBackground(BLUE);

            if (IsKeyDown(KEY_A)) {
                moveX -= 1;
            }
            if (IsKeyDown(KEY_D)) {
                moveX += 1;
            }
            if (IsKeyDown(KEY_S)) {
                moveY += 1;
            }
            if (IsKeyDown(KEY_W)) {
                moveY -= 1;
            }

            if (GetMouseWheelMove() > 0) {
                zoom += 0.1f;
            }
            if (GetMouseWheelMove() < 0) {
                zoom -= 0.1f;
            }

            ArrayList<Jaylib.Vector2> offsets = new ArrayList<>();

            playerCollider.transform.x = (int) x - 10;
            playerCollider.transform.y = (int) y - 10;
            int xCount = 0;
            int yCount = 0;

            boolean b = false;
            for (int i = 0; i < Collision.trees.size(); i++) {

                float tempX = x;
                float tempY = y;

                //tempX += moveX;
                playerCollider.transform.x = (int)(tempX + moveX) - 10;
                playerCollider.transform.y = (int)(tempY) - 10;

                if (playerCollider.Collides(Collision.trees.get(i)))
                {
                    Jaylib.Vector2 mtv = playerCollider.GetMTV(Collision.trees.get(i));
                    moveX = 0;
                    x -= mtv.x();
                }
                playerCollider.transform.x = (int)(tempX) - 10;
                playerCollider.transform.y = (int)(tempY + moveY) - 10;

                if (playerCollider.Collides(Collision.trees.get(i)))
                {
                    Jaylib.Vector2 mtv = playerCollider.GetMTV(Collision.trees.get(i));
                    moveY = 0;
                    y -= mtv.y();
                }
            }

            x += moveX;
            y += moveY;

            Raylib.Vector2 mousePosR = Jaylib.GetMousePosition();
            mousePosR = GetScreenToWorld2D(mousePosR, camera);
            Jaylib.Vector2 mousePos = new Jaylib.Vector2(mousePosR.x(), mousePosR.y());

            //System.out.println(mousePos.x() + ":" + mousePos.y());
            int tID = -1;
            for (int i = 0; i < Collision.trees.size(); i++) {
                if (Collision.trees.get(i).Collides(mousePos)) {
                    //tileSize = 8
                    int xCoord = (int) mousePos.x() / 8;
                    int yCoord = (int) mousePos.y() / 8;
                    worldGenerator.resourceMap[xCoord][yCoord] = -1;
                    tID = i;

                    break;
                }
            }

            if (tID != -1)
            {
                Collision.trees.remove(tID);
                System.out.println(Collision.trees.size());
                woodCount++;
            }



            worldGenerator.Draw();
            DrawTexture(tex, (int)target.x() - 10, (int)target.y() - 10, WHITE);
 
            target.x(x);
            target.y(y);
            camera.target(target);
            camera.zoom(zoom);
            EndMode2D();
            BytePointer bytePointer =  new BytePointer();
            DrawText(Raylib.TextFormat("Drzewo: " + woodCount), 1500, 100, 50, WHITE);
            DrawTexture(woodIcon, 1350, 100 - 40, WHITE);

            //DrawFPS(20, 20);

            EndDrawing();
        }
        CloseWindow();
    }
}
