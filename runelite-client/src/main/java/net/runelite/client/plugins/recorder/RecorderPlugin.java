package net.runelite.client.plugins.recorder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.Keybind;
import net.runelite.client.input.KeyManager;
import net.runelite.client.input.Mouse;
import net.runelite.client.input.MouseManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.HotkeyListener;

import javax.inject.Inject;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.List;

@PluginDescriptor(
        name = "Recorder",
        description = "Records Things",
        tags = {"Record"}
)
@Slf4j
public class RecorderPlugin extends Plugin
{
    public enum Status
    {
        EMPTY("Empty", Color.GRAY),
        READY("Ready", Color.ORANGE),
        RECORDING("Recording", Color.RED),
        PLAYING("Playing", Color.GREEN);

        @Getter
        private String description;

        @Getter
        private Color color;

        Status(String description, Color color)
        {
            this.description = description;
            this.color = color;
        }
    }

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private RecorderOverlay overlay;

    @Inject
    private Client client;

    @Inject
    private KeyManager keyManager;

    @Inject
    private MouseManager mouseManager;

    @Getter
    private Status status = Status.EMPTY;

    private RecorderMouseListener mouseListener;

    private final HotkeyListener recordListener = new HotkeyListener(() -> new Keybind(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK))
    {
        @Override
        public void hotkeyPressed()
        {
            startStopRecording();
        }
    };

    private final HotkeyListener startStopListener = new HotkeyListener(() -> new Keybind(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK))
    {
        @Override
        public void hotkeyPressed()
        {
            startStop();
        }
    };

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        overlayManager.add(overlay);
        keyManager.registerKeyListener(recordListener);
        keyManager.registerKeyListener(startStopListener);
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        overlayManager.remove(overlay);
        keyManager.unregisterKeyListener(recordListener);
        keyManager.unregisterKeyListener(startStopListener);
    }

    private void startStop()
    {
        if(status == Status.PLAYING)
        {
            stop();
            return;
        }

        if(status == Status.READY)
        {
            new Thread(() -> start()).start();
            return;
        }
    }

    private List<Action> actions;
    private long stopTime;

    private void start()
    {
        status = Status.PLAYING;
        long startTime = System.currentTimeMillis() + 50;
        for (Action action : actions)
        {
            if(status != Status.PLAYING) return;
            execute(startTime, action);
        }

        while(System.currentTimeMillis() < stopTime + startTime)
        {
            if(status != Status.PLAYING) return;
            sleep(10);
        }
        
        start();
    }

    private void execute(long startTime, Action action)
    {
        long executeTime = startTime + action.getEventTime() - 50;
        while(System.currentTimeMillis() < executeTime)
        {
            if(status != Status.PLAYING) return;
            sleep(10);
        }

        Mouse.move(client, action.getPosition());
        sleep(50);

        if(action.isRelease())
            Mouse.release(client, action.getPosition(), !action.isRightClick());
        else
            Mouse.click(client, action.getPosition(), !action.isRightClick());
    }

    private void sleep(long duration)
    {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void stop()
    {
        status = Status.READY;
    }

    private void startStopRecording()
    {
        if(status == Status.PLAYING) return;

        if(status == Status.RECORDING)
        {
            stopRecording();
            return;
        }

        startRecording();
    }

    private void startRecording()
    {
        status = Status.RECORDING;
        mouseListener = new RecorderMouseListener();
        mouseManager.registerMouseListener(mouseListener);
    }

    private void stopRecording()
    {
        actions = mouseListener.getActions();
        mouseManager.unregisterMouseListener(mouseListener);

        if(actions.size() > 0)
        {
            stopTime = System.currentTimeMillis() - mouseListener.getStartTime();
            status = Status.READY;
        } else {
            status = Status.EMPTY;
            log.info("No actions recorded.");
        }
    }
}
