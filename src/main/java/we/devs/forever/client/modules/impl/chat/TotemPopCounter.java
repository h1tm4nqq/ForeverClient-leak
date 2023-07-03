package we.devs.forever.client.modules.impl.chat;

import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.event.events.render.TotemPopEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.impl.player.TargetManager;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class TotemPopCounter extends Module {
    public Setting<Boolean> self = (new Setting<>("Self", true));

    public TotemPopCounter() {
        super("TotemPopCounter", "Counts the number of popped totems", Category.CHAT);
    }

    @EventListener
    public void onTotemPop(TotemPopEvent event) {
        if (!self.getValue() && event.getEntity() == mc.player) return;
        String color;
        if (event.getEntity() == mc.player) {
            color = TextUtil.GREEN;
        } else if (friendManager.isFriend(event.getEntity().getName())) {
            color = TextUtil.AQUA;
        } else {
            color = TextUtil.RED;
        }
        Command.sendMessagepref(color + event.getEntity().getName() + " " + TextUtil.WHITE + "popped " + TextUtil.GREEN + event.getPops() + TextUtil.WHITE + " totem" + (event.getPops() == 1 ? "" : "s"), String.valueOf(event.getEntity().getEntityId()));

//        if (notify.getValue()) {
//            doNotification(event.getPlayer().getName() + " popped " + event.getPops() + " totem" + (event.getPops() > 1 ? "s" : "") + "!", TrayIcon.MessageType.NONE);
//        }
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) {
            return;
        }
        for (EntityPlayer player : mc.world.playerEntities) {
            if ((!self.getValue() && player == mc.player) || player.getHealth() > 0 || !TargetManager.targetPopsList.containsKey(player.getName()))
                continue;
            String color;
            if (player == mc.player) {
                color = TextUtil.GREEN;
            } else if (friendManager.isFriend(player.getName())) {
                color = TextUtil.AQUA;
            } else {
                color = TextUtil.RED;
            }
            Command.sendMessagepref(color + player.getName() + " " + TextUtil.GREEN + TextUtil.WHITE + "died after popping " + TextUtil.GREEN + TargetManager.targetPopsList.get(player.getName()) + TextUtil.WHITE + " totem" + (TargetManager.targetPopsList.get(player.getName()) == 1 ? "" : "s"), String.valueOf(player.getEntityId()));
//            if (notify.getValue()) {
//                doNotification(player.getName() + " died after popping " + TargetManager.popList.get(player.getName()) + " totems!", TrayIcon.MessageType.INFO);
//            }
        }
    }


}