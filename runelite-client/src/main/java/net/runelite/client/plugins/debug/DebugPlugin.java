package net.runelite.client.plugins.debug;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.laws.Finder;
import net.runelite.client.laws.Interact;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Debug Info",
        description = "DebuggingInfo",
        tags = {"Debug"}
)
@Slf4j
public class DebugPlugin extends Plugin
{
    private Finder finder;
    private Interact interact;

    @Inject
    private Client client;

    @Override
    protected void startUp() throws Exception {
        finder = new Finder(client);
        interact = new Interact(client);
    }

    @Override
    protected void shutDown() throws Exception
    {
    }


    private int count;
    @Subscribe
    public void onGameTick(GameTick gameTick)
    {
        count++;
        if(count != 10) return;
        count = 0;
        GameObject gameObject = finder.GetObject(14843);
        if(gameObject != null)
            new Thread(() -> {interact.interact(gameObject);}).start();

    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click)
    {
        if(interact != null) interact.onMenuOptionClicked(click);
        log.info(click.toString());
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if(interact != null) interact.onMenuEntryAdded(event);
    }
}
