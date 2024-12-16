package com.company.Components;
import com.company.NoiseGenerator;
import com.raylib.Jaylib;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
import java.lang.Math;


public class WorldGenerator implements IUpdate
{
    private int width;
    private int height;
    private double scale;
    private double treeScale;
    private double rockScale;
    private int tileSize;
    private int seed;

    private double noiseMap[][];
    private double fallOffMap[][];
    private int map[][];
    private List<Texture> textures;
    private List<Texture> resourceTextures;

    private double treeNoiseMap[][];
    private double rockNoiseMap[][];
    public List<Resource> resourceMap = new ArrayList<>();


    private float t = 0;
    //Send seed via network
    public WorldGenerator(int width, int height, double scale, double treeScale, double rockScale, int tileSize)
    {
        this.width = width;
        this.height = height;
        this.scale = scale;
        this.treeScale = treeScale;
        this.rockScale = rockScale;
        this.tileSize = tileSize;
        this.seed = seed;
        noiseMap = new double[width][height];
        treeNoiseMap = new double[width][height];
        rockNoiseMap = new double[width][height];
        textures = new ArrayList<Texture>();
        resourceTextures = new ArrayList<Texture>();
        map = new int[width][height];
        LoadTextures();

        ComponentManager.updateComponents.add(this);
    }

    public void Generate(int seed)
    {
        int treeSeed = seed - 100000;
        int rockSeed = seed - 200000;
        /*
        Random rand = new Random();
        int seed = rand.nextInt();
        int treeSeed = rand.nextInt();
        int rockSeed = rand.nextInt();
         */

        noiseMap = new double[width][height];
        fallOffMap = new double[width][height];
        Jaylib.Vector2 center = new Jaylib.Vector2(width/2, height/2);
        float maxDistance = Vector2Distance(center, new Jaylib.Vector2(0, 0));

        double a = 3;
        double b = 1.1f;

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                noiseMap[x][y] = NoiseGenerator.noise3_ImproveXY(seed, x * scale, y * scale, 0.0);
                treeNoiseMap[x][y] = NoiseGenerator.noise2_ImproveX(treeSeed, x * treeScale, y * treeScale);
                rockNoiseMap[x][y] = NoiseGenerator.noise2_ImproveX(rockSeed, x * rockScale, y * rockScale);
                //resourceMap[x][y] = null;//to mozna pewnie pominac
                Jaylib.Vector2 point = new Jaylib.Vector2(x,y);
                double fVal = Vector2Distance(point, center) / maxDistance;
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
                    //TODO(): FIX THIS
                    Random rand = new Random();
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
                        Tree tree = new Tree();
                        tree.collision = new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize));
                        tree.texID = 0;
                        resourceMap.add(tree);
                        /*
                        resourceMap[x][y] = new Tree();
                        resourceMap[x][y].collision = new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize));
                        resourceMap[x][y].texID = 0;
                         */
                        //Collision.trees.add(new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize)));
                    }
                    else if(rockNoiseMap[x][y] > 0.6)
                    {
                        Rock rock = new Rock();
                        rock.collision = new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize));
                        rock.texID = 1;
                        resourceMap.add(rock);
                    }
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


    @Override
    public void Update()
    {
        for(int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                DrawTexture(textures.get(map[x][y]), x * tileSize, y * tileSize, WHITE);
                /*
                if(resourceMap[x][y] != null)
                {
                    DrawTexture(resourceTextures.get(resourceMap[x][y].texID), x * tileSize, y * tileSize, WHITE);
                }*/
            }
        }

        for(int i = 0; i < resourceMap.size(); i++)
        {
            DrawTexture(resourceTextures.get(resourceMap.get(i).texID),
                    resourceMap.get(i).collision.transform.x,
                    resourceMap.get(i).collision.transform.y,
                    WHITE);
        }
    }
}
