package we.devs.forever.client.modules.impl.player;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.tileentity.*;
import net.minecraft.util.EnumHand;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.Objects;

public class NoInteract extends Module {
    private final Setting<Boolean> pickaxe = (new Setting<>("Pickaxe", false));

    public NoInteract() {
        super("NoInteract", "Prevents u from interacting with blocks", Category.PLAYER);
    }

    @EventListener
    public void onPacketSend(final PacketEvent.Send e) {
        if (fullNullCheck()) {
            return;
        }
        if (e.getPacket() instanceof CPacketPlayerTryUseItemOnBlock
                && !mc.player.isSneaking()
                && mc.gameSettings.keyBindUseItem.isKeyDown()
                && (InventoryUtil.heldItem(Items.EXPERIENCE_BOTTLE, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.GOLDEN_APPLE, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.CHORUS_FRUIT, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.BOW, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.WRITABLE_BOOK, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.WRITTEN_BOOK, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.POTIONITEM, InventoryUtil.Hand.Both)
                || (this.pickaxe.getValue()
                && InventoryUtil.heldItem(Items.DIAMOND_PICKAXE, InventoryUtil.Hand.Main)))) {

            for (final TileEntity entity : mc.world.loadedTileEntityList) {
                if(entity == null) continue;

                if ((entity instanceof TileEntityEnderChest || entity instanceof TileEntityBeacon || entity instanceof TileEntityFurnace || entity instanceof TileEntityHopper || entity instanceof TileEntityChest) && mc.objectMouseOver.getBlockPos().equals(entity.getPos())) {
                    e.cancel();
                    mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
                }
            }
            if (mc.world.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock() == Blocks.ANVIL) {
                e.cancel();
                mc.getConnection().sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
            }
        }

    }

    @SubscribeEvent
    public void onBlockInteract(final PlayerInteractEvent.RightClickBlock e) {
        if (fullNullCheck()) {
            return;
        }
        if ((mc.world.getBlockState(e.getPos()).getBlock() == Blocks.ANVIL
                || mc.world.getBlockState(e.getPos()).getBlock() == Blocks.ENDER_CHEST)
                && !mc.player.isSneaking() && mc.gameSettings.keyBindUseItem.isKeyDown()
                && (InventoryUtil.heldItem(Items.EXPERIENCE_BOTTLE, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.GOLDEN_APPLE, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.CHORUS_FRUIT, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.BOW, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.WRITABLE_BOOK, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.WRITTEN_BOOK, InventoryUtil.Hand.Both)
                || InventoryUtil.heldItem(Items.POTIONITEM, InventoryUtil.Hand.Both)
                || (this.pickaxe.getValue()
                && InventoryUtil.heldItem(Items.DIAMOND_PICKAXE, InventoryUtil.Hand.Main)))) {

            e.setCanceled(true);
            Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
        }
    }
}
