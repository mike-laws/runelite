package net.runelite.client.plugins.nmz;

import net.runelite.client.plugins.threetickfish.ThreeTickFishPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class NmzOverlay extends OverlayPanel
{
    private NmzPlugin plugin;

    @Inject
    private NmzOverlay(NmzPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);

        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("NMZ")
                .build());

        int points = plugin.points;
        int minPoints = plugin.minAbsorption;
        int maxPoints = plugin.maxAbsorption;

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Points:" )
                .right(points + "")
                .rightColor(points > minPoints ? Color.GREEN : Color.RED)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Min Points:" )
                .right(minPoints + "")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Max Points:" )
                .right(maxPoints + "")
                .build());

        return super.render(graphics);
    }
}
