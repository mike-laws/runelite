package net.runelite.client.plugins.recorder;

import lombok.Getter;
import net.runelite.client.input.MouseListener;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class RecorderMouseListener implements MouseListener
{
    @Getter
    private List<Action> actions = new ArrayList<>();

    @Getter
    private long startTime;

    @Override
    public MouseEvent mouseClicked(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mousePressed(MouseEvent mouseEvent) {
        if(actions.size() == 0) startTime = System.currentTimeMillis();

        long time = mouseEvent.getWhen() - startTime;
        actions.add(new Action(time, mouseEvent.getPoint(), false, false));
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseReleased(MouseEvent mouseEvent) {
        if(actions.size() == 0) startTime = System.currentTimeMillis();

        long time = mouseEvent.getWhen() - startTime;
        actions.add(new Action(time, mouseEvent.getPoint(), false, true));
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseEntered(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseExited(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseDragged(MouseEvent mouseEvent) {
        return mouseEvent;
    }

    @Override
    public MouseEvent mouseMoved(MouseEvent mouseEvent) {
        return mouseEvent;
    }
}
