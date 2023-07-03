package we.devs.forever.api.util.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.render.util.TextUtil;

public
class RotationUtil implements Util {

    public static Vec3d getEyesPos() {
        return new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
    }

    public static double yawDist(BlockPos pos) {
        if (pos != null) {
            Vec3d difference = new Vec3d(pos).subtract(RotationUtil.mc.player.getPositionEyes(mc.getRenderPartialTicks()));
            double d = Math.abs((double) RotationUtil.mc.player.rotationYaw - (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0)) % 360.0;
            return d > 180.0 ? 360.0 - d : d;
        }
        return 0.0;
    }

    public static double normalizeAngle(double angle) {
        angle %= 360.0;
        if (angle >= 180.0) {
            angle -= 360.0;
        }
        if (angle < -180.0) {
            angle += 360.0;
        }
        return angle;
    }

    public static double yawDist(Entity e) {
        if (e != null) {
            Vec3d difference = e.getPositionVector().add(0.0, e.getEyeHeight() / 2.0f, 0.0).subtract(RotationUtil.mc.player.getPositionEyes(mc.getRenderPartialTicks()));
            double d = Math.abs((double) RotationUtil.mc.player.rotationYaw - (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0)) % 360.0;
            return d > 180.0 ? 360.0 - d : d;
        }
        return 0.0;
    }

    public static float[] getAnglesToBlock(BlockPos pos, EnumFacing facing) {

        // get our player positions
        double x = mc.player.posX;
        double y = mc.player.posY;
        double z = mc.player.posZ;

        // get the difference between the position and facing
        Vec3d diff = new Vec3d(
                pos.getX() + 0.5 - x + facing.getXOffset() / 2.0,
                pos.getY() + 0.5,
                pos.getZ() + 0.5 - z + facing.getZOffset() / 2.0
        );

        // find the distance between two points
        double distance = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

        // find the yaw and pitch to the vector
        float yaw = (float) (Math.atan2(diff.z, diff.x) * 180.0 / Math.PI - 90.0);
        float pitch = (float) (Math.atan2(y + mc.player.getEyeHeight() - diff.y, distance) * 180.0 / Math.PI);

        // wrap the degrees to values between -180 and 180
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch)};
    }

    public static float getFov() {
        return RotationUtil.mc.gameSettings.fovSetting;
    }

    public static float getHalvedfov() {
        return RotationUtil.getFov() / 2.0f;
    }

    public static boolean isInFov(BlockPos pos) {
        return pos != null && (RotationUtil.mc.player.getDistanceSq(pos) < 4.0 || RotationUtil.yawDist(pos) < (double) (RotationUtil.getHalvedfov() + 2.0f));
    }

    public static boolean isInFov(Entity entity) {
        return entity != null && (RotationUtil.mc.player.getDistanceSq(entity) < 4.0 || RotationUtil.yawDist(entity) < (double) (RotationUtil.getHalvedfov() + 2.0f));
    }

    public static float transformYaw() {
        float yaw = RotationUtil.mc.player.rotationYaw % 360.0f;
        if (RotationUtil.mc.player.rotationYaw > 0.0f) {
            if (yaw > 180.0f) {
                yaw = -180.0f + (yaw - 180.0f);
            }
        } else if (yaw < -180.0f) {
            yaw = 180.0f + (yaw + 180.0f);
        }
        if (yaw < 0.0f) {
            return 180.0f + yaw;
        }
        return -180.0f + yaw;
    }

    public static float[] calcAngleNoY(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difZ = to.z - from.z;
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)};
    }

    public static boolean isInFov(Vec3d vec3d, Vec3d other) {
        if (RotationUtil.mc.player.rotationPitch > 30.0f ? other.y > RotationUtil.mc.player.posY : RotationUtil.mc.player.rotationPitch < -30.0f && other.y < RotationUtil.mc.player.posY) {
            return true;
        }
        float angle = calcAngleNoY(vec3d, other)[0] - RotationUtil.transformYaw();
        if (angle < -270.0f) {
            return true;
        }
        float fov = RotationUtil.mc.gameSettings.fovSetting / 2.0f;
        return angle < fov + 10.0f && angle > -fov - 10.0f;
    }

    public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
        double dirx = me.posX - px;
        double diry = me.posY - py;
        double dirz = me.posZ - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]{yaw, pitch};
    }

    public static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]{
                mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
                mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)
        };
    }

    public static void faceYawAndPitch(float yaw, float pitch) {
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, mc.player.onGround));
    }

    public static void faceVector(Vec3d vec, boolean normalizeAngle) {
        float[] rotations = getLegitRotations(vec);
        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], normalizeAngle ? MathHelper.normalizeAngle((int) rotations[1], 360) : rotations[1], mc.player.onGround));
    }

    public static void faceEntity(Entity entity) {
        final float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()));
        faceYawAndPitch(angle[0], angle[1]);
    }

    public static float[] getAngle(Entity entity) {
        return MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()));
    }

    public static int getDirection4D() {
        return MathHelper.floor((mc.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
    }

    public static String getDirection4D(boolean northRed) {
        int dirnumber = getDirection4D();
        if (dirnumber == 0) {
            return "South (+Z)";
        }
        if (dirnumber == 1) {
            return "West (-X)";
        }
        if (dirnumber == 2) {
            return (northRed ? TextUtil.RED : "") + "North (-Z)";
        }
        if (dirnumber == 3) {
            return "East (+X)";
        }
        return "Loading...";
    }

    public static boolean newIsInFov(BlockPos pos) {
        return pos == null || !(RotationUtil.mc.player.getDistanceSq(pos) < 4.0) && !(RotationUtil.yawDist(pos) < (double) (RotationUtil.getHalvedfov() + 2.0f));
    }

    public static boolean newIsInFov(Entity entity) {
        return entity == null || !(RotationUtil.mc.player.getDistanceSq(entity) < 4.0) && !(RotationUtil.yawDist(entity) < (double) (RotationUtil.getHalvedfov() + 2.0f));
    }

    public static boolean newIsInFov(Vec3d vec3d, Vec3d other) {
        if (RotationUtil.mc.player.rotationPitch > 30.0f ? other.y > RotationUtil.mc.player.posY : RotationUtil.mc.player.rotationPitch < -30.0f && other.y < RotationUtil.mc.player.posY) {
            return true;
        }
        float angle = MathUtil.calcAngleNoY(vec3d, other)[0] - RotationUtil.transformYaw();
        if (angle < -270.0f) {
            return true;
        }
        float fov = RotationUtil.mc.gameSettings.fovSetting / 2.0f;
        return angle < fov + 10.0f && angle > -fov - 10.0f;
    }

    public static void faceVectorPacketInstant(final Vec3d faceVec) {
        final float[] var = getLegitRotations(faceVec);
        RotationUtil.mc.player.connection.sendPacket(new CPacketPlayer.Rotation(var[0], var[1], RotationUtil.mc.player.onGround));
    }

    public static double[] Method1330(double d) {
        float f = mc.player.movementInput.moveForward;
        float f2 = mc.player.movementInput.moveStrafe;
        float f3 = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (f != 0.0f) {
            if (f2 > 0.0f) {
                f3 += (float)(f > 0.0f ? -45 : 45);
            } else if (f2 < 0.0f) {
                f3 += (float)(f > 0.0f ? 45 : -45);
            }
            f2 = 0.0f;
            if (f > 0.0f) {
                f = 1.0f;
            } else if (f < 0.0f) {
                f = -1.0f;
            }
        }
        double d2 = Math.sin(Math.toRadians(f3 + 90.0f));
        double d3 = Math.cos(Math.toRadians(f3 + 90.0f));
        double d4 = (double)f * d * d3 + (double)f2 * d * d2;
        double d5 = (double)f * d * d2 - (double)f2 * d * d3;
        return new double[]{d4, d5};
    }

    public static Vec3d getVectorForRotation(float[] rotation) {
        float yawCos = MathHelper.cos(-rotation[0] * 0.017453292F - (float) Math.PI);
        float yawSin = MathHelper.sin(-rotation[0] * 0.017453292F - (float) Math.PI);
        float pitchCos = -MathHelper.cos(-rotation[1] * 0.017453292F);
        float pitchSin = MathHelper.sin(-rotation[1]* 0.017453292F);
        return new Vec3d(yawSin * pitchCos, pitchSin, yawCos * pitchCos);
    }
}
