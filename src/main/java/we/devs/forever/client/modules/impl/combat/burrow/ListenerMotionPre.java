package we.devs.forever.client.modules.impl.combat.burrow;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.combat.PredictPlayer;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.listener.ModuleListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static we.devs.forever.client.modules.impl.exploit.packetfly.PacketFly.randomHorizontal;

public class ListenerMotionPre extends ModuleListener<Burrow, MotionEvent.Pre> {
    public ListenerMotionPre(Burrow module) {
        super(module, MotionEvent.Pre.class);
    }

    @Override
    public void invoke(MotionEvent.Pre event) {
        if (module.autoBurrow.getValue()) {
            module.startPos = new BlockPos(new Vec3d(MathUtil.round(mc.player.getPositionVector().x, 0), MathUtil.round(mc.player.getPositionVector().y, 0), MathUtil.round(mc.player.getPositionVector().z, 0)));

            if (!doChecks()) return;

            if (module.mode.getValue().isBurrowBlock()
                    && (!BlockUtil.isPosEmpty(module.startPos.up(2)) || !checkEntity(module.startPos))) return;

            if (module.onlyInHole.getValue() && (!BlockUtil.validObi(module.startPos) && !BlockUtil.validBedrock(module.startPos)))
                return;

            List<PredictPlayer> targets = new LinkedList<>(mc.world.playerEntities).stream()
                    .filter(player -> EntityUtil.isValid(player, 8F))
                    .map(entityPlayer -> module.extrapolationTicks.getValue() > 0 ? we.devs.forever.api.util.math.MathUtil.predictPlayer(entityPlayer, module.extrapolationTicks.getValue()) : new PredictPlayer(entityPlayer, entityPlayer))
                    .collect(Collectors.toCollection(LinkedList::new));

            if (targets.isEmpty()) return;

            for (PredictPlayer player : targets) {
                if (player.getTarget().getDistanceSq(module.startPos) <= module.smartRange.getValue() * module.smartRange.getValue()) {
                    placeBurrow();
                    break;
                }
            }

        } else {
            if (mc.player.motionY > 0) {
                module.disable();
                return;
            }
            if (!Objects.equals(module.startPos, new BlockPos(new Vec3d(MathUtil.round(mc.player.getPositionVector().x, 0), MathUtil.round(mc.player.getPositionVector().y, 0), MathUtil.round(mc.player.getPositionVector().z, 0))))) {
                module.disable();
                return;
            }
            placeBurrow();
        }
    }


    public void placeBurrow() {
        if (doChecks()) {
            if (getBlock() == -1) {
                module.disable("Can't find block");
                return;
            }
            if (module.mode.getValue().isBurrowBlock()) {
                if (!module.bypass.getValue()) {
                    if (BlockUtil.isPosEmpty(module.startPos.up(2)) && checkEntity(module.startPos)) {
                        module.isActive = true;

                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698F, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997F, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214F, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821F, mc.player.posZ, mc.player.onGround));

                        module.switchUtil.switchTo(getBlock());
                        placeManager.place(module.startPos, module.rotate.getValue(), module.swing.getValue(), true, module.strict.getValue(), false, module.attackCrystal.getValue());
                        module.switchUtil.switchBack();
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + module.tp.getValue(), mc.player.posZ, false));
                        //   mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - 15, mc.player.posY, mc.player.posZ, false));
                        module.renderBlocks.put(module.startPos, System.currentTimeMillis());
                    }
                } else {
                    if (BlockUtil.isPosEmpty(module.startPos.up(2)) && checkEntity(module.startPos)) {
                        module.isActive = true;

                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698F, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997F, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214F, mc.player.posZ, mc.player.onGround));
                        mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821F, mc.player.posZ, mc.player.onGround));

                        module.switchUtil.switchTo(getBlock());
                        placeManager.place(module.startPos, module.rotate.getValue(), module.swing.getValue(), true, module.strict.getValue(), false, module.attackCrystal.getValue());
                        module.switchUtil.switchBack();
                        for (int i = 0; i < module.retries.getValue(); i++) {
//                        mc.player.setVelocity(module.tp.getValue(), mc.player.posY, module.tp.getValue());
                            sendPackets(module.tp.getValue(), mc.player.posY, module.tp.getValue());
                        }
                        //   mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - 15, mc.player.posY, mc.player.posZ, false));
                        module.renderBlocks.put(module.startPos, System.currentTimeMillis());
                    }
                }
            } else {
                module.switchUtil.switchTo(getBlock());
                placeManager.place(module.startPos, module.rotate.getValue(), module.swing.getValue(), false, module.strict.getValue(), false, module.attackCrystal.getValue());

                module.renderBlocks.put(module.startPos, System.currentTimeMillis());
                module.switchUtil.switchBack();
            }
            if (module.autoDisable.getValue()) module.disable();

        } else module.isActive = false;
    }


    public void sendPackets(double x, double y, double z) {
        Vec3d nextPos = new Vec3d(mc.player.posX + x, mc.player.posY + y, mc.player.posZ + z);
        Vec3d bounds = new Vec3d(mc.player.posX + randomHorizontal(), Math.max(1.5D, Math.min(mc.player.posY + y, 253.5D)), mc.player.posZ + randomHorizontal());

        CPacketPlayer nextPosPacket = new CPacketPlayer.Position(nextPos.x, nextPos.y, nextPos.z, mc.player.onGround);
        mc.player.connection.sendPacket(nextPosPacket);

        if (!module.twice.getValue()) return;
        CPacketPlayer boundsPacket = new CPacketPlayer.Position(bounds.x, bounds.y, bounds.z, mc.player.onGround);
        mc.player.connection.sendPacket(boundsPacket);
    }

    private int getHeadSlot() {
        int slot = -1;
        for (int i = 0; i < 9; i++) {
            final ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemSkull) {
                slot = i;
                break;
            }
        }
        return slot;
    }

    public int getBlock() {
        switch (module.mode.getValue()) {
            case All: {
                return InventoryUtil.findAnyBlock0();
            }
            case Obsidian:
            case EChest: {
                for (Block block : module.mode.getValue().getBlocks()) {
                    int slot = InventoryUtil.findHotbarBlock(block);
                    if (slot > -1 && slot < 9) return slot;
                }
                break;
            }
            case Web: {
                return InventoryUtil.findHotbarBlock(Blocks.WEB);
            }
            case Skulls: {
                return getHeadSlot();
            }
            case Block: {
                return InventoryUtil.findAnyBlock(module.mode.getValue().getBlocks());
            }
        }
        return -1;
    }

    private boolean doChecks() {
        return BlockUtil.canPlace(module.startPos) &&
                BlockUtil.isPosEmpty(module.startPos) &&
                mc.player.onGround;
    }

    private boolean checkEntity(BlockPos pos) {
        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))).stream()
                .filter(Objects::nonNull)
                .filter(entity -> !entity.equals(mc.player))
                .filter(entity -> !entity.isDead)
                .filter(entity -> !(entity instanceof EntityXPOrb || entity instanceof EntityItem || entity instanceof EntityArrow || entity instanceof EntityEnderCrystal)).count() == 0;


    }

}
