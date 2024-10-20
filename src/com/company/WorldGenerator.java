package com.company;
import com.raylib.Jaylib;
import com.raylib.Raylib;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;

public class WorldGenerator
{
    private int width;
    private int height;
    private double scale;
    private int tileSize;

    private double noiseMap[][];
    private double fallOffMap[][];
    private int map[][];
    private List<Texture> textures;

    WorldGenerator(int width, int height, double scale, int tileSize)
    {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.tileSize = tileSize;
        noiseMap = new double[width][height];
        textures = new ArrayList<Texture>();
        map = new int[width][height];
        LoadTextures();
        Generate();
    }

    private void Generate()
    {
        Random rand = new Random();
        int seed = rand.nextInt();

        noiseMap = new double[width][height];
        fallOffMap = new double[width][height];
        Jaylib.Vector2 center = new Jaylib.Vector2(width/2, height/2);
        float maxDistance = Jaylib.Vector2Distance(center, new Jaylib.Vector2(0, 0));

        double a = 3;
        double b = 1.1f;

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                noiseMap[x][y] = NoiseGenerator.noise3_ImproveXY(seed, x * scale, y * scale, 0.0);

                Jaylib.Vector2 point = new Jaylib.Vector2(x,y);
                double fVal = Jaylib.Vector2Distance(point, center) / maxDistance;
                double fPower = Math.pow(fVal, a);
                fallOffMap[x][y] = fPower/(fPower + Math.pow(b - b*fVal, a));
            }
        }

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                if(noiseMap[x][y] - fallOffMap[x][y] > 0.0)
                {
                    map[x][y] = 1; //Glade1.jpg
                }
                else
                {
                    map[x][y] = 0;
                }
            }
        }
    }

    public void Draw()
    {
        for(int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                double noise = noiseMap[x][y] - fallOffMap[x][y];
                int val = (int)(noise * 255);
                if(val < 0)
                {
                    val = 0;
                }
                //DrawRectangle(x * tileSize, y * tileSize, tileSize, tileSize, new Jaylib.Color(val, val, val, 255));
                DrawTexture(textures.get(map[x][y]), x * tileSize, y * tileSize, WHITE);
            }
        }
    }

    private void LoadTextures()
    {
        textures.add(LoadTexture("Textures/waterTest.png"));
        textures.get(0).width(tileSize);
        textures.get(0).height(tileSize);
        textures.add(LoadTexture("Textures/Glade1.jpg"));
        textures.get(1).width(tileSize);
        textures.get(1).height(tileSize);
    }
}
