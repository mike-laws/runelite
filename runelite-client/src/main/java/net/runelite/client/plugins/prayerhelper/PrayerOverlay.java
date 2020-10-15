package net.runelite.client.plugins.prayerhelper;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

import javax.inject.Inject;
import java.awt.*;

public class PrayerOverlay extends Overlay {

    private final PanelComponent panelComponent = new PanelComponent();

    private final PrayerHelperPlugin plugin;

    @Inject
    public PrayerOverlay(PrayerHelperPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        panelComponent.getChildren().clear();

        panelComponent.getChildren().add(TitleComponent.builder()
                .text("Prayer Helper")
                .color(Color.WHITE)
                .build());

        Color prayerColor = Color.RED;
        String prayerMessage = "Off";

        if(plugin.shouldPray)
        {
            prayerColor = Color.GREEN;
            prayerMessage = "On";
        }

        panelComponent.getChildren().add(LineComponent.builder()
                .left("Prayer [=]:")
                .right(prayerMessage)
                .rightColor(prayerColor)
                .build());

        return panelComponent.render(graphics);
    }
}
