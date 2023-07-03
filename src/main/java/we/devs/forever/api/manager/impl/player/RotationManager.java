package we.devs.forever.api.manager.impl.player;

import net.minecraft.block.Block;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.api.util.render.entity.StaticModelPlayer;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

//Important: only call this onUpdateWalkingPlayer stage 0!
public
class RotationManager extends AbstractManager {

    public float desiredPitch;
    public float desiredYaw;
    private float yaw;
    private float pitch;
    private float renderYaw;
    private float renderPitch;
    private float renderYawOffset;
    private float prevYaw;
    private float prevPitch;
    private float prevRenderYawOffset;
    private float prevRotationYawHead;
    private float rotationYawHead;
    private int ticksExisted;
    private boolean isSet = false;
    public boolean isRenderSet = false;
    private int priority = 0;
    private boolean isStrict = false;
    private boolean flag = false;
    public float partialTicks;
    private final TimerUtil rotateTimer = new TimerUtil();
    private final ModelPlayer modelPlayer = mc.getRenderManager().playerRenderer.getMainModel();
    ;

    public RotationManager() {
        super("RotationManager");
    }


    public void doRotation(RotationType rotation, BlockPos pos, int priority) {
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            float[] rotations = calculateAngle(new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            doRotation(rotation, rotations[0], rotations[1], priority);
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        float[] rotations = calculateAngle(hitVec);
        doRotation(rotation, rotations[0], rotations[1], priority);
    }

    public void doRotation(RotationType rotation, BlockPos pos) {
        EnumFacing side = BlockUtil.getFirstFacing(pos);
        if (side == null) {
            float[] rotations = calculateAngle(new Vec3d(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f));
            doRotation(rotation, rotations[0], rotations[1], 0);
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        float[] rotations = calculateAngle(hitVec);
        doRotation(rotation, rotations[0], rotations[1], 0);
    }

    public void doRotation(RotationType rotation, Vec3d vec, int priority) {
//        float[] rotations = calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec);
        float[] rotations = calculateAngle(vec);
        doRotation(rotation, rotations[0], rotations[1], priority);
    }

    public void doRotation(RotationType rotation, Vec3d vec) {
//        float[] rotations = calculateAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), vec);
        float[] rotations = calculateAngle(vec);
        doRotation(rotation, rotations[0], rotations[1], 0);
    }

    public void doRotation(RotationType rotation, float[] rotations) {
        doRotation(rotation, rotations[0], rotations[1], 0);
    }

    public void doRotation(RotationType rotation, float[] rotations, int priority) {
        doRotation(rotation, rotations[0], rotations[1], priority);
    }

    public void doRotation(RotationType rotation, float yaw, float pitch) {
        doRotation(rotation, yaw, pitch, 0);
    }

    public void doRotation(RotationType rotation, float yaw, float pitch, int priority) {
        if (priority != 0 && this.priority > priority) return;
        if (priority != 0) this.priority = priority;
//        synchronized (this) {
        //   setRenderRotations(doSlowRotations(yaw), pitch);
        isSet = true;

        renderRotationManager.set(yaw, pitch);
        switch (rotation) {
            case Legit: {
//                setRotations(yaw, pitch);
                   if (motionEvent != null)
                       motionEvent.setRotations(yaw, pitch);
//                    updateEvent.setRotations(yaw, pitch);
                break;
            }
            case Adaptive: {
//                resetRotations();
                if (isAdaptive()) {
                    lookAt(yaw, pitch, false, true);
                } else {
                    setRotations(yaw, pitch);
                }
                isStrict = false;
                break;
            }
            case Normal: {
                lookAt(yaw, pitch, false, false);
                break;
            }
            case Packet: {
                lookAt(yaw, pitch, false, true);
                break;
            }
        }
    }


    public float injectYawStep(float fArray, float f) {
        float f2;
        float f3;
        if (f < 0.1f) {
            f = 0.1f;
        }
        if (f > 1.0f) {
            f = 1.0f;
        }
        if (f < 1.0f && Math.abs(f3 = MathHelper.wrapDegrees((float) (fArray - (f2 = ((IEntityPlayerSP) RotationManager.mc.player).getLastReportedYaw())))) > 180.0f * f) {
            fArray = f2 + f3 * (180.0f * f / Math.abs(f3));
        }
        return fArray;
    }

    //Client-side rotations

    public void updateRotations() {
        yaw = mc.player.rotationYaw;
        pitch = mc.player.rotationPitch;
    }

    public void updateRotations(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void resetRotations() {
//        renderRotationManager.set(mc.player.rotationYaw, mc.player.rotationPitch);
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.rotationPitch = pitch;

        priority = 0;
        isSet = false;
        isStrict = false;
    }

    public void setRotations(float yaw, float pitch) {
        mc.player.rotationYaw = yaw;
        mc.player.rotationYawHead = yaw;
        mc.player.rotationPitch = pitch;
    }


    public Vec3d getVectorForRotation(float yaw, float pitch) {
        float yawCos = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-pitch * 0.017453292F);
        float pitchSin = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }

    //Packet rotations
    public void lookAt(float yaw, float pitch, boolean normalize, boolean update) {
        float[] angle = {yaw, pitch};
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], normalize
                ? (float) MathHelper.normalizeAngle((int) angle[1], 360)
                : angle[1], positionManager.getOnGround()));

        if (update) {
            ((IEntityPlayerSP) mc.player).setLastReportedYaw(angle[0]);
            ((IEntityPlayerSP) mc.player).setLastReportedPitch(angle[1]);
        }
    }

    public void resetRotationsPacket() {
        float[] angle = new float[]{mc.player.rotationYaw, mc.player.rotationPitch};
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(angle[0], angle[1], positionManager.getOnGround()));
    }

    public float[] getAngle(Vec3d vec) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ);
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new float[]{mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)};
    }

    /*Render rotations*/
    private void set(float yaw, float pitch) {
        if (mc.player.ticksExisted == ticksExisted) {
            return;
        }

        ticksExisted = mc.player.ticksExisted;
        prevYaw = renderYaw;
        prevPitch = renderPitch;

        prevRenderYawOffset = renderYawOffset;
        renderYawOffset = getRenderYawOffset(yaw, prevRenderYawOffset);

        prevRotationYawHead = rotationYawHead;
        rotationYawHead = yaw;

        renderYaw = yaw;
        renderPitch = pitch;
    }

    public float getRenderYaw() {
        return renderYaw;
    }

    public float getRenderPitch() {
        return renderPitch;
    }

    public float getRotationYawHead() {
        return rotationYawHead;
    }

    public float getRenderYawOffset() {
        return renderYawOffset;
    }

    public float getPrevYaw() {
        return prevYaw;
    }

    public float getPrevPitch() {
        return prevPitch;
    }

    public float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    private float getRenderYawOffset(float yaw, float offsetIn) {
        float result = offsetIn;
        float offset;

        double xDif = mc.player.posX - mc.player.prevPosX;
        double zDif = mc.player.posZ - mc.player.prevPosZ;

        if (xDif * xDif + zDif * zDif > 0.0025000002f) {
            offset = (float) MathHelper.atan2(zDif, xDif) * 57.295776f - 90.0f;
            float wrap = MathHelper.abs(MathHelper.wrapDegrees(yaw) - offset);
            if (95.0F < wrap && wrap < 265.0F) {
                result = offset - 180.0F;
            } else {
                result = offset;
            }
        }

        if (mc.player.swingProgress > 0.0F) {
            result = yaw;
        }

        result = offsetIn + MathHelper.wrapDegrees(result - offsetIn) * 0.3f;
        offset = MathHelper.wrapDegrees(yaw - result);

        if (offset < -75.0f) {
            offset = -75.0f;
        } else if (offset >= 75.0f) {
            offset = 75.0f;
        }

        result = yaw - offset;
        if (offset * offset > 2500.0f) {
            result += offset * 0.2f;
        }

        return result;
    }


    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public String getDirection4D(boolean northRed) {
        return RotationUtil.getDirection4D(northRed);
    }

    public boolean isSet() {
        return isSet;
    }

    public void setStrict() {
        isStrict = true;
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }

    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks) {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F) {
            ;
        }

        while (f >= 180.0F) {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }

    private boolean isAdaptive() {
        Block block = mc.world.getBlockState(mc.player.getPosition()).getBlock();

        if (isStrict) return !EntityUtil.isMoving() || EntityUtil.isInHole(mc.player) || mc.player.onGround
                || (block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.ENDER_CHEST) || block.equals(Blocks.ANVIL));

        return true;
    }

    public ModelPlayer setPlayerModel(float yaw, float pitch) {
        return new StaticModelPlayer(mc.player, yaw, pitch, mc.player instanceof AbstractClientPlayer && ((AbstractClientPlayer) mc.player).getSkinType().equals("slim"), 0);
    }


//    public Vec3d getVectorForRotation(float... rots) {
//        float yawCos = MathHelper.cos(-rots[0] * 0.017453292F - (float) Math.PI);
//        float yawSin = MathHelper.sin(-rots[0] * 0.017453292F - (float) Math.PI);
//        float pitchCos = -MathHelper.cos(-rots[1] * 0.017453292F);
//        float pitchSin = MathHelper.sin(-rots[1] * 0.017453292F);
//        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
//    }

    public static float[] calculateAngle(Vec3d vec3d) {
        Vec3d vec3d2 = new Vec3d(RotationManager.mc.player.posX, RotationManager.mc.player.posY + (double) RotationManager.mc.player.getEyeHeight(), RotationManager.mc.player.posZ);
        double d = vec3d.x - vec3d2.x;
        double d2 = vec3d.y - vec3d2.y;
        double d3 = vec3d.z - vec3d2.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float) Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float) (-Math.toDegrees(Math.atan2(d2, d4)));
        return new float[]{RotationManager.mc.player.rotationYaw + MathHelper.wrapDegrees((float) (f - RotationManager.mc.player.rotationYaw)), RotationManager.mc.player.rotationPitch + MathHelper.wrapDegrees((float) (f2 - RotationManager.mc.player.rotationPitch))};
    }


    public float[] getRotations() {
        return new float[]{yaw, pitch};
    }


}
