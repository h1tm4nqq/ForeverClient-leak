package we.devs.forever.client.modules.impl.combat.offhandold;

import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemFood;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.client.modules.api.listener.ModuleListener;

public class ListenerMotionPre extends ModuleListener<Offhand, MotionEvent.Pre> {
    public ListenerMotionPre(Offhand module) {
        super(module, MotionEvent.Pre.class);
    }

    @Override
    public void invoke(MotionEvent.Pre event) {
        if(fullNullCheck()) return;
        if (module.timer.passedMs(50L)) {
            if (mc.player != null && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && Mouse.isButtonDown(1)) {
                mc.player.setActiveHand(EnumHand.OFF_HAND);
                mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
            }
            module.switchval = 0;
        } else if ((mc.player.getHeldItemMainhand().getItem() instanceof ItemFood
                || mc.player.getHeldItemMainhand().getItem() instanceof ItemBow
                || mc.player.getHeldItemMainhand().getItem() instanceof ItemEnderPearl
                || mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
            mc.gameSettings.keyBindUseItem.pressed = false;
        }
        module.countItems();
        if (module.delay0.passedMs( (long) (module.delay.getValue() * 25))) module.doSwitch();

        if (module.secondTimer.passedMs(50L) && module.second) {
            module.second = false;
            module. timer.reset();
        }
        if (module.thirdtimer.passedDms(1000.0)) {
            module.switchval = 0;
        }
    }
}
