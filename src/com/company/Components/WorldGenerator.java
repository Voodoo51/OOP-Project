package com.company.Components;
import com.company.NoiseGenerator;
import com.raylib.Jaylib;
import com.raylib.Raylib;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import static com.raylib.Raylib.*;
import static com.raylib.Jaylib.*;
import java.lang.Math;
import java.util.Stack;


public class WorldGenerator implements IUpdate
{
    private int width;
    private int height;
    private double scale;
    private double treeScale;
    private double rockScale;
    private int tileSize;
    private int seed;
    private static float homeRadius = 5;

    private double noiseMap[][];
    private double fallOffMap[][];
    private int map[][];
    private List<Texture> textures;
    private List<Texture> resourceTextures;

    private double treeNoiseMap[][];
    private double rockNoiseMap[][];
    public List<Resource> resourceMap = new ArrayList<>();
    public List<Collision> waterCollision = new ArrayList<>();

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
                Jaylib.Vector2 tilePos = new Jaylib.Vector2(x, y);

                if(Jaylib.Vector2Distance(tilePos, center) < homeRadius)
                {
                    Random rand = new Random();
                    int rVal = rand.nextInt(3);

                    if(rVal == 0)
                    {
                        map[x][y] = 1; //GladeFlower.jpg
                    }
                    else if(rVal == 2)
                    {
                        map[x][y] = 2; //GladeFlower.jpg
                    }
                    else
                    {
                        map[x][y] = 3; //GladeFlower.jpg
                    }
                    continue;
                }

                /*
                if(x >= 40 && x <= 60 && y >= 40 && y <= 60)
                {
                    if(rVal == 0)
                    {
                        map[x][y] = 1; //GladeFlower.jpg
                    }
                    else if(rVal == 2)
                    {
                        map[x][y] = 2; //GladeFlower.jpg
                    }
                    else
                    {
                        map[x][y] = 3; //GladeFlower.jpg
                    }
                    continue;
                }
                */

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

        GenerateWaterCollision();
    }

    public static int[][] GetObstacleMap(int width, int height, double scale, double treeScale, double rockScale, int seed)
    {
        int[][] obstacleMap = new int[width][height];
        int treeSeed = seed - 100000;
        int rockSeed = seed - 200000;

        Jaylib.Vector2 center = new Jaylib.Vector2(width/2, height/2);
        float maxDistance = Vector2Distance(center, new Jaylib.Vector2(0, 0));
        double a = 3;
        double b = 1.1f;

        double[][] noiseMap = new double[width][height];
        double[][] treeNoiseMap = new double[width][height];
        double[][] rockNoiseMap = new double[width][height];
        double[][] fallOffMap = new double[width][height];

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
                Jaylib.Vector2 tilePos = new Jaylib.Vector2(x, y);

                if(Jaylib.Vector2Distance(tilePos, center) < homeRadius)
                {
                    obstacleMap[x][y] = 1; //Glade1.jpg

                    continue;
                }

                /*
                if(x >= 40 && x <= 60 && y >= 40 && y <= 60)
                {
                    obstacleMap[x][y] = 1; //Glade1.jpg

                    continue;
                }
                */

                if(noiseMap[x][y] - fallOffMap[x][y] > 0.1)
                {
                    obstacleMap[x][y] = 1; //Glade1.jpg
                    if(treeNoiseMap[x][y] > 0.35)
                    {
                        obstacleMap[x][y] = 0;
                    }
                    else if(rockNoiseMap[x][y] > 0.6)
                    {
                        obstacleMap[x][y] = 0;
                    }
                }
                else
                {
                    obstacleMap[x][y] = 0;
                }
            }
        }

        return obstacleMap;
    }

    private void GenerateWaterCollision()
    {
        int[][] pickedColliders = new int[width][height];
        java.util.function.BiConsumer<Integer, Integer> PlaceNewColliderAt = (x, y) -> {
            if(x < 0 && x >= width && y < 0 && y >= height) return;

            if(pickedColliders[x][y] == 0)
            {
                waterCollision.add(new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize)));
                pickedColliders[x][y] = 1;
            }

        };

        for(int x = 0; x < width; x++)
        {
            for(int y = 0; y < height; y++)
            {
                if(map[x][y] != 0) continue;

                waterCollision.add(new Collision(new Transform(x * tileSize, y * tileSize, tileSize, tileSize)));
                /*
                if(map[x][y] == 0) continue;

                PlaceNewColliderAt.accept(x - 1, y);
                PlaceNewColliderAt.accept(x + 1, y);
                PlaceNewColliderAt.accept(x, y - 1);
                PlaceNewColliderAt.accept(x, y + 1);
                */
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


        resourceTextures.add(LoadTexture("Textures/tree.png"));
        resourceTextures.get(0).width(tileSize);
        resourceTextures.get(0).height(tileSize);
        resourceTextures.add(LoadTexture("Textures/rock.png"));
        resourceTextures.get(1).width(tileSize);
        resourceTextures.get(1).height(tileSize);
    }

    public Raylib.Vector2 GetRandomLandPositionWithinRadius(Transform transform, float radius)
    {
        Raylib.Vector2 pos = new Jaylib.Vector2();

        boolean posFound = false;

        Random rand = new Random();
        Jaylib.Vector2 center = new Jaylib.Vector2(50,50);
        Collision collision = new Collision(new Transform(0, 0, transform.width, transform.height));

        while(!posFound)
        {
            int x = rand.nextInt(100);
            int y = rand.nextInt(100);
            pos.x(x);
            pos.y(y);
            if(Jaylib.Vector2Distance(pos, center) > radius)
            {
                continue;
            }

            if(map[x][y] != 0)
            {
                collision.transform.x = x * tileSize;
                collision.transform.y = y * tileSize;
                boolean collided = false;
                for(int i = 0; i < resourceMap.size(); i++)
                {
                    if(collision.Collides(resourceMap.get(i).collision))
                    {
                        collided = true;
                        break;
                    }
                }

                for(int i = 0; i < waterCollision.size(); i++)
                {
                    if(collision.Collides(waterCollision.get(i)))
                    {
                        collided = true;
                        break;
                    }
                }

                if(!collided)
                {
                    posFound = true;
                    pos.x(x * tileSize);
                    pos.y(y * tileSize);
                }
            }
        }

        return pos;
    }

    public static Raylib.Vector2 GetRandomLandPositionWithRadius(float radius, int[][] obstacleMap)
    {
        Raylib.Vector2 pos = new Jaylib.Vector2();

        boolean posFound = false;

        Random rand = new Random();

        Raylib.Vector2 center = new Jaylib.Vector2();
        center.x(obstacleMap.length/2 * 8);
        center.y(obstacleMap[0].length/2 * 8);

        while(!posFound)
        {
            int x = rand.nextInt(100);
            int y = rand.nextInt(100);

            pos.x(x * 8);
            pos.y(y * 8);
            System.out.println("FROM: " + center.x() +" : " + center.y() + " TO " + pos.x() + " : " + pos.y() + "  =  " + Jaylib.Vector2Distance(pos, center) + " -- " + obstacleMap[x][y]);

            if(Jaylib.Vector2Distance(pos, center) < radius)
            {
                continue;
            }

            if(obstacleMap[x][y] != 0)
            {
                if(FloodFillToCenter(obstacleMap, new Jaylib.Vector2(x, y)))
                {
                    posFound = true;

                    //pos.x(x * 16);
                    //pos.y(y * 16);
                    System.out.println("POS FOUND AT: " + x + " : " + y);
                }
            }
        }

        return pos;
    }

    public static boolean FloodFillToCenter(int[][] obstacleMap, Raylib.Vector2 pos)
    {
        int[][] visited = new int[obstacleMap.length][obstacleMap[0].length];

        Stack<Raylib.Vector2> moveStack = new Stack<>();
        moveStack.add(pos);
        while(!moveStack.empty())
        {
            Raylib.Vector2 currentPos = moveStack.pop();
            int x = (int)currentPos.x();
            int y = (int)currentPos.y();

            if(x == 50 && y == 50)
            {
                return true;
            }

            if(x >= 0 && x < visited.length && y >= 0 && y < visited[0].length)
            {
                if(obstacleMap[x][y] == 0)
                {
                    continue;
                }

                if(visited[x][y] == 1)
                {
                    continue;
                }

                visited[x][y] = 1;

                Jaylib.Vector2 up = new Jaylib.Vector2(currentPos.x(), currentPos.y() + 1);

                Jaylib.Vector2 down = new Jaylib.Vector2(currentPos.x(), currentPos.y() - 1);

                Jaylib.Vector2 right = new Jaylib.Vector2(currentPos.x() + 1, currentPos.y());

                Jaylib.Vector2 left = new Jaylib.Vector2(currentPos.x() - 1, currentPos.y());

                up.x(currentPos.x() - 1);
                up.y(currentPos.y());

                moveStack.add(up);
                moveStack.add(down);
                moveStack.add(right);
                moveStack.add(left);
            }
        }

        return false;
    }

    static double lerp(double a, double b, double f)
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
