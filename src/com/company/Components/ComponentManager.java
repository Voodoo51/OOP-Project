package com.company.Components;

import java.util.ArrayList;
import java.util.List;

public class ComponentManager
{
    public static List<IStart> startComponents = new ArrayList<>();
    public static List<IUpdate> updateComponents = new ArrayList<>();
    public static List<IUpdateUI> updateUIComponents = new ArrayList<>();

    public void Start()
    {
        for(int i = 0; i < startComponents.size(); i++)
            startComponents.get(i).Start();
    }

    public void Update()
    {
        for(int i = 0; i < updateComponents.size(); i++)
            updateComponents.get(i).Update();
    }

    public void UpdateUI()
    {
        for(int i = 0; i < updateUIComponents.size(); i++)
            updateUIComponents.get(i).UpdateUI();
    }
}

interface IStart
{
    public void Start();
}

interface IUpdate
{
    public void Update();
}

interface IUpdateUI
{
    public void UpdateUI();
}