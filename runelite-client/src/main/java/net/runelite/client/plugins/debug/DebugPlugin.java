package net.runelite.client.plugins.debug;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.input.KeyListener;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.MouseListener;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import javax.inject.Inject;
import java.awt.event.MouseEvent;

@PluginDescriptor(
        name = "Debug Info",
        description = "DebuggingInfo",
        tags = {"Debug"}
)
@Slf4j
public class DebugPlugin extends Plugin
{
    @Inject
    private MouseManager mouseManager;
    @Inject
    private KeyManager keyManager;

    private MouseListener mouseListener = new MouseListener() {
        @Override
        public MouseEvent mouseClicked(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }

        @Override
        public MouseEvent mousePressed(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }

        @Override
        public MouseEvent mouseReleased(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }

        @Override
        public MouseEvent mouseEntered(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }

        @Override
        public MouseEvent mouseExited(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }

        @Override
        public MouseEvent mouseDragged(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }

        @Override
        public MouseEvent mouseMoved(MouseEvent mouseEvent) {
            log.info(mouseEvent.toString());
            return mouseEvent;
        }
    };

    @Override
    protected void startUp()
    {
        //mouseManager.registerMouseListener(mouseListener);
    }

    @Override
    protected void shutDown()
    {
        //mouseManager.unregisterMouseListener(mouseListener);
    }

    @Subscribe
    public void onWidgetLoaded(WidgetLoaded widgetLoaded)
    {
        log.info(widgetLoaded.toString());
    }

    @Subscribe
    public void onNpcChanged(NpcChanged npcChanged)
    {
        log.info("NPC Changed: " + npcChanged.getNpc().getName());
    }

    @Subscribe
    public void onNpcActionChanged(NpcActionChanged event)
    {
        log.info("NPC Action Changed: " + event.getNpcComposition().getName());
    }

    @Subscribe
    public void onNpcSpawned(NpcSpawned npcSpawned)
    {
        log.info("NPC Spawned: " + npcSpawned.getNpc().getName());
    }

    @Subscribe
    public void onNpcDespawned(NpcDespawned npcDespawned)
    {
        log.info("NPC Despawned: " + npcDespawned.getNpc().getName());
    }


}
