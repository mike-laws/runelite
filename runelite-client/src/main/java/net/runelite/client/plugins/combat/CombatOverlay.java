package net.runelite.client.plugins.combat;

import net.runelite.client.plugins.threetickfish.ThreeTickFishPlugin;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TextComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class CombatOverlay extends OverlayPanel
{
    private CombatPlugin plugin;

    @Inject
    private CombatOverlay(CombatPlugin plugin)
    {
        super(plugin);
        setPosition(OverlayPosition.TOP_LEFT);

        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Auto Combat")
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Combat [Crl + =]:" )
                .right(plugin.isCombatRunning ? "On" : "Off")
                .rightColor(plugin.isCombatRunning ? Color.GREEN : Color.RED)
                .build());

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Looting: [Ctrl + -]" )
                .right(plugin.isLootingRunning ? "On" : "Off")
                .rightColor(plugin.isLootingRunning ? Color.GREEN : Color.RED)
                .build());



        panelComponent.getChildren().add(LineComponent.builder()
                .left("Status:" )
                .build());


        panelComponent.getChildren().add(LineComponent.builder()
                .left(plugin.status)
                .build());

        return super.render(graphics);
    }
}
