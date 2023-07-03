package we.devs.forever.client.modules.impl.combat;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockObsidian;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class SelfAnvil extends Module {
    private final Setting<Boolean> rotate = (new Setting<>("Rotate", true));
    private final Setting<Boolean> onlyHole = (new Setting<>("HoleOnly", false));
    private final Setting<Boolean> helpingBlocks = (new Setting<>("HelpingBlocks", true));
    private final Setting<Boolean> chat = (new Setting<>("Chat Msgs", true));
    private final Setting<Boolean> packet = (new Setting<>("Packet", false));
    private final Setting<Integer> blocksPerTick = (new Setting<>("Blocks/Tick", 2, 1, 8));
    private BlockPos placePos;
    private BlockPos playerPos;
    private int blockSlot;
    private int obbySlot;
    private int lastBlock;
    private int blocksThisTick;

    public SelfAnvil() {
        super("SelfAnvil", "Self Anvil (on some servers bypasses burrow patches).", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        playerPos = new BlockPos(SelfAnvil.mc.player.posX, SelfAnvil.mc.player.posY, SelfAnvil.mc.player.posZ);
        placePos = playerPos.offset(EnumFacing.UP, 2);
        blockSlot = findBlockSlot();
        obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        lastBlock = SelfAnvil.mc.player.inventory.currentItem;
        if (!doFirstChecks()) {
            disable();
        }
    }

    @Override
    public void onTick() {
        blocksThisTick = 0;
        doSelfAnvil();
    }

    private void doSelfAnvil() {
        if (helpingBlocks.getValue() && BlockUtil.isPositionPlaceable(placePos, false, true) == 2) {
            InventoryUtil.switchToHotbarSlot(obbySlot, false);
            doHelpBlocks();
        }
        if (blocksThisTick < blocksPerTick.getValue() && BlockUtil.isPositionPlaceable(placePos, false, true) == 3) {
            InventoryUtil.switchToHotbarSlot(blockSlot, false);
            BlockUtil.placeBlock(placePos, EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), false);
            InventoryUtil.switchToHotbarSlot(lastBlock, false);
            SelfAnvil.mc.player.connection.sendPacket(new CPacketEntityAction(SelfAnvil.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            disable();
        }
    }

    private void doHelpBlocks() {
        if (blocksThisTick >= blocksPerTick.getValue()) {
            return;
        }
        for (EnumFacing side1 : EnumFacing.values()) {
            if (side1 == EnumFacing.DOWN || BlockUtil.isPositionPlaceable(placePos.offset(side1), false, true) != 3)
                continue;
            BlockUtil.placeBlock(placePos.offset(side1), EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), false);
            ++blocksThisTick;
            return;
        }
        for (EnumFacing side1 : EnumFacing.values()) {
            if (side1 == EnumFacing.DOWN) continue;
            for (EnumFacing side2 : EnumFacing.values()) {
                if (BlockUtil.isPositionPlaceable(placePos.offset(side1).offset(side2), false, true) != 3)
                    continue;
                BlockUtil.placeBlock(placePos.offset(side1).offset(side2), EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), false);
                ++blocksThisTick;
                return;
            }
        }
        for (EnumFacing side1 : EnumFacing.values()) {
            for (EnumFacing side2 : EnumFacing.values()) {
                for (EnumFacing side3 : EnumFacing.values()) {
                    if (BlockUtil.isPositionPlaceable(placePos.offset(side1).offset(side2).offset(side3), false, true) != 3)
                        continue;
                    BlockUtil.placeBlock(placePos.offset(side1).offset(side2).offset(side3), EnumHand.MAIN_HAND, rotate.getValue(), packet.getValue(), false);
                    ++blocksThisTick;
                    return;
                }
            }
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack item = SelfAnvil.mc.player.inventory.getStackInSlot(i);
            if (!(item.getItem() instanceof ItemBlock) || !((block = Block.getBlockFromItem(SelfAnvil.mc.player.inventory.getStackInSlot(i).getItem())) instanceof BlockFalling))
                continue;
            return i;
        }
        return -1;
    }

    private boolean doFirstChecks() {
        int canPlace = BlockUtil.isPositionPlaceable(placePos, false, true);
        if (SelfAnvil.fullNullCheck() || !SelfAnvil.mc.world.isAirBlock(playerPos)) {
            return false;
        }
        if (!BlockUtil.isBothHole(playerPos) && onlyHole.getValue()) {
            return false;
        }
        if (blockSlot == -1) {
            if (chat.getValue()) {
                Command.sendMessage("<" + getName() + "> \u00a7cNo Anvils in hotbar.");
            }
            return false;
        }
        if (canPlace == 2) {
            if (!helpingBlocks.getValue()) {
                if (chat.getValue()) {
                    Command.sendMessage("<" + getName() + "> \u00a7cNowhere to place.");
                }
                return false;
            }
            if (obbySlot == -1) {
                if (chat.getValue()) {
                    Command.sendMessage("<" + getName() + "> \u00a7cNo Obsidian in hotbar.");
                }
                return false;
            }
        } else if (canPlace != 3) {
            if (chat.getValue()) {
                Command.sendMessage("<" + getName() + "> \u00a7cNot enough room.");
            }
            return false;
        }
        return true;
    }
}

