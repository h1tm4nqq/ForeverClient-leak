package we.devs.forever.client.modules.impl.player;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEndCrystal;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemMinecart;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class FastPlace
        extends Module {
    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Custom));
    private final Setting<Boolean> obby = (new Setting<>("All", false, v -> this.mode.getValue() == Mode.Custom));
    private final Setting<Boolean> crystals = (new Setting<>("Crystals", false, v -> this.mode.getValue() == Mode.Custom));
    private final Setting<Boolean> exp = (new Setting<>("Experience", false, v -> this.mode.getValue() == Mode.Custom));
    private final Setting<Boolean> minecart = (new Setting<>("Minecarts", false, v -> this.mode.getValue() == Mode.Custom));
    private final Setting<Boolean> packetCrystal = (new Setting<>("PacketCrystal", false, v -> this.mode.getValue() == Mode.Custom));
    private final Setting<Boolean> ghostFix = (new Setting<>("GhostFix", false));
    private final Setting<Boolean> strict = (new Setting<>("Strict", false));
    private BlockPos mousePos = null;


    public FastPlace() {
        super("FastUse", "Allows you to use items faster", Category.PLAYER);
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (strict.getValue() && mc.player.ticksExisted % 2 == 0) return;
        if (this.mode.getValue() == Mode.All) {
            mc.rightClickDelayTimer = 0;
        }
        if (this.mode.getValue() == Mode.Custom) {
            if (InventoryUtil.holdingItem(ItemExpBottle.class) && this.exp.getValue()) {
                mc.rightClickDelayTimer = 0;
            }
            if (this.obby.getValue()) {
                mc.rightClickDelayTimer = 0;
            }
            if (InventoryUtil.holdingItem(ItemMinecart.class) && this.minecart.getValue()) {
                mc.rightClickDelayTimer = 0;
            }
            if (this.packetCrystal.getValue() && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                boolean offhand;
                boolean bl = offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
                if (offhand || mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
                    RayTraceResult result = mc.objectMouseOver;
                    if (result == null) {
                        return;
                    }
                    switch (result.typeOfHit) {
                        case MISS: {
                            this.mousePos = null;
                            break;
                        }
                        case BLOCK: {
                            this.mousePos = mc.objectMouseOver.getBlockPos();
                            break;
                        }
                        case ENTITY: {
                            Entity entity;
                            if (this.mousePos == null || (entity = result.entityHit) == null || !this.mousePos.equals(new BlockPos(entity.posX, entity.posY - 1.0, entity.posZ)))
                                break;
                            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.mousePos, EnumFacing.DOWN, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0f, 0.0f, 0.0f));
                        }
                    }
                }
            }
        }
        if (InventoryUtil.holdingItem(ItemEndCrystal.class) && (this.crystals.getValue() || this.mode.getValue() == Mode.All)) {
            mc.rightClickDelayTimer = 0;
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null || mc.world == null) return;
        if (ghostFix.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemExpBottle) {
            if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
                event.cancel();
            }
        }
    }

    public enum Mode {
        Custom,
        All
    }
}
