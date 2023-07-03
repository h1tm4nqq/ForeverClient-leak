package we.devs.forever.api.manager.impl.player.interact;

import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.client.Client;

public class PlaceManager extends AbstractManager {
    public PlaceManager() {
        super("PlaceManager");
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }


    public boolean place(BlockPos pos, RotationType rotationType, Swing swing, boolean packet, boolean strict, boolean nolag, boolean attackCrystals) {

        if (pos == null) return false;
        ClickLocation clickLocation = ClickLocation.getClick(pos, strict);
        if (clickLocation == null) return false;

        BlockPos currentPos = clickLocation.getNeighbour();
        EnumFacing currentFace = clickLocation.getFacing();

        if (attackCrystals) {
            Client.interactionManager.attackCrystals(pos, rotationType, strict, swing);
        }

        boolean shouldSneak = Client.interactionManager.shouldShiftClick(currentPos);

        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }

        boolean sprint = mc.player.isSprinting();

        if (sprint) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }

        // our rotation
        float[] rotation = rotationManager.calculateAngle(new Vec3d(currentPos.getX(), currentPos.getY(), currentPos.getZ()));
        Vec3d hitVec = clickLocation.getHit();

        // vector to the block

        if (strict && rotationType == RotationType.Adaptive) rotationManager.setStrict();
        rotationManager.doRotation(rotationType, rotation);

        if (packet) {
            float x = (float) (hitVec.x - currentPos.getX());
            float y = (float) (hitVec.y - currentPos.getY());
            float z = (float) (hitVec.z - currentPos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPos, currentFace, EnumHand.MAIN_HAND, x, y, z));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, currentPos, currentFace, hitVec, EnumHand.MAIN_HAND);
        }

        EntityUtil.swing(swing);

        if (nolag) rotationManager.resetRotations();

        if (sprint) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            mc.player.setSprinting(true);
        }

        if (shouldSneak) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            mc.player.setSneaking(false);
        }

        return true;
    }

    public boolean inRange(BlockPos pos1, boolean strict, float range, float wallRange) {
        ClickLocation clickLocation = ClickLocation.getClick(pos1, strict);
        if (clickLocation == null) return false;
        Vec3d eyePos = mc.player.getPositionEyes(1.0f);
        Vec3d hit = clickLocation.getHit();
        BlockPos pos = clickLocation.getNeighbour();
        if (canSee(pos, hit) || !strict)
            return eyePos.squareDistanceTo(hit) <= wallRange * wallRange;
        else
            return eyePos.squareDistanceTo(hit) <= range * range;
    }

    public static boolean canSee(BlockPos blockPos, Vec3d vec3d) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX() + vec3d.x, blockPos.getY() + vec3d.y, blockPos.getZ() + vec3d.z), false, true, false) == null;
    }
}
