package we.devs.forever.client.modules.impl.misc;

import we.devs.forever.api.manager.impl.render.WayPoint;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.components.items.buttons.ColorPickerButton;

import java.awt.*;
import java.util.Objects;

public class WayPoints extends Module {
    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Distance));
    public Setting<Color> color = (new Setting<>("TextColor", Color.WHITE, ColorPickerButton.Mode.Normal, 100));
    public Setting<Float> scale = (new Setting<>("Scale", 1F, 0.1F, 10F));

    public WayPoints() {
        super("WayPoints", "Save in render position", Category.MISC);
    }

    @Override
    public void onAltRender3D(float partialTicks) {
        wayPointManager.wayPoints.stream()
                .filter(wayPoint -> {
                    String server;
                    if (mc.currentServerData != null) {
                        server = mc.currentServerData.serverIP;
                    } else {
                        server = "offline";
                    }
                    return Objects.equals(wayPoint.getServer(), server);
                })
                .forEach(wayPoint -> {
                    String name = "";
                    WayPoint.WorldType worldType = wayPoint.getWorldType();
                    boolean inHell = mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell");
                    float nether = inHell && worldType != WayPoint.WorldType.HELL ? 0.125f : 1;
                    switch (mode.getValue()) {
                        case Name: {
                            name = wayPoint.getName();
                            break;
                        }
                        case Coords: {
                            String[] string = wayPoint.getCords().split(":");
                            name = wayPoint.getName() + " XYZ: "
                                    + String.format("%.1f", Float.parseFloat(string[0]) * nether) + ", "
                                    + String.format("%.1f", Float.parseFloat(string[1])) + ", "
                                    + String.format("%.1f", Float.parseFloat(string[2]) * nether);
                            break;
                        }
                        case Distance: {
                            name = wayPoint.getName() + " " + String.format("%.1f", Math.sqrt(mc.player.getDistanceSq(wayPoint.getPos()))* nether) + "m";
                            break;
                        }

                    }
                    RenderUtil.drawText(wayPoint.getPos(), name, scale.getValue(), color.getColor());
                });
    }

    public enum Mode {
        Distance,
        Coords,
        Name
    }
}
