package net.runelite.client.plugins.autogather;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.Keybind;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.function.BooleanSupplier;

@PluginDescriptor(
        name = "Auto Gatherer",
        description = "Helps with gathering skills",
        tags = {"Auto", "Gatherer"}
)
@Slf4j
public class AutoGatherPlugin extends Plugin
{
    public enum Mode
    {
        WOODCUTTING("Woodcutting"),
        MINING("Mining"),
        FISHING("Fishing");

        Mode(String name)
        {
            this.name = name;
        }

        @Getter
        private String name;
    }

    public class Action
    {
        @Getter
        private Mode mode;

        private String option;

        private String description;

        @Getter
        private String name;

        @Getter
        private MenuOptionClicked lastClicked;

        public Action(String option, String description, Mode mode)
        {
            this.option = option;
            this.description = description;
            this.mode = mode;
        }

        public boolean isMatch(MenuOptionClicked menuOptionClicked)
        {
            if(menuOptionClicked.getMenuOption().equals(option))
            {
                lastClicked = menuOptionClicked;
                name = mode == Mode.FISHING ? "Fishing spot" : cleanName(lastClicked.getMenuTarget());
                return true;
            }
            return false;
        }

        public boolean isMatch(GameObject gameObject)
        {
            if(mode == Mode.FISHING) return false;
            if(lastClicked == null) return false;

            return gameObject.getId() == lastClicked.getId();
        }

        public boolean isMatch(NPC npc)
        {
            if(mode != Mode.FISHING) return false;
            if(lastClicked == null) return false;

            return lastClicked.getMenuTarget().contains(npc.getName());
        }


    }

    private Action[] actions = {
            new Action("Chop down", "Chopping", Mode.WOODCUTTING),
            new Action("Mine", "Mining", Mode.MINING),
            new Action("Net","Netting", Mode.FISHING),
            new Action("Bait", "Bating", Mode.FISHING),
            new Action("Harpoon","Harpooning", Mode.FISHING),
            new Action("Lure","Luring", Mode.FISHING),
            new Action("Cage","Caging", Mode.FISHING),
            new Action("Fish","Fishing", Mode.FISHING),
        };

    @Getter
    private String status = "";

    @Getter
    private final Set<GameObject> gameObjects = new HashSet<>();

    @Getter
    private final Set<NPC> npcs = new HashSet<>();

    private MenuEntry getMenuEntry()
    {
        MenuEntry menuEntry = new MenuEntry();
        if(currentAction == null) return null;
        if(currentAction.lastClicked == null) return null;
        menuEntry.setIdentifier(currentAction.lastClicked.getId());
        menuEntry.setOption(currentAction.lastClicked.getMenuOption());
        menuEntry.setType(currentAction.lastClicked.getMenuAction().getId());
        menuEntry.setTarget(currentAction.lastClicked.getMenuTarget());
        menuEntry.setParam0(currentAction.lastClicked.getActionParam());
        menuEntry.setParam1(currentAction.lastClicked.getWidgetId());

        if(mode == Mode.FISHING)
        {
            NPC npc = npcs.stream()
                    .filter(e -> currentAction.lastClicked.getMenuTarget().contains(e.getName()))
                    .min(Comparator.comparingInt(a -> a.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()))).get();

            if(npc == null)
            {
                status = "Idle...";
                return null;
            }

            status = "Interacting with fishing spot...";
            menuEntry.setIdentifier(npc.getIndex());

            return menuEntry;
        }
        else
        {
            GameObject gameObject = gameObjects.stream()
                    .filter(e -> e.getId() == currentAction.lastClicked.getId())
                    .min(Comparator.comparingInt(a -> a.getLocalLocation().distanceTo(client.getLocalPlayer().getLocalLocation()))).get();

            if(gameObject == null)
            {
                status = "Idle...";
                return null;
            }

            int minX = gameObject.getSceneMinLocation().getX();
            int minY = gameObject.getSceneMinLocation().getY();
            int maxY = gameObject.getSceneMaxLocation().getY();
            int maxX = gameObject.getSceneMaxLocation().getX();

            int x = gameObject.getLocalLocation().getSceneX() == minX ? maxX : minX;
            int y = gameObject.getLocalLocation().getSceneY() == minY ? maxY : minY;

            menuEntry.setParam0(x);
            menuEntry.setParam1(y);
            status = "Interacting with " + currentAction.name + "...";

            return menuEntry;
        }
    }

    private String cleanName(String name)
    {
        int position = name.indexOf(">");
        if(position == -1) return name;

        return name.substring(position + 1);
    }

    private boolean isIdle()
    {
        return client.getLocalPlayer().getAnimation() == -1;
    }

    @Subscribe
    public void onGameTick(GameTick event)
    {
        new Thread(() -> execute()).start();
    }

    private void rebuildNpcs()
    {
        npcs.clear();
        client.getNpcs().forEach(e -> npcs.add(e));
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned)
    {
        npcs.remove(npcDespawned.getNpc());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned)
    {
        npcs.add(npcSpawned.getNpc());
    }

    private boolean executing;

    private void execute()
    {
        if(executing) return;

        executing = true;
        if(!isActive())
        {
            status = "";
            executing = false;
            return;
        }

        if(!isIdle())
        {
            status = currentAction.description + " " + currentAction.name;
            executing = false;
            return;
        }


        MenuEntry entry = getMenuEntry();
        if(entry == null)
        {
            executing = false;
            return;
        }
        
        sendAction(entry);

        boolean taskSuccess = waitUntil(() -> !isIdle(), 5000);

        executing = false;


    }

    private void rebuildAllGameObjects()
    {
        gameObjects.clear();
        Arrays.stream(client.getScene().getTiles())
                .flatMap(Arrays::stream)
                .flatMap(Arrays::stream)
                .filter(e -> e != null)
                .map(e -> e.getGameObjects())
                .flatMap(Arrays::stream)
                .forEach(e -> gameObjects.add(e));
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN ||
                event.getGameState() == GameState.HOPPING)
        {
            gameObjects.clear();
            npcs.clear();
        }
    }

    @Getter
    private Action currentAction;

    @Getter
    private boolean isActive = false;

    @Getter
    private Mode mode = Mode.FISHING;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private AutoGatherOverlay overlay;

    @Inject
    private AutoGatherSceneOverlay sceneOverlay;

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    private final HotkeyListener hotkeyListener = new HotkeyListener(() -> new Keybind(KeyEvent.VK_MINUS, 0))
    {
        @Override
        public void hotkeyPressed()
        {
            log.info("Action Key Pressed");
            new Thread(() -> execute()).start();
        }
    };

    @Override
    protected void startUp() throws Exception
    {
        overlayManager.add(overlay);
        overlayManager.add(sceneOverlay);
        keyManager.registerKeyListener(hotkeyListener);
        rebuildAllGameObjects();
        rebuildNpcs();
    }

    @Override
    protected void shutDown() throws Exception
    {
        overlayManager.remove(overlay);
        overlayManager.remove(sceneOverlay);
        keyManager.unregisterKeyListener(hotkeyListener);
    }

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
        if(sendingAction)
        {
            log.info("Clicked: " + click.toString());
            if(click.getId() == entry.getIdentifier()){
                sendingAction = false;
                entry = null;
            }
            return;
        }

        if(isActive)
        {
            isActive = false;
            return;
        }

        if(!client.isKeyPressed(KeyCode.KC_SHIFT))
            return;

        log.info(click.toString());

        for (Action action : actions) {
            if(action.isMatch(click))
            {
                isActive = true;
                mode = action.getMode();
                currentAction = action;
                click.consume();
                return;
            }
        }
    }

    private void click(int x, int y){
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
    public void onGameObjectSpawned(GameObjectSpawned event) {
        gameObjects.add(event.getGameObject());
    }

    @Subscribe
    public void onGameObjectChanged(GameObjectChanged event)
    {

    }

    @Subscribe
    public void onGameObjectDespawned(final GameObjectDespawned event)
    {
        if(gameObjects.contains(event.getGameObject())){
            gameObjects.remove(event.getGameObject());
        }
    }

    private boolean sendingAction = false;
    private MenuEntry entry = null;

    private void sendAction(MenuEntry entry)
    {
        sendingAction = true;
        this.entry = entry;
        click(50, 50);
        while(sendingAction)
        {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };
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

}
