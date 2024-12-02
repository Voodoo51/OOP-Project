package com.company;

enum ResourceType
{
    Tree,
    Rock
}

public abstract class Resource {
    public float x;
    public float y;

    public abstract ResourceType GetType();
}
