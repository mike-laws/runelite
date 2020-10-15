package net.runelite.client.plugins.prayerhelper;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("prayerhelper")
public interface PrayerHelperConfig extends Config {

    @ConfigItem(
            position = 1,
            keyName = "useOverloads",
            name = "Use Overloads",
            description = "Use overloads if in NMZ"
    )
    default boolean getUseOverloads()
    {
        return true;
    }
}
