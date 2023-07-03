package we.devs.forever.api.util.math;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.*;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.combat.PredictPlayer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

public
class MathUtil implements Util {

    private static final Random random = new Random();

    public static double getRandom(final double min, final double max) {
        return MathHelper.clamp(min + random.nextDouble() * max, min, max);
    }

    public static int clamp(int num, int min, int max) {
        return (num < min) ? min : (Math.min(num, max));
    }

    public static float clamp(float num, float min, float max) {
        return (num < min) ? min : (Math.min(num, max));
    }

    public static double clamp(double num, double min, double max) {
        return (num < min) ? min : (Math.min(num, max));
    }

    public static float sin(final float value) {
        return MathHelper.sin(value);
    }

    public static float cos(final float value) {
        return MathHelper.cos(value);
    }

    public static float wrapDegrees(final float value) {
        return MathHelper.wrapDegrees(value);
    }

    public static Vec3d roundVec(Vec3d vec3d, int places) {
        return new Vec3d(round(vec3d.x, places), round(vec3d.y, places), round(vec3d.z, places));
    }

    public static double square(double input) {
        return (input * input);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.FLOOR);
        return bd.doubleValue();
    }

    public static float wrap(float valI) {
        float val = valI % 360.0f;
        if (val >= 180.0f)
            val -= 360.0f;
        if (val < -180.0f)
            val += 360.0f;
        return val;
    }

    public static Vec3d direction(float yaw) {
        return new Vec3d(Math.cos(degToRad(yaw + 90f)), 0, Math.sin(degToRad(yaw + 90f)));
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean descending) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        if (descending) {
            list.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        } else {
            list.sort(Map.Entry.comparingByValue());
        }

        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    public static String getTimeOfDay() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);

        if (timeOfDay < 12) {
            return "Good Morning ";
        } else if (timeOfDay < 16) {
            return "Good Afternoon ";
        } else if (timeOfDay < 21) {
            return "Good Evening ";
        } else {
            return "Good Night ";
        }
    }

    public static double degToRad(double deg) {
        return deg * (float) (Math.PI / 180.0f);
    }

    public static double[] directionSpeed(double speed) {
        float forward = mc.player.movementInput.moveForward;
        float side = mc.player.movementInput.moveStrafe;
        float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
        if (forward != 0) {
            if (side > 0) {
                yaw += (forward > 0 ? -45 : 45);
            } else if (side < 0) {
                yaw += (forward > 0 ? 45 : -45);
            }
            side = 0;
            if (forward > 0) {
                forward = 1;
            } else if (forward < 0) {
                forward = -1;
            }
        }
        final double sin = Math.sin(Math.toRadians(yaw + 90));
        final double cos = Math.cos(Math.toRadians(yaw + 90));
        final double posX = (forward * speed * cos + side * speed * sin);
        final double posZ = (forward * speed * sin - side * speed * cos);
        return new double[]{posX, posZ};
    }

    public static List<Vec3d> getBlockBlocks(Entity entity) {
        List<Vec3d> vec3ds = new ArrayList<>();
        AxisAlignedBB bb = entity.getEntityBoundingBox();
        double y = entity.posY;
        double minX = round(bb.minX, 0);
        double minZ = round(bb.minZ, 0);
        double maxX = round(bb.maxX, 0);
        double maxZ = round(bb.maxZ, 0);
        if (minX != maxX) {
            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(maxX, y, minZ));
            if (minZ != maxZ) {
                vec3ds.add(new Vec3d(minX, y, maxZ));
                vec3ds.add(new Vec3d(maxX, y, maxZ));
                return vec3ds;
            }
        } else if (minZ != maxZ) {
            vec3ds.add(new Vec3d(minX, y, minZ));
            vec3ds.add(new Vec3d(minX, y, maxZ));
            return vec3ds;
        }
        vec3ds.add(entity.getPositionVector());
        return vec3ds;
    }
    public static Vec3d predict(Vec3d vec, float factor, double xOffset, double yOffset, double zOffset) {
        return vec.add(xOffset * (double)factor, yOffset * (double)factor, zOffset * (double)factor);
    }

    public static Vec3d predict(Entity entity, float factor, boolean useY) {
        double d;
        Vec3d vec3d = entity.getPositionVector();
        double d2 = entity.motionX;
        if (useY) {
            d = entity.motionY;
            return predict(vec3d, factor, d2, d, entity.motionZ);
        }
        d = 0.0;
        return predict(vec3d, factor, d2, d, entity.motionZ);
    }
    public static boolean areVec3dsAlignedRetarded(Vec3d vec3d1, Vec3d vec3d2) {
        BlockPos pos1 = new BlockPos(vec3d1);
        BlockPos pos2 = new BlockPos(vec3d2.x, vec3d1.y, vec3d2.z);
        return pos1.equals(pos2);
    }

    public static float[] calcAngle(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt(difX * difX + difZ * difZ);
        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist)));
        if (pD > 90F) {
            pD = 90F;
        } else if (pD < -90F) {
            pD = -90F;
        }
        return new float[]{yD, pD};
    }

    public static PredictPlayer predictPlayer(EntityPlayer entity, int ticks) {
        if (entity == null) return null;

        // Position of the player
        double[] posVec = new double[]{entity.posX, entity.posY, entity.posZ};
        // This is likely a temp variable that is going to replace posVec
        double[] newPosVec;
        // entity motions
        double motionX = entity.posX - entity.prevPosX;
        double motionY = entity.posY - entity.prevPosY;
        double motionZ = entity.posZ - entity.prevPosZ;
        // Y Prediction stuff
        boolean goingUp = false;
        boolean start = true;
        // If he want manual out hole
        boolean isHole = false;
//        if (manualOutHole && motionY > .2) {
//            if (BlockUtilPa.isHole(EntityUtil.getPosition(entity), false, true).getType() != BlockUtilPa.HoleType.NONE
//                    && BlockUtil.getBlock(EntityUtil.getPosition(entity).add(0, 2, 0)) instanceof BlockAir)
//                isHole = true;
//            else if (BlockUtilPa.isHole(EntityUtil.getPosition(entity).add(0, -1, 0), false, true).getType() != BlockUtilPa.HoleType.NONE)
//                isHole = true;
//
//            if (isHole)
//                posVec[1] += 1;
//        }
        for (int i = 0; i < ticks; i++) {
            RayTraceResult result;
            // Here we can choose if calculating XZ separated or not
            // Clone posVec
            newPosVec = posVec.clone();
            // Add X
            newPosVec[0] += motionX;
            // Check collisions
            result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], posVec[2]));

            if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                posVec = newPosVec.clone();
            }
            // Calculate Z
            newPosVec = posVec.clone();
            newPosVec[2] += motionZ;
            // Check collisions
            result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]), new Vec3d(newPosVec[0], posVec[1], newPosVec[2]));
            if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                posVec = newPosVec.clone();
            }
            // In case of calculating them toogheter
            if (!isHole) {
                newPosVec = posVec.clone();
                // If the enemy is not on the ground. We also be sure that it's not -0.078
                // Because -0.078 is the motion we have when standing in a block.
                // I dont know if we have antiHunger the server say we are onGround or not, i'll keep it here
                if (!entity.onGround && motionY != -0.0784000015258789 && motionY != 0) {
                    double decreasePow = 4 / Math.pow(10, 1);
                    if (start) {
                        // If it's the first time, we have to check first if our motionY is == 0.
                        // MotionY is == 0 when we are jumping at the moment when we are going down
                        if (motionY == 0)
                            motionY = decreasePow;
                        // Check if we are going up or down. We say > because of motionY
                        start = false;
                    }
                    // Lets just add values to our motionY
                    float increasePowY = 1F / 10F;
                    float decreasePowY = 5F / 10F;
                    motionY += goingUp ? increasePowY : decreasePowY;
                    // If the motionY is going too far, go down
                    if (Math.abs(motionY) > decreasePow) {
                        goingUp = false;
                        motionY = decreasePowY;
                    }
                    // Lets add motionY
                    newPosVec[1] += (goingUp ? 1 : -1) * motionY;
                    // Get result
                    result = mc.world.rayTraceBlocks(new Vec3d(posVec[0], posVec[1], posVec[2]),
                            new Vec3d(newPosVec[0], newPosVec[1], newPosVec[2]));

                    if (result == null || result.typeOfHit == RayTraceResult.Type.ENTITY) {
                        posVec = newPosVec.clone();
                    } else {
                        if (!goingUp) {
                            goingUp = true;
                            // Add this for deleting before motion
                            newPosVec[1] += increasePowY;
                            motionY = increasePowY;
                            newPosVec[1] += motionY;
                        }
                    }


                }
            }
        }
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), entity.getName()));
        clonedPlayer.setPosition(posVec[0], posVec[1], posVec[2]);
        clonedPlayer.inventory.copyInventory(entity.inventory);
        clonedPlayer.setHealth(entity.getHealth());
        clonedPlayer.prevPosX = entity.prevPosX;
        clonedPlayer.prevPosY = entity.prevPosY;
        clonedPlayer.prevPosZ = entity.prevPosZ;
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            clonedPlayer.addPotionEffect(effect);
        }
        return new PredictPlayer(entity,clonedPlayer);
    }

    public static float[] calcAngleNoY(Vec3d from, Vec3d to) {
        double difX = to.x - from.x;
        double difZ = to.z - from.z;
        return new float[]{(float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0)};
    }

    public static double lerp(double delta, double start, double end) {
        return start + delta * (end - start);
    }

    public static float[] calculateLookAt(double x, double y, double z, EntityPlayer me) {
        double dirx = lerp(MathUtil.mc.getRenderPartialTicks(), me.lastTickPosX, me.posX) - x;
        double diry = lerp(MathUtil.mc.getRenderPartialTicks(), me.lastTickPosY, me.posY) + (double) me.getEyeHeight() - y;
        double dirz = lerp(MathUtil.mc.getRenderPartialTicks(), me.lastTickPosZ, me.posZ) - z;
        double distance = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
        float pitch = (float) Math.asin(diry / distance);
        float yaw = (float) Math.atan2(dirz / distance, dirx / distance);

        pitch = (float) ((double) (pitch * 180.0F) / 3.141592653589793D);
        yaw = (float) ((double) (yaw * 180.0F) / 3.141592653589793D);
        return new float[]{yaw += 90.0F, pitch};
    }
}

