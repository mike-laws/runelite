package net.runelite.client.plugins.threetickfish;

import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class ThreeTickFishOverlay extends OverlayPanel
{
    @Inject
    private ThreeTickFishPlugin plugin;

    @Inject
    private ThreeTickFishOverlay(ThreeTickFishPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);

        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Three Tick Fishing")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:" )
                .right(plugin.isRunning() ? "On" : "Off")
                .rightColor(plugin.isRunning() ? Color.GREEN : Color.RED)
                .build());

        return super.render(graphics);
    }

}
