package com.company.Components;

import com.raylib.Jaylib;
import com.raylib.Raylib;

import static com.raylib.Raylib.GetMouseWheelMove;

public class CameraFollow implements IUpdate
{
    private Raylib.Camera2D camera;
    private Transform target;
    private float zoom;

    public CameraFollow(Transform target, Raylib.Camera2D camera)
    {
        this.target = target;
        this.camera = camera;
        zoom = 1.0f;
        ComponentManager.updateComponents.add(this);
    }

    @Override
    public void Update()
    {
        if (GetMouseWheelMove() > 0)
            zoom += 0.1f;
        if (GetMouseWheelMove() < 0)
            zoom -= 0.1f;

        camera.target(new Jaylib.Vector2(target.x, target.y));
        camera.zoom(zoom);
    }
}
