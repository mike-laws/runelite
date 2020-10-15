package net.runelite.client.plugins.prayerhelper;

import com.google.inject.Provides;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.StatChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.api.widgets.WidgetItem;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.combat.CombatConfig;
import net.runelite.client.plugins.prayer.PrayerConfig;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collection;

@PluginDescriptor(
        name = "Prayer Helper",
        description = "Helps with praying",
        tags = {"prayer"},
        enabledByDefault = false
)
@Slf4j
public class PrayerHelperPlugin extends Plugin {

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private PrayerOverlay overlay;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private ItemManager itemManager;

    @Inject
    private PrayerHelperConfig config;

    public boolean shouldPray = false;

    private boolean disabled = false;

    private static final int[] NMZ_MAP_REGION = {9033};

    private boolean inNightmareZone;

    private boolean usePotion;

    private WidgetItem potionWidget;

    private boolean configUsePotion;

    @Provides
    PrayerHelperConfig provideConfig(ConfigManager configManager)
    {
        return configManager.getConfig(PrayerHelperConfig.class);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged)
    {
        if (!configChanged.getGroup().equals("prayerhelper"))
        {
            return;
        }

        configUsePotion = config.getUseOverloads();
        if(configUsePotion)
        {
            usePotion = true;
        }
    }

    public final HotkeyListener prayerKeyListener = new HotkeyListener(() -> new Keybind(KeyEvent.VK_EQUALS, 0))
    {
        @Override
        public void hotkeyPressed()
        {
            inNightmareZone = isInNightmareZone();
            if(inNightmareZone)
            {
                usePotion = true;
            }

            shouldPray = !shouldPray;
            if(!shouldPray)
            {
                disabled = true;
            }
        }
    };


    @Override
    protected void startUp() throws Exception
    {
        keyManager.registerKeyListener(prayerKeyListener);
        overlayManager.add(overlay);
        robot = new Robot();
    }

    @Override
    protected void shutDown() throws Exception
    {
        keyManager.unregisterKeyListener(prayerKeyListener);
        overlayManager.remove(overlay);
    }

    @Subscribe
    public void onGameTick(GameTick tick)
    {
        boolean prayerActive = isAnyPrayerActive();
        new Thread(() -> prayerFlick(prayerActive)).start();
    }


    @SneakyThrows
    private void prayerFlick(boolean prayerActive)
    {
        if(!shouldPray && disabled)
        {
            if(prayerActive)
            {
                activateQuickPrayer();
                disabled = false;
                return;
            }
        }

        if(!shouldPray) return;
        activateQuickPrayer();

        if(prayerActive)
        {
            Thread.sleep(100);
            activateQuickPrayer();
        }

        if(configUsePotion)
        {
            if(inNightmareZone)
            {
                if(usePotion) {
                    if (findPotion()) {
                        click(potionWidget);
                        usePotion = false;
                    }
                }
            }
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged)
    {
        if(statChanged.getSkill() != Skill.STRENGTH) return;
        if(statChanged.getBoostedLevel() == statChanged.getLevel()){
            usePotion = true;
        }
    }

    private final int[] overloads = {11730,11731,11732,11733};

    private boolean findPotion(){
        final Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        final Collection<WidgetItem> widgetItems = inventoryWidget.getWidgetItems();
        for(WidgetItem item : widgetItems)
        {
            if(Arrays.stream(overloads).anyMatch(i -> i == item.getId()))
            {
                usePotion = true;
                potionWidget = item;
                return true;
            }
        }
        return false;
    }

    private void activateQuickPrayer()
    {
        Widget orb = client.getWidget(WidgetInfo.MINIMAP_QUICK_PRAYER_ORB);
        if(orb == null) return;
        int x = orb.getCanvasLocation().getX() + (orb.getWidth() / 2);
        int y = orb.getCanvasLocation().getY() + (orb.getHeight() / 2);
        click(x, y);
    }



    private Robot robot;

    private void click(WidgetItem item)
    {
        Rectangle bounds = item.getCanvasBounds();
        click((int)bounds.getCenterX(), (int)bounds.getCenterY());
    }

    private void click(int x, int y)
    {
        MouseEvent mouseEvent = new MouseEvent(client.getCanvas(), MouseEvent.MOUSE_PRESSED, System.currentTimeMillis(), 0, x, y, 1, false);
        client.getCanvas().dispatchEvent(mouseEvent);
    }

    private void clickItem(net.runelite.api.Point point)
    {
        clickItem(point.getX(), point.getY());
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

    private boolean isAnyPrayerActive()
    {
        for (Prayer pray : Prayer.values())//Check if any prayers are active
        {
            if (client.isPrayerActive(pray))
            {
                return true;
            }
        }

        return false;
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

    /*
    @Subscribe
    public void onMenuEntryAdded(MenuEntryAdded event)
    {
        if(!usePotion) return;
        MenuEntry[] entries = { potionEntry };
        client.setMenuEntries(entries);
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked event)
    {
        if(!usePotion) return;
        if(Arrays.stream(overloads).anyMatch(i -> i == event.getId())){
            potionEntry = null;
            usePotion = false;
            log.info("Potion entry clicked!");
        }
    }
     */
}
