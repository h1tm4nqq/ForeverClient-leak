package we.devs.forever.client.modules.impl.combat;

import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.util.*;

public class SelfTrap
        extends Module {
    private final Setting<Integer> blocksPerTick = (new Setting<>("BlocksPerTick", 8, 1, 20));
    private final Setting<Integer> delay = (new Setting<>("Delay", 50, 0, 250));
    private final Setting<Boolean> rotate = (new Setting<>("Rotate", true));
    private final Setting<Integer> disableTime = (new Setting<>("DisableTime", 200, 50, 300));
    private final Setting<Boolean> disable = (new Setting<>("AutoDisable", true));
    private final Setting<Boolean> packet = (new Setting<>("PacketPlace", false));
    private final TimerUtil offTimer = new TimerUtil();
    private final TimerUtil timer = new TimerUtil();
    private final Map<BlockPos, Integer> retries = new HashMap<>();
    private final TimerUtil retryTimer = new TimerUtil();
    private int blocksThisTick = 0;
    private boolean isSneaking;
    private boolean hasOffhand = false;

    public SelfTrap() {
        super("SelfTrap", "Lure your enemies in!", Module.Category.COMBAT);
    }

    @Override
    public void onEnable() {
        if (SelfTrap.fullNullCheck()) {
            this.disable();
        }
        this.offTimer.reset();
    }

    @Override
    public void onTick() {
        if (this.isEnabled() && (this.blocksPerTick.getValue() != 1 || !this.rotate.getValue())) {
            this.doHoleFill();
        }
    }

    @EventListener
    public void onUpdateWalkingPlayer(MotionEvent.Pre event) {
        if (this.isEnabled() && event.getStage() == 0 && this.blocksPerTick.getValue() == 1 && this.rotate.getValue()) {
            this.doHoleFill();
        }
    }

    @Override
    public void onDisable() {
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        this.retries.clear();
        this.hasOffhand = false;
    }

    private void doHoleFill() {
        if (this.check()) {
            return;
        }
        for (BlockPos position : this.getPositions()) {
            int placeability = BlockUtil.isPositionPlaceable(position, false);
            if (placeability == 1 && (this.retries.get(position) == null || this.retries.get(position) < 4)) {
                this.placeBlock(position);
                this.retries.put(position, this.retries.get(position) == null ? 1 : this.retries.get(position) + 1);
            }
            if (placeability != 3) continue;
            this.placeBlock(position);
        }
    }

    private List<BlockPos> getPositions() {
        ArrayList<BlockPos> positions = new ArrayList<>();
        positions.add(new BlockPos(SelfTrap.mc.player.posX, SelfTrap.mc.player.posY + 2.0, SelfTrap.mc.player.posZ));
        int placeability = BlockUtil.isPositionPlaceable(positions.get(0), false);
        switch (placeability) {
            case 0: {
                return new ArrayList<>();
            }
            case 3: {
                return positions;
            }
            case 1: {
                if (BlockUtil.isPositionPlaceable(positions.get(0), false, false) == 3) {
                    return positions;
                }
            }
            case 2: {
                positions.add(new BlockPos(SelfTrap.mc.player.posX + 1.0, SelfTrap.mc.player.posY + 1.0, SelfTrap.mc.player.posZ));
                positions.add(new BlockPos(SelfTrap.mc.player.posX + 1.0, SelfTrap.mc.player.posY + 2.0, SelfTrap.mc.player.posZ));
            }
        }
        positions.sort(Comparator.comparingDouble(Vec3i::getY));
        return positions;
    }

    private void placeBlock(BlockPos pos) {
        if (this.blocksThisTick < this.blocksPerTick.getValue()) {
            boolean smartRotate = this.blocksPerTick.getValue() == 1 && this.rotate.getValue() != false;
            int originalSlot = SelfTrap.mc.player.inventory.currentItem;
            int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
            int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
            if (obbySlot == -1 && eChestSot == -1) {
                this.toggle();
            }
            SelfTrap.mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
            SelfTrap.mc.playerController.updateController();
            this.isSneaking = smartRotate ? BlockUtil.placeBlockSmartRotate(pos, this.hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, true, this.packet.getValue(), this.isSneaking) : BlockUtil.placeBlock(pos, this.hasOffhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, this.rotate.getValue(), this.packet.getValue(), this.isSneaking);
            SelfTrap.mc.player.inventory.currentItem = originalSlot;
            SelfTrap.mc.playerController.updateController();
            this.timer.reset();
            ++this.blocksThisTick;
        }
    }

    private boolean check() {
        if (SelfTrap.fullNullCheck()) {
            this.disable();
            return true;
        }
        int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
        int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
        if (obbySlot == -1 && eChestSot == -1) {
            this.toggle();
        }
        this.blocksThisTick = 0;
        this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
        if (this.retryTimer.passedMs(2000L)) {
            this.retries.clear();
            this.retryTimer.reset();
        }
        if (!EntityUtil.isSafe(SelfTrap.mc.player)) {
            this.offTimer.reset();
            return true;
        }
        if (this.disable.getValue() && this.offTimer.passedMs(this.disableTime.getValue())) {
            this.disable();
            return true;
        }
        return !this.timer.passedMs(this.delay.getValue());
    }
}