package com.company.Components;

import com.raylib.Jaylib;

import java.util.ArrayList;
import java.util.List;

public class Collision
{
    public Transform transform;

    public static List<Collision> trees = new ArrayList<Collision>();

    public Collision(Transform transform)
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

    public boolean Collides(Jaylib.Vector2 mousePos)
    {
        if(transform.x > mousePos.x()) return false;
        if(transform.x + transform.width < mousePos.x()) return false;
        if(transform.y > mousePos.y()) return false;
        if(transform.y + transform.height < mousePos.y()) return false;

        return true;
    }

    public Jaylib.Vector2 GetMTV(Collision collision)
    {
        Jaylib.Vector2 ourPos = new Jaylib.Vector2(transform.x, transform.y);
        Jaylib.Vector2 ourSize = new Jaylib.Vector2(transform.width, transform.height);

        Jaylib.Vector2 colliderPos = new Jaylib.Vector2(collision.transform.x, collision.transform.y);
        Jaylib.Vector2 colliderSize = new Jaylib.Vector2(collision.transform.width, collision.transform.height);

        Jaylib.Vector2[] axis = {new Jaylib.Vector2 (0,1), new Jaylib.Vector2(1,0) };

        Jaylib.Vector2[] ourVertices = {new Jaylib.Vector2 (ourPos.x(), ourPos.y()),
                                        new Jaylib.Vector2 (ourPos.x() + ourSize.x(), ourPos.y()),
                                        new Jaylib.Vector2 (ourPos.x(), ourPos.y() + ourSize.y()),
                                        new Jaylib.Vector2 (ourPos.x() + ourSize.x(), ourPos.y() + ourSize.y())};

        Jaylib.Vector2[] colliderVertices = {new Jaylib.Vector2 (colliderPos.x(), colliderPos.y()),
                                             new Jaylib.Vector2 (colliderPos.x() + colliderSize.x(), colliderPos.y()),
                                             new Jaylib.Vector2 (colliderPos.x(), colliderPos.y() + colliderSize.y()),
                                             new Jaylib.Vector2 (colliderPos.x() + colliderSize.x(), colliderPos.y() + colliderSize.y())};

        float overlap = 9999;
        Jaylib.Vector2 smallestAxis = null;
        int flip = 1;

        for(int a = 0; a < 2; a++)
        {
            float ourMin = 99999;
            float ourMax = -99999;

            float colliderMin = 99999;
            float colliderMax = -99999;


            for (int i = 0; i < 4; i++)
            {
                float ourDot = Jaylib.Vector2DotProduct(axis[a], ourVertices[i]);
                float colliderDot = Jaylib.Vector2DotProduct(axis[a], colliderVertices[i]);

                if (ourDot < ourMin)
                {
                    ourMin = ourDot;
                }
                else if (ourDot > ourMax)
                {
                    ourMax = ourDot;
                }

                if (colliderDot < colliderMin)
                {
                    colliderMin = colliderDot;
                }
                else if (colliderDot > colliderMax)
                {
                    colliderMax = colliderDot;
                }
            }



            float o = Math.min(ourMax, colliderMax) - Math.max(ourMin, colliderMin);
            if (o < overlap)
            {
                if (ourMin < colliderMin)
                {
                    flip = 1;
                }
                else
                {
                    flip = -1;
                }

                overlap = o;
                smallestAxis = axis[a];
            }
        }


        overlap *= flip;
        assert smallestAxis != null;
        smallestAxis.x(smallestAxis.x() * overlap);
        smallestAxis.y(smallestAxis.y() * overlap);

        return smallestAxis;
    }
}
