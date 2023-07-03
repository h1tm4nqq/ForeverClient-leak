package we.devs.forever.client.ui.foreverClientGui.hud.component;


import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

import java.awt.*;
import java.util.Comparator;

public class TextRadar extends Hud {
    public TextRadar() {
        super("TextRadar");
    }

    @Override
    protected void onRenderHud() {
        final float[] l = {0};
        final float[] width = {0};
        mc.world.playerEntities.stream()
                .filter(entityPlayer -> entityPlayer != mc.player)
                .sorted(Comparator.comparingDouble(player -> player.getDistanceSq(mc.player)))
                .forEach(entityPlayer -> {
                    String color = getColor(entityPlayer);
                    String text = color + entityPlayer.getName() + TextUtil.RESET
                            + " [" + color + String.format("%.1f", entityPlayer.getDistance(mc.player)) + TextUtil.RESET + "]";
                    renderer.drawStringWithShadow(text, X.getValue(), Y.getValue() + l[0], Color.WHITE.getRGB());
                    l[0] += renderer.getHeight() + 0.2F;
                    width[0] = Math.max(width[0], renderer.getStringWidth(text));
                });
    this.width = width[0];
    this.height = l[0];
    }

    private String getColor(EntityPlayer entityPlayer) {
        if (friendManager.isFriend(entityPlayer)) {
            return TextUtil.AQUA;
        }
        if (entityPlayer.getDistanceSq(mc.player) <= 100) {
            return TextUtil.RED;
        } else if (entityPlayer.getDistanceSq(mc.player) <= 400) {
            return TextUtil.YELLOW;
        }
        return TextUtil.GREEN;
    }
}
