package com.company.Components;

import com.raylib.Raylib;

import java.util.ArrayList;
import java.util.List;

import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.DrawTexture;
import static com.raylib.Raylib.LoadTexture;

enum StructureType
{
    WoodWall,
    StoneWall,
    Tower
}

public class Structure implements IUpdate
{
    public Collision collision;
    private Raylib.Texture texture;

    public static List<Structure> structures = new ArrayList<>();
    public StructureType type;

    public static List<Raylib.Texture> textures = new ArrayList<>();

    public Structure(Collision collision, StructureType type)
    {
        //texture = LoadTexture(path);
        this.collision = collision;
        this.type = type;
        //structures.add(this);

        if(type == StructureType.WoodWall)
            texture = textures.get(0);
        if(type == StructureType.StoneWall)
            texture = textures.get(1);
        if(type == StructureType.Tower)
            texture = textures.get(2);

        texture.width(collision.transform.width);
        texture.height(collision.transform.height);
    }

    public Structure(StructureData structureData)
    {
        collision = new Collision(new Transform(structureData.transform.x,structureData.transform.y, structureData.transform.width, structureData.  transform.height));

        type = structureData.type;

        if(type == StructureType.WoodWall)
            texture = textures.get(0);
        if(type == StructureType.StoneWall)
            texture = textures.get(1);
        if(type == StructureType.Tower)
            texture = textures.get(2);

        texture.width(collision.transform.width);
        texture.height(collision.transform.height);
        type = structureData.type;


    }

    @Override
    public void Update()
    {
        DrawTexture(texture, collision.transform.x, collision.transform.y, WHITE);
    }

    public static void LoadTextures()
    {
        textures.add(LoadTexture("Textures/wall1.png"));
        textures.add(LoadTexture("Textures/stoneWall.jpg"));
        textures.add(LoadTexture("Textures/towerIcon.png"));
    }
}
