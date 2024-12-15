package com.company.Components;

import com.raylib.Raylib;

import static com.raylib.Jaylib.WHITE;
import static com.raylib.Raylib.*;

public class ResourceUI implements IUpdateUI
{
    public int count = 0;
    private Raylib.Texture icon;
    private Transform iconTrasnform;
    private Transform textTrasnform;
    private ResourceType resourceType;

    public ResourceUI(String path, Transform iconTransform, Transform textTransform, ResourceType resourceType)
    {
        icon = LoadTexture(path);
        this.iconTrasnform = iconTransform;
        this.textTrasnform = textTransform;
        this.resourceType = resourceType;

        icon.width(iconTransform.width);
        icon.height(iconTransform.height);
        ComponentManager.updateUIComponents.add(this);
    }

    @Override
    public void UpdateUI()
    {
        DrawTexture(icon, iconTrasnform.x, iconTrasnform.y, WHITE);
        DrawText(Raylib.TextFormat(resourceType.toString() +": " + count), textTrasnform.x, textTrasnform.y, textTrasnform.height, WHITE);
    }
}
