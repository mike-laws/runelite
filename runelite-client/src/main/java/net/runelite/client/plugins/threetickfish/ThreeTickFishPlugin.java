package net.runelite.client.plugins.threetickfish;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;

import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.function.BooleanSupplier;

@PluginDescriptor(
        name = "Three Tick Fish",
        description = "Helps with fishing ticks",
        tags = {"Fishing"}
)
@Slf4j
public class ThreeTickFishPlugin extends Plugin
{
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private KeyManager keyManager;
    @Inject
    private ThreeTickFishOverlay overlay;
    @Inject
    private ThreeTickFishSceneOverlay sceneOverlay;
    @Inject
    private MouseManager mouseManager;

    private final HotkeyListener hotkeyListener1 = new HotkeyListener(() -> new Keybind(KeyEvent.VK_1, 0))
    {
        @Override
        public void hotkeyPressed()
        {
            new Thread(() -> fish()).start();
        }
    };

    private final HotkeyListener hotkeyListener2 = new HotkeyListener(() -> new Keybind(KeyEvent.VK_2, 0))
    {
        @Override
        public void hotkeyPressed()
        {
            new Thread(() -> {
                guam = findInventoryItem(GUAM_ID);
                clickItem(guam);

            }).start();
        }
    };

    private final HotkeyListener hotkeyListener3 = new HotkeyListener(() -> new Keybind(KeyEvent.VK_3, 0))
    {
        @Override
        public void hotkeyPressed()
        {
            new Thread(() -> {
                tar = findInventoryItem(TAR_ID);
                clickItem(tar);
            }).start();
        }
    };

    private final HotkeyListener hotkeyListener4 = new HotkeyListener(() -> new Keybind(KeyEvent.VK_4, 0))
    {
        @Override
        public void hotkeyPressed()
        {
            if(running)
                stop();
            else
                start();

        }
    };

    private final HotkeyListener hotkeyListener5 = new HotkeyListener(() -> new Keybind(KeyEvent.VK_5, 0))
    {
        @Override
        public void hotkeyPressed()
        {

        }
    };

    private static final int GUAM_ID = 249;
    private static final int TAR_ID = 1939;

    private MenuEntry fishingEntry;
    private MenuEntry entry;
    private LocalPoint fishingSpotLocation;

    private Point guam;
    private Point tar;
    private Point cancel;


    @Getter
    private boolean running;
    @Getter
    private NPC fishingSpot;

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        overlayManager.add(sceneOverlay);
        keyManager.registerKeyListener(hotkeyListener1);
        keyManager.registerKeyListener(hotkeyListener2);
        keyManager.registerKeyListener(hotkeyListener3);
        keyManager.registerKeyListener(hotkeyListener4);
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        overlayManager.remove(sceneOverlay);
        keyManager.unregisterKeyListener(hotkeyListener1);
        keyManager.unregisterKeyListener(hotkeyListener2);
        keyManager.unregisterKeyListener(hotkeyListener3);
        keyManager.unregisterKeyListener(hotkeyListener4);
    }

    //region Subscribes
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event){
        if(entry == null) return;

        MenuEntry[] entries = new MenuEntry[1];
        entries[0] = entry;

        client.setMenuEntries(entries);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked click)
    {
        if (entry == null) return;
        if(click.getId() == entry.getIdentifier()) entry = null;
    }

    @Subscribe
    public void onAnimationChanged(final AnimationChanged event)
    {
        if(!running) return;

        Player local = client.getLocalPlayer();

        if (event.getActor() != local)
            return;

        int animId = local.getAnimation();

        if(animId != 5249)
            return;

        log.info("Detected grinding animation");
        new Thread(() ->
        {
            cancelAnimation();
            if(!fish())
            {
                stop();
                return;
            }
            sleep(600);
            makeHerbSlow();
        }).start();
    };

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        if(!running) return;
        if(widgetLoaded.getGroupId() != WidgetID.LEVEL_UP_GROUP_ID) return;
        log.info("Level Up!");
        stop();

        new Thread(() -> {
            cancelAnimation();
            sleep(200);
            cancelAnimation();
            sleep(200);
            cancelAnimation();
        }).start();


        //Todo: Reset
    }

    private void onFishingSpotMoved()
    {
        log.info("Fishing spot moved!");
        stop();
        cancelAnimation();

    }
    //endregion

    private void startFishingListener()
    {
        new Thread(() -> {
            while(running)
            {
                if(fishingSpot == null)
                    continue;

                if (fishingSpotLocation == null) {
                    fishingSpotLocation = fishingSpot.getLocalLocation();
                    continue;
                }

                if (!fishingSpotLocation.equals(fishingSpot.getLocalLocation())) {
                    fishingSpotLocation = fishingSpot.getLocalLocation();
                    onFishingSpotMoved();
                }
                sleep(50);
            }
        }).start();
    }


    private void start()
    {
        running = true;

        fishingSpot = findFishingSpot();
        fishingEntry = createFishingEntry();

        if(fishingSpot == null)
            stop();

        startFishingListener();
    }

    private void reset()
    {
        fishingSpot = findFishingSpot();
        fishingEntry = createFishingEntry();

        if(fishingSpot == null)
            stop();

        sendAction(fishingEntry);
            //TODO: Wait until fishing...
    }

    private void stop()
    {
        running = false;
        fishingSpot = null;
        fishingSpotLocation = null;
        fishingEntry = null;
    }

    private boolean fish()
    {
        if(fishingSpot == null) return false;

        sendAction(fishingEntry);
        return true;
    }

    private void makeHerbSlow()
    {
        guam = findInventoryItem(GUAM_ID);
        if(guam == null) return;

        tar = findInventoryItem(TAR_ID);
        if(tar == null) return;

        int x = guam.getX();
        int y = tar.getY() - guam.getY();
        y = Math.min(guam.getY(), tar.getY()) + Math.abs(y);
        cancel = new Point(x, y);

        clickItem(guam);
        sleep(600);
        clickItem(tar);
    }

    private void cancelAnimation()
    {
        clickItem(sceneOverlay.getMinimapPlayerPoint());
    }

    //region Utility
    private Point findInventoryItem(int itemId)
    {
        Widget inventory = client.getWidget(WidgetInfo.INVENTORY);
        if(inventory == null) return null;

        WidgetItem item = inventory.getWidgetItems().stream()
                .filter(e -> e != null)
                .filter(e -> e.getId() == itemId)
                .findFirst()
                .orElse(null);

        if(item == null) return null;

        Rectangle canvasBounds = item.getCanvasBounds();
        return new Point((int)canvasBounds.getCenterX(), (int)canvasBounds.getCenterY());
    }

    private NPC findFishingSpot()
    {
        LocalPoint playerPosition = client.getLocalPlayer().getLocalLocation();

        return client.getNpcs().stream()
                .filter(e -> e != null)
                .filter(e -> getName(e).equals("Rod Fishing spot"))
                .min(Comparator.comparingInt(a -> {
                    LocalPoint position = a.getLocalLocation();
                    if(position == null)
                        return 1000;
                    return position.distanceTo(playerPosition);
                }))
                .orElse(null);
    }

    private String getName(NPC npc)
    {
        try
        {
            String name = npc.getName();
            return name == null ? "" : name;
        } catch (Exception e)
        {
            return "";
        }
    }

    private MenuEntry createFishingEntry()
    {
        if(fishingSpot == null) return null;

        MenuEntry entry = new MenuEntry();
        entry.setParam0(0);
        entry.setParam1(0);
        entry.setOption("Lure");
        entry.setTarget("<col=ffff00>" + fishingSpot.getName());
        entry.setType(MenuAction.NPC_FIRST_OPTION.getId());
        entry.setIdentifier(fishingSpot.getIndex());

        return entry;
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


    private void shiftClick(int x, int y)
    {
        MouseEvent me = new MouseEvent(client.getCanvas(), // which
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                InputEvent.SHIFT_DOWN_MASK + InputEvent.BUTTON1_DOWN_MASK,
                x, y,
                1,
                false);
        client.getCanvas().dispatchEvent(me);
    }

    private void clickItem(Point point)
    {
        clickItem(point.getX(), point.getY());
    }

    private void shiftClickItem(Point point)
    {
        shiftClickItem(point.getX(), point.getY());
    }

    private void shiftClickItem(int x, int y)
    {
        shiftMove(x, y);
        shiftClick(x, y);
        sleep(50);
        MouseEvent me = new MouseEvent(client.getCanvas(), // which
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                InputEvent.SHIFT_DOWN_MASK,
                x, y,
                1,
                false);
        client.getCanvas().dispatchEvent(me);

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

    private void moveAndClick(Point point)
    {
        moveAndClick(point.getX(), point.getY());
    }

    private void moveAndClick(int x, int y)
    {
        move(x, y);
        click(x, y);
    }

    private boolean sendAction(MenuEntry entry)
    {
        this.entry = entry;
        click(50, 50);
        return true;
        //return waitUntil(() -> entry == null, 5000);
    }

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

    public void shiftMove(int x, int y)
    {
        MouseEvent mouseEvent = new MouseEvent(
                client.getCanvas(),
                MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(),
                InputEvent.SHIFT_DOWN_MASK+InputEvent.BUTTON1_DOWN_MASK,
                x,
                y,
                1,
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
    //endregion





}
