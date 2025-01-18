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
        zoom = 5.0f;
        camera.zoom(zoom);
        ComponentManager.updateComponents.add(this);
    }

    @Override
    public void Update()
    {
        camera.target(new Jaylib.Vector2(target.x, target.y));
    }
}
