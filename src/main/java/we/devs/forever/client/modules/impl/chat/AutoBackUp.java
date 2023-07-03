package we.devs.forever.client.modules.impl.chat;

import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.play.client.CPacketChatMessage;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.ArrayList;
import java.util.List;

public class AutoBackUp extends Module {
    public Setting<Lang> lang = new Setting<>("Lang", Lang.Russian);
    public Setting<Boolean> enemies = new Setting<>("Enemies", true, "Sends messages to your friends");
    public Setting<Float> range = new Setting<>("Range", 15F, 1F, 15F, v -> enemies.getValue());
    public Setting<Float> delay = new Setting<>("Delay", 10F, 0F, 30F);
    private final TimerUtil timerUtil = new TimerUtil();
    List<String> sents = new ArrayList<>();

    public AutoBackUp() {
        super("AutoBackUp", "Sends messages to your friends with your coords", Category.CHAT);
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            disable();
            return;
        }
        sents.clear();
        if (delay.getValue() == 0) {
            for (NetworkPlayerInfo player : mc.getConnection().getPlayerInfoMap()) {
                if (player.getGameProfile().getName().equals(mc.player.getName())) continue;
                if (!friendManager.isFriend(player.getGameProfile().getName())) continue;
                mc.player.connection.sendPacket(new CPacketChatMessage("/msg " + player.getGameProfile().getName() + " " + (lang.getValue() == Lang.Russian ? mesRu() : mesEn())));
                sents.add(player.getGameProfile().getName());
            }
            disable();
        }
    }

    @Override
    public void onTick() {
        for (NetworkPlayerInfo player : mc.getConnection().getPlayerInfoMap()) {
            if (!timerUtil.passedMs(delay.getValue() * 100F)) continue;
            if (sents.contains(player.getGameProfile().getName())) continue;
            if (player.getGameProfile().getName().equals(mc.player.getName())) continue;
            if (!friendManager.isFriend(player.getGameProfile().getName())) continue;
            mc.player.connection.sendPacket(new CPacketChatMessage("/msg " + player.getGameProfile().getName() + " " + (lang.getValue() == Lang.Russian ? mesRu() : mesEn())));
            sents.add(player.getGameProfile().getName());
            timerUtil.reset();
        }
        if (mc.getConnection().getPlayerInfoMap().stream()
                .filter(networkPlayerInfo -> !networkPlayerInfo.getGameProfile().getName().equals(mc.player.getName()))
                .filter(networkPlayerInfo -> friendManager.isFriend(networkPlayerInfo.getGameProfile().getName()))
                .filter(networkPlayerInfo -> !sents.contains(networkPlayerInfo.getGameProfile().getName()))
                .count() == 0) disable();
    }

    public String mesRu() {
        String defaultt = String.format("Нужен бекап, я на XYZ %s, %s, %s",
                (int) mc.player.posX,
                (int) mc.player.posY,
                (int) mc.player.posZ);

        if (enemies.getValue()) {
            StringBuilder stringBuilder = new StringBuilder(defaultt + " , враги рядом: ");
            mc.player.world.playerEntities.stream()
                    .filter(entityPlayer -> !entityPlayer.isDead)
                    .filter(entityPlayer -> !friendManager.isFriend(entityPlayer))
                    .filter(entityPlayer -> entityPlayer.getDistanceSq(mc.player) < range.getValue() * range.getValue())
                    .forEach(entityPlayer -> stringBuilder.append(entityPlayer.getName()).append(", "));
            return stringBuilder.toString();

        }


        return defaultt;
    }

    public String mesEn() {
        String defaultt = String.format("Need backup, I am at XYZ %s, %s, %s",
                (int) mc.player.posX,
                (int) mc.player.posY,
                (int) mc.player.posZ);

        if (enemies.getValue()) {
            StringBuilder stringBuilder = new StringBuilder(defaultt + " , Enemies near: ");
            mc.player.world.playerEntities.stream()
                    .filter(entityPlayer -> !entityPlayer.isDead)
                    .filter(entityPlayer -> !friendManager.isFriend(entityPlayer))
                    .filter(entityPlayer -> entityPlayer.getDistanceSq(mc.player) < range.getValue() * range.getValue())
                    .forEach(entityPlayer -> stringBuilder.append(entityPlayer.getName()).append(", "));
            return stringBuilder.toString();
        }
        return defaultt;
    }

    public enum Lang {
        English,
        Russian
    }
}
