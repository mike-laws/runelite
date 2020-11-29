package net.runelite.client.plugins.recorder;

import lombok.Getter;

import java.awt.*;

public class Action
{
    @Getter
    private long eventTime;

    @Getter
    private Point position;

    @Getter
    private boolean isRightClick;

    @Getter
    private boolean isRelease;

    public Action(long eventTime, Point position, boolean isRightClick, boolean isRelease)
    {
        this.eventTime = eventTime;
        this.position = position;
        this.isRightClick = isRightClick;
        this.isRelease = isRelease;
    }
}
