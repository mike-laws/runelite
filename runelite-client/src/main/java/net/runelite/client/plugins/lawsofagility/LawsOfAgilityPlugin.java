package net.runelite.client.plugins.lawsofagility;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.laws.Finder;
import net.runelite.client.laws.Interact;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;

@PluginDescriptor(
        name = "Laws of Agility",
        description = "Laws of Agility",
        tags = {"Laws", "Agility"}
)

//TODO: Agility tasks
//TODO: Find Token
//TODO: Loot Token

@Slf4j
public class LawsOfAgilityPlugin extends Plugin
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
    protected void shutDown() throws Exception {

    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click)
    {
        if(interact != null) interact.onMenuOptionClicked(click);
    }

    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if(interact != null) interact.onMenuEntryAdded(event);
    }
}
