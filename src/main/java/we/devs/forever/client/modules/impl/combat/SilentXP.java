package we.devs.forever.client.modules.impl.combat;

import net.minecraft.init.Items;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import we.devs.forever.api.event.events.client.KeyEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;

public class SilentXP extends Module {
    public static boolean onActivate;
    public static SilentXP INSTANCE;
    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Press));
    //public Setting<Boolean> antiFriend = (new Setting<>("AntiFriend", true));
    public Setting<Bind> key = (new Setting<>("Key", new Bind(-1)));
    //  public Setting<Boolean> groundOnly = (new Setting<>("BelowHorizon", false));
    public Setting<Boolean> rotate = (new Setting<>("XPDown", false, "When you use SilentXp camera will rotate down"));
    private final Setting<Integer> delay = (new Setting<>("Delay", 1, 1, 1000, "Speed of xp bottles"));
    private final Setting<Boolean> ghostFix = (new Setting<>("GhostFix", false, "Fixes ghost xp bottles"));
    public Setting<AutoSwitch> silentSwitch = (new Setting<>("SilentSwitch", AutoSwitch.Silent, "Allows use xp bottles with silent switch"));

    TimerUtil timerUtil = new TimerUtil();

    public SilentXP() {

        super("SilentXP", "Silent XP.", Category.COMBAT);
        INSTANCE = this;

    }

    public static   boolean toggled;

    @EventListener
    public void onKey(KeyEvent event) {
        if (event.getKey() == key.getValue().getKey())
            switch (mode.getValue()) {
                case Press: {
                    toggled = event.iskeyDown();
                    break;
                }
                case Toggle: {
                    if (event.iskeyDown()) {
                        toggled = !toggled;
                    }
                    break;
                }
            }


    }

    @EventListener
    public void onUpdateWalkingPlayerPre(MotionEvent.Pre event) {
        if (!fullNullCheck()) {
            if(mc.currentScreen != null) toggled =false;
            if (toggled) throwXP(event);
        }

    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (ghostFix.getValue() && onActivate && mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                event.cancel();
            }
        }
    }


    private void throwXP(MotionEvent.Pre event) {
        if (timerUtil.passedMs(delay.getValue())) {
            timerUtil.reset();
            int oldslot = mc.player.inventory.currentItem;
            int xpSlot = InventoryUtil.findHotbarBlock(ItemExpBottle.class);
            boolean offhand;

//            RayTraceResult result;
//            if (mcf && antiFriend.getValue() && (result = mc.objectMouseOver) != null && result.typeOfHit == RayTraceResult.Type.ENTITY && result.entityHit instanceof EntityPlayer) {
//                return;
//            }
            if (rotate.getValue()) {
                event.setRotations(mc.player.rotationYaw, 90F);
            }
            offhand = mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE;
            if (xpSlot != -1 || offhand) {

                if (!offhand) {
                    InventoryUtil.switchSilent(xpSlot, xpSlot, oldslot, silentSwitch.getValue());
                }
                onActivate = true;
                mc.playerController.processRightClick(mc.player, mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                if (!offhand) {
                    InventoryUtil.switchSilent(oldslot, xpSlot, oldslot, silentSwitch.getValue());
                }
            }
        }

    }

    public enum Mode {
        Toggle,
        Press
    }
}