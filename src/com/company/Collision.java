package com.company;

import java.util.ArrayList;
import java.util.List;

public class Collision
{
    public Transform transform;

    public static List<Collision> trees = new ArrayList<Collision>();

    Collision(Transform transform)
    {
        this.transform = transform;
    }

    public boolean Collides(Collision collider)
    {
        if(transform.x > collider.transform.x + collider.transform.width) return false;
        if(transform.x + transform.width < collider.transform.x) return false;
        if(transform.y > collider.transform.y + collider.transform.height) return false;
        if(transform.y + transform.height < collider.transform.y) return false;

        return true;
    }
}
