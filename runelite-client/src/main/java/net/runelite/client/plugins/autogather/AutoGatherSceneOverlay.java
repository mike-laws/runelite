package net.runelite.client.plugins.autogather;

import net.runelite.api.GameObject;
import net.runelite.api.NPC;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class AutoGatherSceneOverlay extends Overlay {

    private AutoGatherPlugin plugin;

    @Inject
    private AutoGatherSceneOverlay(AutoGatherPlugin plugin)
    {
        this.plugin = plugin;
        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(!plugin.isActive()) return null;
        graphics.setColor(Color.cyan);

        for (GameObject gameObject:plugin.getGameObjects())
        {
            if(plugin.getCurrentAction().isMatch(gameObject))
            {
                if(gameObject == null) continue;
                Shape convexHull = gameObject.getConvexHull();
                if(convexHull == null) continue;
                graphics.draw(convexHull);
            }
        }

        for (NPC npc:plugin.getNpcs())
        {
            if(plugin.getCurrentAction().isMatch(npc))
            {
                if(npc == null) continue;
                Shape convexHull = npc.getConvexHull();
                if(convexHull == null) continue;
                graphics.draw(convexHull);
            }
        }

        return null;
    }
}
