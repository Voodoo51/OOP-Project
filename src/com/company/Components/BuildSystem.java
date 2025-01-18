package com.company.Components;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import javax.swing.plaf.PanelUI;
import java.util.ArrayList;
import java.util.List;

import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.*;

class StructureData
{
    Transform transform;
    StructureType type;
    public int woodCost = 0;
    public int rockCost = 0;

    public StructureData(Transform transform, StructureType type)
    {
        this.transform = transform;
        this.type = type;

        if(type == StructureType.WoodWall)
        {
            woodCost = 2;
            rockCost = 0;
        }
        if(type == StructureType.StoneWall)
        {
            woodCost = 0;
            rockCost = 2;
        }
        if(type == StructureType.Tower)
        {
            woodCost = 8;
            rockCost = 1;
        }
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
    public List<Texture> resourcesTextures = new ArrayList<>();
    public StructureData currentStructure = null;
    public Collision currentStructureCollision;
    public int structure;
    public Camera2D camera;
    private Player player;

    public BuildSystem(Camera2D camera, Player player)
    {
        this.player = player;
        int halfX = (int) (GetScreenWidth()/2.0f);
        int halfY = (int) (GetScreenHeight()/2.0f);
        int offset = 200;

        this.camera = camera;

        ComponentManager.updateUIComponents.add(this);
        ComponentManager.updateComponents.add(this);

        currentStructureCollision = new Collision(new Transform(0,0, 6, 6));

        textures.add(LoadTexture("Textures/wall1.png"));
        textures.add(LoadTexture("Textures/stoneWall.jpg"));
        textures.add(LoadTexture("Textures/towerIcon.png"));

        resourcesTextures.add(LoadTexture("Textures/woodIcon.png"));
        resourcesTextures.add(LoadTexture("Textures/rockIcon.png"));
        resourcesTextures.get(0).width(50);
        resourcesTextures.get(0).height(50);
        resourcesTextures.get(1).width(50);
        resourcesTextures.get(1).height(50);

        structureUIs.add(new StructureUI(0,
                new Collision(new Transform(halfX - offset, halfY + 300, 100, 100)),
                new StructureData(new Transform(0,0,6,6), StructureType.WoodWall)));
        offset = 0;
        structureUIs.add(new StructureUI(1,
                new Collision(new Transform(halfX - offset, halfY + 300, 100, 100)),
                new StructureData(new Transform(0,0,6,6), StructureType.StoneWall)));
        offset = -200;
        structureUIs.add(new StructureUI(2,
                new Collision(new Transform(halfX - offset, halfY + 300, 100, 100)),
                new StructureData( new Transform(0,0,6,8), StructureType.Tower)));
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

                DrawTexture(resourcesTextures.get(0), structureUIs.get(i).collision.transform.x, structureUIs.get(i).collision.transform.y + 100, WHITE);
                DrawText(Raylib.TextFormat( structureUIs.get(i).structureData.woodCost + ""), structureUIs.get(i).collision.transform.x + 50, structureUIs.get(i).collision.transform.y + 100, 20, WHITE);

                DrawTexture(resourcesTextures.get(1), structureUIs.get(i).collision.transform.x + 50, structureUIs.get(i).collision.transform.y + 100, WHITE);
                DrawText(Raylib.TextFormat( structureUIs.get(i).structureData.rockCost + ""), structureUIs.get(i).collision.transform.x + 100, structureUIs.get(i).collision.transform.y + 100, 20, WHITE);

            }

            if(IsMouseButtonPressed(0))
            {
                Raylib.Vector2 mousePosR = Jaylib.GetMousePosition();
                Jaylib.Vector2 mousePos = new Jaylib.Vector2(mousePosR.x(), mousePosR.y());

                for(int i = 0; i < structureUIs.size(); i++)
                    if(structureUIs.get(i).collision.Collides(mousePos) &&
                       player.wood.count >= structureUIs.get(i).structureData.woodCost &&
                       player.rock.count >= structureUIs.get(i).structureData.rockCost)
                    {

                        state = BuildSystemState.StructureSelected;
                        currentStructure = structureUIs.get(i).structureData;
                        //structure = structureUIs.get(i).texture;
                        currentStructureCollision.transform.width = structureUIs.get(i).structureData.transform.width;
                        currentStructureCollision.transform.height = structureUIs.get(i).structureData.transform.height;

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
