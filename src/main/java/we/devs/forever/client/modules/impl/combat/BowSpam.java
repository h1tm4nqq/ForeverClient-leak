package we.devs.forever.client.modules.impl.combat;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.setting.Setting;

public class BowSpam
        extends Module {
    private final TimerUtil timer = new TimerUtil();
    public Setting<Mode> mode = (new Setting<>("Mode", Mode.Fast));
    public Setting<Boolean> bowbomb = (new Setting<>("BowBomb", false, v -> this.mode.getValue() != Mode.BowBomb));
    public Setting<Boolean> allowOffhand = (new Setting<>("Offhand", true, v -> this.mode.getValue() != Mode.AutoRelease));
    public Setting<Integer> ticks = (new Setting<>("Ticks", 3, 0, 20, "Speed", v -> this.mode.getValue() == Mode.BowBomb || this.mode.getValue() == Mode.Fast));
    public Setting<Integer> delay = (new Setting<>("Delay", 50, 0, 500, "Speed", v -> this.mode.getValue() == Mode.AutoRelease));
    public Setting<Boolean> tpsSync = (new Setting<>("TpsSync", true));
    public Setting<Boolean> autoSwitch = (new Setting<>("AutoSwitch", false));
    public Setting<Boolean> onlyWhenSave = (new Setting<>("OnlyWhenSave", true, v -> this.autoSwitch.getValue()));
    public Setting<Target> targetMode = (new Setting<>("Target", Target.Lowest, v -> this.autoSwitch.getValue()));
    public Setting<Float> range = (new Setting<>("Range", 3.0f, 0.0f, 6.0f, "Range of the target", v -> this.autoSwitch.getValue()));
    public Setting<Float> health = (new Setting<>("Lethal", 6.0f, 0.1f, 36.0f, "When should it switch?", v -> this.autoSwitch.getValue()));
    public Setting<Float> ownHealth = (new Setting<>("OwnHealth", 20.0f, 0.1f, 36.0f, "Own Health.", v -> this.autoSwitch.getValue()));
    private boolean offhand;
    private boolean switched;
    private int lastHotbarSlot = -1;

    public BowSpam() {
        super("BowSpam", "Spams your bow.", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        this.lastHotbarSlot = BowSpam.mc.player.inventory.currentItem;
    }

    @EventListener
    public void onUpdateWalkingPlayerPre(MotionEvent.Pre event) {
        if (this.autoSwitch.getValue() && InventoryUtil.findHotbarBlock(ItemBow.class) != -1 && this.ownHealth.getValue() <= EntityUtil.getHealth(BowSpam.mc.player) && (!this.onlyWhenSave.getValue() || EntityUtil.isSafe(BowSpam.mc.player))) {
            EntityPlayer target = this.getTarget();
            if (!(target == null || moduleManager.getModuleByClass(AutoCrystal.class).isEnabled() && InventoryUtil.holdingItem(ItemEndCrystal.class))) {
                Vec3d pos = target.getPositionVector();
                if (BowSpam.mc.player.canEntityBeSeen(target)) {
                } else if (EntityUtil.canEntityFeetBeSeen(target)) {
                } else {
                    return;
                }
                if (!(BowSpam.mc.player.getHeldItemMainhand().getItem() instanceof ItemBow)) {
                    this.lastHotbarSlot = BowSpam.mc.player.inventory.currentItem;
                    InventoryUtil.switchToHotbarSlot(ItemBow.class, false);
                    BowSpam.mc.gameSettings.keyBindUseItem.pressed = true;
                    this.switched = true;
                }
                rotationManager.doRotation(RotationType.Normal, pos, 9);
                if (BowSpam.mc.player.getHeldItemMainhand().getItem() instanceof ItemBow) {
                    this.switched = true;
                }
            }
        } else if (event.getStage() == 0 && this.switched && this.lastHotbarSlot != -1) {
            InventoryUtil.switchToHotbarSlot(this.lastHotbarSlot, false);
            BowSpam.mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
            this.switched = false;
        } else {
            BowSpam.mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
        }
        if (this.mode.getValue() == Mode.Fast && (this.offhand || BowSpam.mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) && BowSpam.mc.player.isHandActive()) {
            float f3;
            float f = BowSpam.mc.player.getItemInUseMaxCount();
            float f2 = this.ticks.getValue();
            float f4 = f3 = this.tpsSync.getValue() ? serverManager.getTpsFactor() : 1.0f;
            if (f >= f2 * f3) {
                BowSpam.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, BowSpam.mc.player.getHorizontalFacing()));
                BowSpam.mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(this.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                BowSpam.mc.player.stopActiveHand();
            }
        }
    }

    @Override
    public void onUpdate() {
        this.offhand = BowSpam.mc.player.getHeldItemOffhand().getItem() == Items.BOW && this.allowOffhand.getValue();
        switch (this.mode.getValue()) {
            case AutoRelease: {
                if (!this.offhand && !(BowSpam.mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) || !this.timer.passedMs((int) ((float) this.delay.getValue() * (this.tpsSync.getValue() ? serverManager.getTpsFactor() : 1.0f))))
                    break;
                BowSpam.mc.playerController.onStoppedUsingItem(BowSpam.mc.player);
                this.timer.reset();
                break;
            }
            case BowBomb: {
                float f3;
                if (!this.offhand && !(BowSpam.mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) || !BowSpam.mc.player.isHandActive())
                    break;
                float f = BowSpam.mc.player.getItemInUseMaxCount();
                float f2 = this.ticks.getValue();
                float f4 = f3 = this.tpsSync.getValue() ? serverManager.getTpsFactor() : 1.0f;
                if (!(f >= f2 * f3)) break;
                BowSpam.mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, BowSpam.mc.player.getHorizontalFacing()));
                BowSpam.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(BowSpam.mc.player.posX, BowSpam.mc.player.posY - 0.0624, BowSpam.mc.player.posZ, BowSpam.mc.player.rotationYaw, BowSpam.mc.player.rotationPitch, false));
                BowSpam.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(BowSpam.mc.player.posX, BowSpam.mc.player.posY - 999.0, BowSpam.mc.player.posZ, BowSpam.mc.player.rotationYaw, BowSpam.mc.player.rotationPitch, true));
                BowSpam.mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(this.offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND));
                BowSpam.mc.player.stopActiveHand();
            }
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getStage() == 0 && this.bowbomb.getValue() && this.mode.getValue() != Mode.BowBomb && event.getPacket() instanceof CPacketPlayerDigging && ((CPacketPlayerDigging) event.getPacket()).getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && (this.offhand || BowSpam.mc.player.inventory.getCurrentItem().getItem() instanceof ItemBow) && BowSpam.mc.player.getItemInUseMaxCount() >= 20 && !BowSpam.mc.player.onGround) {
            BowSpam.mc.player.connection.sendPacket(new CPacketPlayer.Position(BowSpam.mc.player.posX, BowSpam.mc.player.posY - (double) 0.1f, BowSpam.mc.player.posZ, false));
            BowSpam.mc.player.connection.sendPacket(new CPacketPlayer.Position(BowSpam.mc.player.posX, BowSpam.mc.player.posY - 10000.0, BowSpam.mc.player.posZ, true));
        }
    }

    private EntityPlayer getTarget() {
        double maxHealth = 36.0;
        EntityPlayer target = null;
        for (EntityPlayer player : BowSpam.mc.world.playerEntities) {
            if (player == null || EntityUtil.isDead(player) || EntityUtil.getHealth(player) > this.health.getValue() || player.equals(BowSpam.mc.player) || friendManager.isFriend(player) || BowSpam.mc.player.getDistanceSq(player) > MathUtil.square(this.range.getValue()) || !BowSpam.mc.player.canEntityBeSeen(player) && !EntityUtil.canEntityFeetBeSeen(player))
                continue;
            if (target == null) {
                target = player;
                maxHealth = EntityUtil.getHealth(player);
            }
            if (this.targetMode.getValue() == Target.Closest && BowSpam.mc.player.getDistanceSq(player) < BowSpam.mc.player.getDistanceSq(target)) {
                target = player;
                maxHealth = EntityUtil.getHealth(player);
            }
            if (this.targetMode.getValue() != Target.Lowest || !((double) EntityUtil.getHealth(player) < maxHealth))
                continue;
            target = player;
            maxHealth = EntityUtil.getHealth(player);
        }
        return target;
    }

    public enum Target {
        Closest,
        Lowest

    }

    public enum Mode {
        Fast,
        AutoRelease,
        BowBomb

    }
}

