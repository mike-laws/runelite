package net.runelite.client.plugins.threetickfish;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class ThreeTickFishSceneOverlay extends Overlay
{
    @Getter
    private Point minimapPlayerPoint;

    private ThreeTickFishPlugin plugin;

    private Client client;
    @Inject
    private ThreeTickFishSceneOverlay(ThreeTickFishPlugin plugin, Client client)
    {
        this.plugin = plugin;
        this.client = client;

        setLayer(OverlayLayer.ABOVE_SCENE);
        setPosition(OverlayPosition.DYNAMIC);
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        graphics.setColor(Color.green);

        NPC fishingSpot = plugin.getFishingSpot();
        Shape convexHull = fishingSpot != null ? fishingSpot.getConvexHull() : null;

        if(convexHull != null) graphics.draw(convexHull);

        LocalPoint localPoint = client.getLocalPlayer().getLocalLocation();
        minimapPlayerPoint = Perspective.localToMinimap(client, localPoint);

        graphics.drawOval(minimapPlayerPoint.getX(), minimapPlayerPoint.getY(), 5, 5);
        return null;
    }
}
