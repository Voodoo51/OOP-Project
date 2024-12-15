package com.company.Components;

enum ResourceType
{
    Wood,
    Rock
}

public abstract class Resource
{
    public Collision collision;
    public int texID;
    public ResourceType type;
}
