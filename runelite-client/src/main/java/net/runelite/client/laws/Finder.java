package net.runelite.client.laws;

import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.Tile;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

public class Finder
{
    private Client client;

    public Finder(Client client)
    {
        this.client = client;
    }

    public GameObject GetObject(Function<GameObject, Boolean> predicate)
    {
        return Arrays.stream(client.getScene().getTiles())
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .map(Tile::getGameObjects)
                .flatMap(Arrays::stream)
                .filter(Objects::nonNull)
                .filter(predicate::apply)
                .findFirst()
                .orElse(null);
    }

    public GameObject GetObject(int id)
    {
        return GetObject(e -> e.getId() == id);
    }

}
