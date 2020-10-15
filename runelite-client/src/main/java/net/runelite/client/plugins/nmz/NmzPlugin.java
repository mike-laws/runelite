package net.runelite.client.plugins.nmz;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BooleanSupplier;

@PluginDescriptor(
        name = "Nightmare Zone Helper",
        description = "NMZ",
        tags = {"NMZ"}
)
@Slf4j
public class NmzPlugin extends Plugin
{
    @Inject
    private Client client;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private NmzOverlay overlay;

    private static final int[] NMZ_MAP_REGION = { 9033 };
    private static final int[] ABSORPTION_RANGE = { 50, 400, 600, 950 };
    private static final int[] OVERLOAD_POTION = { 11730, 11731, 11732, 11733 };
    private static final int[] ABSORPTION_POTION = { 11734, 11735, 11736, 11737 };

    private Random random = new Random();
    public int minAbsorption;
    public int maxAbsorption;
    public int points;
    private boolean drinking = false;

    private WidgetItem overload;
    private WidgetItem absorption;



    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        minAbsorption = getMinAbsorption();
        maxAbsorption = getMaxAbsorption();
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        try
        {
            if(!isInNightmareZone()) return;
            points = client.getVar(Varbits.NMZ_ABSORPTION);
            overload = getItem(OVERLOAD_POTION);
            absorption = getItem(ABSORPTION_POTION);
            loop();
        }
        catch (Exception e) { }
    }

    private void loop()
    {
        if(processAbsorption()) return;
        processOverload();
    }

    private boolean isOverloadActive()
    {
        return client.getBoostedSkillLevel(Skill.RANGED) > client.getRealSkillLevel(Skill.RANGED);
    }

    private boolean processAbsorption()
    {
        if(drinking)
        {
            if(points > maxAbsorption)
            {
                drinking = false;
                minAbsorption = getMinAbsorption();
                maxAbsorption = getMaxAbsorption();
                return false;
            }
            new Thread(() -> useAbsorptionPotion()).start();
            return true;
        }

        if(points < minAbsorption)
        {
            drinking = true;
            return true;
        }

        return false;
    }

    private void processOverload()
    {
        if(isOverloadActive()) return;
        new Thread(() -> useOverload()).start();
    }

    private void useOverload()
    {
        if(overload == null) return;
        useItem(overload);
        //waitUntil(() -> isOverloadActive(), 5000);
    }

    private void useItem(WidgetItem item)
    {
        Rectangle bounds = item.getCanvasBounds();

        double xOffset = bounds.width * 0.2;
        double yOffset = bounds.height * 0.2;
        double x = (random.nextDouble() * (bounds.width - xOffset)) + bounds.getMinX() + (xOffset / 2);
        double y = (random.nextDouble() * (bounds.height - yOffset)) + bounds.getMinY() + (yOffset / 2);

        clickItem((int)x, (int)y);
    }

    private WidgetItem getItem(int[] ids)
    {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if(inventory == null || inventory.isHidden()) return null;

        for (WidgetItem item : inventory.getWidgetItems())
        {
            if(Arrays.stream(ids).anyMatch(e -> e == item.getId()))
            {
                return item;
            }
        }
        return null;
    }

    private void useAbsorptionPotion()
    {
        if(absorption == null) return;
        //int points = this.points;
        useItem(absorption);
        //waitUntil(() -> this.points > points, 3000);
    }

    private int getMinAbsorption()
    {
        int min = ABSORPTION_RANGE[0];
        int max = ABSORPTION_RANGE[1];
        minAbsorption = random.nextInt(max - min) + min;
        return minAbsorption;
    }

    private int getMaxAbsorption()
    {
        int min = ABSORPTION_RANGE[2];
        int max = ABSORPTION_RANGE[3];
        maxAbsorption = random.nextInt(max - min) + min;
        return maxAbsorption;
    }

    public boolean isInNightmareZone()
    {
        if (client.getLocalPlayer() == null)
        {
            return false;
        }

        // NMZ and the KBD lair uses the same region ID but NMZ uses planes 1-3 and KBD uses plane 0
        return client.getLocalPlayer().getWorldLocation().getPlane() > 0 && Arrays.equals(client.getMapRegions(), NMZ_MAP_REGION);
    }

    //region Utility
    private boolean waitUntil(BooleanSupplier isComplete, long timeout)
    {
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < timeout)
        {
            if(isComplete.getAsBoolean())
            {
                return true;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void move(Point point)
    {
        move(point.getX(), point.getY());
    }

    public void move(int x, int y)
    {
        MouseEvent mouseEvent = new MouseEvent(
                client.getCanvas(),
                MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(),
                0,
                x,
                y,
                0,
                false);

        client.getCanvas().dispatchEvent(mouseEvent);
        sleep(50);
    }

    private void sleep(long millis)
    {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void clickItem(int x, int y)
    {
        move(x, y);
        click(x, y);
        sleep(50);
        MouseEvent me = new MouseEvent(client.getCanvas(), // which
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                0,
                x, y,
                1,
                false);
        client.getCanvas().dispatchEvent(me);
    }

    private void click(Point point)
    {
        click(point.getX(), point.getY());
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
    //endregion
}
