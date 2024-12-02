package com.company;

public class Tree extends Resource
{
    public Collision collision;

    @Override
    public ResourceType GetType() {
        return ResourceType.Tree;
    }
}
