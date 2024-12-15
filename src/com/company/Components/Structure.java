package com.company.Components;

import com.raylib.Raylib;

import java.util.ArrayList;
import java.util.List;

import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.DrawTexture;
import static com.raylib.Raylib.LoadTexture;

public class Structure implements IUpdate
{
    public Collision collision;
    private Raylib.Texture texture;

    public static List<Structure> structures = new ArrayList<>();

    public Structure(String path, Collision collision)
    {
        texture = LoadTexture(path);
        this.collision = collision;

        texture.width(collision.transform.width);
        texture.width(collision.transform.height);

        //structures.add(this);
    }

    public Structure(StructureData structureData)
    {
        texture = LoadTexture(structureData.path);
        collision = new Collision(new Transform(structureData.transform.x,structureData.transform.y, structureData.transform.width, structureData.  transform.height));
        texture.width(collision.transform.width);
        texture.height(collision.transform.height);
    }

    @Override
    public void Update()
    {
        DrawTexture(texture, collision.transform.x, collision.transform.y, WHITE);
    }
}
