package net.runelite.client.input;

import net.runelite.api.Client;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;

public class Mouse
{
    public static void move(Client client, Point position)
    {
        MouseEvent mouseEvent = new MouseEvent(
                client.getCanvas(),
                MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(),
                0,
                position.x,
                position.y,
                0,
                false);

        client.getCanvas().dispatchEvent(mouseEvent);
    }

    public static void click(Client client, Point position, boolean left)
    {
        MouseEvent mouseEvent = new MouseEvent(client.getCanvas(), // which
                MouseEvent.MOUSE_PRESSED,
                System.currentTimeMillis(),
                left ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON2_DOWN_MASK,
                position.x,
                position.y,
                1,
                false);

        client.getCanvas().dispatchEvent(mouseEvent);
    }

    public static void release(Client client, Point position, boolean left)
    {
        MouseEvent mouseEvent = new MouseEvent(client.getCanvas(), // which
                MouseEvent.MOUSE_RELEASED,
                System.currentTimeMillis(),
                left ? InputEvent.BUTTON1_DOWN_MASK : InputEvent.BUTTON2_DOWN_MASK,
                position.x,
                position.y,
                1,
                false);

        client.getCanvas().dispatchEvent(mouseEvent);
    }

}
