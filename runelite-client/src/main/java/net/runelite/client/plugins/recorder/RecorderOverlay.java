package net.runelite.client.plugins.recorder;

import com.google.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class RecorderOverlay extends OverlayPanel
{
    private final RecorderPlugin plugin;

    @Inject
    private RecorderOverlay(RecorderPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);

        this.plugin = plugin;
    }


    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Recorder")
                .color(Color.WHITE)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Record:" )
                .right("[Ctrl + R]")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Start/Stop:" )
                .right("[Ctrl + P]")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:" )
                .right(plugin.getStatus().getDescription())
                .rightColor(plugin.getStatus().getColor())
                .build());

        return super.render(graphics);
    }
}
