package com.company.Components;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.List;

import static com.company.Main.camera;
import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.*;

class StructureData
{
    String path;
    Transform transform;

    public StructureData(String path, Transform transform)
    {
        this.path = path;
        this.transform = transform;
    }
}

class StructureUI
{
    public Collision collision;
    public StructureData structureData;
    public int textureID;

    public StructureUI(int textureID, Collision collision, StructureData structureData)
    {
        this.textureID = textureID;
        this.collision = collision;
        this.structureData = structureData;
    }
}

enum BuildSystemState
{
    Off,
    InMenu,
    StructureSelected
}

public class BuildSystem implements IUpdateUI, IUpdate
{
    public BuildSystemState state = BuildSystemState.Off;
    public List<StructureUI> structureUIs = new ArrayList<>();
    public List<Texture> textures = new ArrayList<>();
    public StructureData currentStructure = null;
    public Collision currentStructureCollision;
    public int structure;


    public BuildSystem()
    {
        int halfX = (int) (GetScreenWidth()/2.0f);
        int halfY = (int) (GetScreenHeight()/2.0f);
        int offset = 200;

        ComponentManager.updateUIComponents.add(this);
        ComponentManager.updateComponents.add(this);

        currentStructureCollision = new Collision(new Transform(0,0, 16, 16));

        textures.add(LoadTexture("Textures/woodWall.png"));
        textures.add(LoadTexture("Textures/stoneWall.jpg"));
        textures.add(LoadTexture("Textures/towerIcon.png"));

        structureUIs.add(new StructureUI(0,
                new Collision(new Transform(halfX - offset, halfY + 300, 100, 100))
                ,new StructureData("Textures/woodWall.png",
                new Transform(0,0,16,16))));
        offset = 0;
        structureUIs.add(new StructureUI(1,
                new Collision(new Transform(halfX - offset, halfY + 300, 100, 100))
                ,new StructureData("Textures/stoneWall.jpg",
                new Transform(0,0,16,16))));
        offset = -200;
        structureUIs.add(new StructureUI(2,
                new Collision(new Transform(halfX - offset, halfY + 300, 100, 100))
                ,new StructureData("Textures/towerIcon.png",
                new Transform(0,0,16,16))));
    }

    @Override
    public void UpdateUI()
    {
        if(state == BuildSystemState.Off) return;

        if(state == BuildSystemState.InMenu)
        {
            for(int i = 0; i < structureUIs.size(); i++)
            {
                textures.get(structureUIs.get(i).textureID).width(structureUIs.get(i).collision.transform.width);
                textures.get(structureUIs.get(i).textureID).height(structureUIs.get(i).collision.transform.height);
                DrawTexture(textures.get(structureUIs.get(i).textureID), structureUIs.get(i).collision.transform.x, structureUIs.get(i).collision.transform.y, WHITE);
            }

            if(IsMouseButtonPressed(0))
            {
                Raylib.Vector2 mousePosR = Jaylib.GetMousePosition();
                Jaylib.Vector2 mousePos = new Jaylib.Vector2(mousePosR.x(), mousePosR.y());

                for(int i = 0; i < structureUIs.size(); i++)
                    if(structureUIs.get(i).collision.Collides(mousePos))
                    {
                        state = BuildSystemState.StructureSelected;
                        currentStructure = structureUIs.get(i).structureData;
                        //structure = structureUIs.get(i).texture;

                        structure = structureUIs.get(i).textureID;
                        //structure = new Structure(structureUIs.get(i).structureData);
                        //ComponentManager.updateComponents.add(structure);

                        //structure.width(currentStructure.transform.width);
                        //structure.height(currentStructure.transform.height);
                        break;
                    }
            }
        }
    }

    @Override
    public void Update()
    {
        if(state != BuildSystemState.StructureSelected) return;

        Raylib.Vector2 mousePosR = Jaylib.GetMousePosition();
        mousePosR = GetScreenToWorld2D(mousePosR, camera);
        Jaylib.Vector2 mousePos = new Jaylib.Vector2(mousePosR.x(), mousePosR.y());

        currentStructureCollision.transform.x = (int)mousePos.x();
        currentStructureCollision.transform.y = (int)mousePos.y();

        textures.get(structure).width(currentStructureCollision.transform.width);
        textures.get(structure).height(currentStructureCollision.transform.height);

        currentStructure.transform = currentStructureCollision.transform;

        DrawTexture(textures.get(structure), (int)mousePos.x(), (int)mousePos.y(), WHITE);
    }
}
