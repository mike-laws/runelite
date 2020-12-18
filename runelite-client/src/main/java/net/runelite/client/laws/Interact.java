package net.runelite.client.laws;

import net.runelite.api.*;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;

import java.awt.event.MouseEvent;

public class Interact
{
    private static final String OPTION = "Custom Option";

    private Client client;

    private MenuEntry entry;

    public Interact(Client client)
    {
        this.client = client;
    }

    private void sendAction(MenuEntry entry)
    {
        this.entry = entry;
        click(1,1);
    }

    public void interact(GameObject gameObject)
    {
        Point sceneMin = gameObject.getSceneMinLocation();
        Point sceneMax = gameObject.getSceneMaxLocation();

        //Todo: Calculate proper x, y positions for rotated objects.

        MenuEntry entry = new MenuEntry();
        entry.setIdentifier(gameObject.getId());
        entry.setParam0(sceneMin.getX());
        entry.setParam1(sceneMin.getY());
        entry.setType(MenuAction.GAME_OBJECT_FIRST_OPTION.getId());
        entry.setOption(OPTION);
        entry.setTarget(OPTION);
        sendAction(entry);
    }

    private void click(int x, int y)
    {
        MouseEvent me = new MouseEvent(client.getCanvas(), // which
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                0,
                x, y,
                1,
                false);
        client.getCanvas().dispatchEvent(me);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click)
    {
        if(entry == null) return;
        if(click.getMenuOption().equals(OPTION))
        {
            entry = null;
        }
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if(entry == null) return;
        MenuEntry[] entries = new MenuEntry[1];
        entries[0] = entry;

        client.setMenuEntries(entries);
    }
}
