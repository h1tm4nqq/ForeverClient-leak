package we.devs.forever.client.modules.impl.combat.offhandold;

import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import we.devs.forever.api.event.events.player.ProcessRightClickBlockEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerProcessRightClickBlockEvent extends ModuleListener<Offhand, ProcessRightClickBlockEvent> {
    public ListenerProcessRightClickBlockEvent(Offhand module) {
        super(module, ProcessRightClickBlockEvent.class);
    }

    @Override
    public void invoke(ProcessRightClickBlockEvent event) {
        if (event.hand == EnumHand.MAIN_HAND && event.stack.getItem() == Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.objectMouseOver != null && event.pos == mc.objectMouseOver.getBlockPos()) {
            event.cancel();
            mc.player.setActiveHand(EnumHand.OFF_HAND);
            mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
        }
    }
}
