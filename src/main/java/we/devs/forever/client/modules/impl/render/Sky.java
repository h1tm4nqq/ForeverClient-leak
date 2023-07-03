package we.devs.forever.client.modules.impl.render;

import we.devs.forever.api.event.events.render.FogEvent;
import we.devs.forever.api.event.events.render.GetWorldTimeEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;

public class Sky
        extends Module {
    //private final Setting<Integer> red = (new Setting<>("Red", 255, 0, 255));
    //private final Setting<Integer> green = (new Setting<>("Green", 255, 0, 255));
    //private final Setting<Integer> blue = (new Setting<>("Blue", 255, 0, 255));
    public static Setting<Boolean> ambience = (new Setting<>("Ambience", false));
    public static Setting<Color> color = (new Setting<>("AmbienceColor", new Color(30, 30, 30, 30), ColorPickerButton.Mode.Normal, 100, v -> ambience.getValue()));
    private  final Setting<Boolean> customSky = new Setting<>("CustomSky", true);
    private  final Setting<Color> color1 = (new Setting<>("Color", new Color(100, 100, 100), ColorPickerButton.Mode.Normal, 100, v -> customSky.getValue()));
    public Setting<TimeMode> timeChange =new Setting<>("TimeMode", TimeMode.NONE);
    public Setting<Integer> time =new Setting<>("Time", 0,0, 23000, v -> this.timeChange.getValue() != TimeMode.NONE);
    public enum TimeMode {
        NONE, STATIC, IRL
    }

    public Sky() {
        super("Sky", "Change the sky color.", Category.RENDER);
    }

    @EventListener
    public void onGetWorldTime(GetWorldTimeEvent event) {
        if (timeChange.getValue() != TimeMode.NONE) {
            if (timeChange.getValue() == TimeMode.STATIC) {
                event.setWorldTime(time.getValue());
            } else {
                ZonedDateTime nowZoned = ZonedDateTime.now();
                Instant midnight = nowZoned.toLocalDate().atStartOfDay(nowZoned.getZone()).toInstant();
                Duration duration = Duration.between(midnight, Instant.now());
                long seconds = duration.getSeconds();
                event.setWorldTime((int) (seconds / 86400F));
            }
            event.cancel();
        }
    }

    @EventListener
    public void onFogDensity(FogEvent.Density event) {
        if (customSky.getValue()) {
            event.setDensity(0);
            event.setCanceled(true);
        }
    }

    @EventListener
    public void onFogColor(FogEvent.Color event) {
        if (customSky.getValue()) {
            event.setR(color1.getColor().getRed());
            event.setG(color1.getColor().getGreen());
            event.setB(color1.getColor().getBlue());
        }
    }

}

