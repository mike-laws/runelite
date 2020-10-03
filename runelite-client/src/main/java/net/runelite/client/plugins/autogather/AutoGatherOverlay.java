package net.runelite.client.plugins.autogather;

import com.google.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import java.awt.*;

public class AutoGatherOverlay extends OverlayPanel
{
    private final AutoGatherPlugin plugin;

    @Inject
    private AutoGatherOverlay(AutoGatherPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);

        this.plugin = plugin;
    }


    @Override
    public Dimension render(Graphics2D graphics)
    {
        if(!plugin.isActive()) return null;


        panelComponent.getChildren().add(TitleComponent.builder()
                .text(plugin.getMode().getName())
                .color(Color.GREEN)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:" )
                .right(plugin.getStatus())
                .build());


        return super.render(graphics);
    }
}
