package com.company;
import com.raylib.Jaylib;
import com.raylib.Raylib;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
import java.lang.Math;


public class WorldGenerator
{
    private int width;
    private int height;
    private double scale;
    private double treeScale;
    private double rockScale;
    private int tileSize;

    private double noiseMap[][];
    private double fallOffMap[][];
    private int map[][];
    private List<Texture> textures;
    private List<Texture> resourceTextures;

    private double treeNoiseMap[][];
    private double rockNoiseMap[][];
    private int resourceMap[][];


    private float t = 0;
    //Send seed via network
    WorldGenerator(int width, int height, double scale, double treeScale, double rockScale, int tileSize)
    {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.treeScale = treeScale;
        this.rockScale = rockScale;
        this.tileSize = tileSize;
        noiseMap = new double[width][height];
        treeNoiseMap = new double[width][height];
        rockNoiseMap = new double[width][height];
        resourceMap = new int[width][height];
        textures = new ArrayList<Texture>();
        resourceTextures = new ArrayList<Texture>();
        map = new int[width][height];
        LoadTextures();
        Generate();
    }

    private void Generate()
    {
        Random rand = new Random();
        int seed = rand.nextInt();
        int treeSeed = rand.nextInt();
        int rockSeed = rand.nextInt();

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
                treeNoiseMap[x][y] = NoiseGenerator.noise2_ImproveX(treeSeed, x * treeScale, y * treeScale);
                rockNoiseMap[x][y] = NoiseGenerator.noise2_ImproveX(rockSeed, x * rockScale, y * rockScale);
                resourceMap[x][y] = -1;
                Jaylib.Vector2 point = new Jaylib.Vector2(x,y);
                double fVal = Jaylib.Vector2Distance(point, center) / maxDistance;
                double fPower = Math.pow(fVal, a);
                fVal = fPower/(fPower + Math.pow(b - b*fVal, a));
                fallOffMap[x][y] = lerp( -0.5, 1, fVal);
            }
        }

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                if(noiseMap[x][y] - fallOffMap[x][y] > 0.1)
                {
                    int rVal = rand.nextInt(2);

                    if(rVal == 0)
                    {
                        map[x][y] = 2; //GladeFlower.jpg
                    }
                    else
                    {
                        map[x][y] = 3; //GladeFlower.jpg
                    }
                }
                else if(noiseMap[x][y] - fallOffMap[x][y] > -0.1)
                {
                    map[x][y] = 1; //Glade1.jpg
                }
                else
                {
                    map[x][y] = 0;
                }

                if(noiseMap[x][y] - fallOffMap[x][y] > -0.1)
                {
                    if(treeNoiseMap[x][y] > 0.35)
                    {
                        resourceMap[x][y] = 0;
                        Collision.trees.add(new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize)));
                    }
                    else if(rockNoiseMap[x][y] > 0.6)
                    {
                        resourceMap[x][y] = 1;
                    }
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
                int val2 = (int)(treeNoiseMap[x][y] * 255);
                int val3 = (int)(fallOffMap[x][y] * 255);
                if(val < 0)
                {
                    val = 0;
                }
                if(val2 < 0)
                {
                    val2 = 0;
                }
                //DrawRectangle(x * tileSize, y * tileSize, tileSize, tileSize, new Jaylib.Color(val, val, val, 255));
                //DrawRectangle(x * tileSize, y * tileSize, tileSize, tileSize, new Jaylib.Color(val3, val3, val3, 255));
                DrawTexture(textures.get(map[x][y]), x * tileSize, y * tileSize, WHITE);
                //float sin = (float)(Math.sin(t/10000)/2 + 0.5);
                //int valS = (int)(sin * 255);
                //DrawTexture(textures.get(map[x][y]), x * tileSize, y * tileSize, new Jaylib.Color(valS, valS, valS, 255));
                t += 0.01;
                if(resourceMap[x][y] != -1)
                {
                    DrawTexture(resourceTextures.get(resourceMap[x][y]), x * tileSize, y * tileSize, WHITE);
                }
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
        textures.add(LoadTexture("Textures/GladeFlower.jpg"));
        textures.get(2).width(tileSize);
        textures.get(2).height(tileSize);
        textures.add(LoadTexture("Textures/GladeFlower2.jpg"));
        textures.get(3).width(tileSize);
        textures.get(3).height(tileSize);


        resourceTextures.add(LoadTexture("Textures/testTree.png"));
        resourceTextures.get(0).width(tileSize);
        resourceTextures.get(0).height(tileSize);
        resourceTextures.add(LoadTexture("Textures/testRock.png"));
        resourceTextures.get(1).width(tileSize);
        resourceTextures.get(1).height(tileSize);
    }

    double lerp(double a, double b, double f)
    {
        return a * (1.0 - f) + (b * f);
    }
}
