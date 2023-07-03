package we.devs.forever.client.modules.impl.combat.autocrystalold.listeners;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.*;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.manager.impl.player.RotationManager;
import we.devs.forever.client.modules.api.listener.ModuleListener;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.DirectionMode;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.Combatutil;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

public class ListenerOnUpdateWalkingPlayer extends ModuleListener<AutoCrystal, MotionEvent.Pre> {
    public ListenerOnUpdateWalkingPlayer(AutoCrystal module) {
        super(module, MotionEvent.Pre.class , ListenerPriority.HIGH);
    }

    @Override
    public void invoke(MotionEvent.Pre event) {
//        module.placeLocations.forEach((pos, time) -> {
//            if (System.currentTimeMillis() - time > 1000) {
//                module.placeLocations.remove(pos);
//            }
//        });
//        module.breakLocations.forEach((id, time) -> {
//            if (System.currentTimeMillis() - time > 5000) {
//                module.breakLocations.remove(id);
//            }
//        });
//        if (module.clearTimer.passedMs(1000)) {
//            // Command.sendMessage(String.format("Place: %s Break: %s", counterPlace, counterBreak));
//            module.counterPlace = 0;
//            module.counterBreak = 0;
//            module.clearTimer.reset();
//
//        }
//        if (module.renderTimer.passedMs(1000)) {
//            module.renderTimer.reset();
//            module.renderPos = null;
//        }
//        if((Burrow.burrow.isActive() && module.stopBurrow.getValue())) return;
//        if((AutoPiston.autoPiston.isEnabled() && module.stopAutoPiston.getValue())) return;
//        if((PistonPush.pistonPush.isEnabled() && module.stopPistonPush.getValue())) return;
    }

    public EnumFacing handlePlaceRotation(BlockPos pos) {
        if (pos == null || mc.player == null) {
            return null;
        }
        EnumFacing facing = null;
        if (module.directionMode.getValue() != DirectionMode.Vanilla) {
            Vec3d placeVec = null;
            double[] placeRotation = null;

            double increment = 0.45D;
            double start = 0.05D;
            double end = 0.95D;

            Vec3d eyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()), mc.player.posZ);

            for (double xS = start; xS <= end; xS += increment) {
                for (double yS = start; yS <= end; yS += increment) {
                    for (double zS = start; zS <= end; zS += increment) {
                        Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                        double distToPosVec = eyesPos.distanceTo(posVec);
                        double diffX = posVec.x - eyesPos.x;
                        double diffY = posVec.y - eyesPos.y;
                        double diffZ = posVec.z - eyesPos.z;
                        double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                        double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                        // inline values for slightly better perfornamce
                        float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                        float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                        float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                        float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                        Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                        Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                        RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, true, false);
                        if (module.placeWallRange.getValue() >= module.placeRange.getValue() || (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK && rayTraceResult.getBlockPos().equals(pos))) {

                            if (module.strictDirection.getValue()) {
                                if (placeVec != null && placeRotation != null && ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) || facing == null)) {
                                    if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(posVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(placeVec)) {
                                        placeVec = posVec;
                                        placeRotation = tempPlaceRotation;
                                        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                            facing = rayTraceResult.sideHit;
                                            module.postResult = rayTraceResult;
                                        }
                                    }
                                } else {
                                    placeVec = posVec;
                                    placeRotation = tempPlaceRotation;
                                    if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                        facing = rayTraceResult.sideHit;
                                        module.postResult = rayTraceResult;
                                    }
                                }
                            } else {
                                if (placeVec != null && placeRotation != null && ((rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) || facing == null)) {
                                    if (Math.hypot((((tempPlaceRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempPlaceRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                            Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                        placeVec = posVec;
                                        placeRotation = tempPlaceRotation;
                                        if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                            facing = rayTraceResult.sideHit;
                                            module.postResult = rayTraceResult;
                                        }
                                    }
                                } else {
                                    placeVec = posVec;
                                    placeRotation = tempPlaceRotation;
                                    if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                        facing = rayTraceResult.sideHit;
                                        module.postResult = rayTraceResult;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (module.directionMode.getValue() == DirectionMode.Strict) {
                if (placeRotation != null && facing != null) {
                    module.rotationTimer.reset();
                    module.rotationVector = placeVec;
                    float[] angle = RotationManager.calculateAngle(module.rotationVector);
                    module.yaw = angle[0];
                    module.pitch = angle[1];
                    return facing;
                } else {
                    for (double xS = start; xS <= end; xS += increment) {
                        for (double yS = start; yS <= end; yS += increment) {
                            for (double zS = start; zS <= end; zS += increment) {
                                Vec3d posVec = (new Vec3d(pos)).add(xS, yS, zS);

                                double distToPosVec = eyesPos.distanceTo(posVec);
                                double diffX = posVec.x - eyesPos.x;
                                double diffY = posVec.y - eyesPos.y;
                                double diffZ = posVec.z - eyesPos.z;
                                double diffXZ = MathHelper.sqrt(diffX * diffX + diffZ * diffZ);

                                double[] tempPlaceRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, diffXZ)))};

                                // inline values for slightly better perfornamce
                                float yawCos = MathHelper.cos((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                                float yawSin = MathHelper.sin((float) (-tempPlaceRotation[0] * 0.017453292F - 3.1415927F));
                                float pitchCos = -MathHelper.cos((float) (-tempPlaceRotation[1] * 0.017453292F));
                                float pitchSin = MathHelper.sin((float) (-tempPlaceRotation[1] * 0.017453292F));

                                Vec3d rotationVec = new Vec3d((yawSin * pitchCos), pitchSin, (yawCos * pitchCos));
                                Vec3d eyesRotationVec = eyesPos.add(rotationVec.x * distToPosVec, rotationVec.y * distToPosVec, rotationVec.z * distToPosVec);

                                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(eyesPos, eyesRotationVec, false, true, true);
                                if (rayTraceResult != null && rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {

                                    if (module.strictDirection.getValue()) {
                                        if (placeVec != null && placeRotation != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || facing == null)) {
                                            if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(posVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(placeVec)) {
                                                placeVec = posVec;
                                                placeRotation = tempPlaceRotation;
                                                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                    facing = rayTraceResult.sideHit;
                                                    module.postResult = rayTraceResult;
                                                }
                                            }
                                        } else {
                                            placeVec = posVec;
                                            placeRotation = tempPlaceRotation;
                                            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                facing = rayTraceResult.sideHit;
                                                module.postResult = rayTraceResult;
                                            }
                                        }
                                    } else {
                                        if (placeVec != null && placeRotation != null && (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK || facing == null)) {
                                            if (Math.hypot((((tempPlaceRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempPlaceRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                                    Math.hypot((((placeRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (placeRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                                placeVec = posVec;
                                                placeRotation = tempPlaceRotation;
                                                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                    facing = rayTraceResult.sideHit;
                                                    module.postResult = rayTraceResult;
                                                }
                                            }
                                        } else {
                                            placeVec = posVec;
                                            placeRotation = tempPlaceRotation;
                                            if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                                                facing = rayTraceResult.sideHit;
                                                module.postResult = rayTraceResult;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (placeRotation != null) {
                    module.rotationTimer.reset();
                    module.rotationVector = placeVec;
                    float[] angle = RotationManager.calculateAngle(module.rotationVector);
                    module.yaw = angle[0];
                    module.pitch = angle[1];
                }
                if (facing != null) {
                    return facing;
                }
            }
        } else {
            EnumFacing bestFacing = null;
            Vec3d bestVector = null;
            for (EnumFacing enumFacing : EnumFacing.values()) {
                Vec3d cVector = new Vec3d(pos.getX() + 0.5 + enumFacing.getDirectionVec().getX() * 0.5,
                        pos.getY() + 0.5 + enumFacing.getDirectionVec().getY() * 0.5,
                        pos.getZ() + 0.5 + enumFacing.getDirectionVec().getZ() * 0.5);
                RayTraceResult rayTraceResult = mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), cVector, false, true, false);
                if (rayTraceResult != null && rayTraceResult.typeOfHit.equals(RayTraceResult.Type.BLOCK) && rayTraceResult.getBlockPos().equals(pos)) {
                    if (module.strictDirection.getValue()) {
                        if (bestVector == null || mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(cVector) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(bestVector)) {
                            bestVector = cVector;
                            bestFacing = enumFacing;
                            module.postResult = rayTraceResult;
                        }
                    } else {
                        module.rotationTimer.reset();
                        module.rotationVector = cVector;
                        float[] angle = RotationManager.calculateAngle(module.rotationVector);
                        module.yaw = angle[0];
                        module.pitch = angle[1];
                        return enumFacing;
                    }
                }
            }
            if (bestFacing != null) {
                module.rotationTimer.reset();
                module.rotationVector = bestVector;
                float[] angle = RotationManager.calculateAngle(module.rotationVector);
                module.yaw = angle[0];
                module.pitch = angle[1];
                return bestFacing;
            } else if (module.strictDirection.getValue()) {
                for (EnumFacing enumFacing : EnumFacing.values()) {
                    Vec3d cVector = new Vec3d(pos.getX() + 0.5 + enumFacing.getDirectionVec().getX() * 0.5,
                            pos.getY() + 0.5 + enumFacing.getDirectionVec().getY() * 0.5,
                            pos.getZ() + 0.5 + enumFacing.getDirectionVec().getZ() * 0.5);
                    if (bestVector == null || mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(cVector) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(bestVector)) {
                        bestVector = cVector;
                        bestFacing = enumFacing;
                    }
                }
                if (bestFacing != null) {
                    module.rotationTimer.reset();
                    module.rotationVector = bestVector;
                    float[] angle = RotationManager.calculateAngle(module.rotationVector);
                    module.yaw = angle[0];
                    module.pitch = angle[1];
                    return bestFacing;
                }
            }
        }
        if ((double) pos.getY() > mc.player.posY + (double) mc.player.getEyeHeight()) {
            module.rotationTimer.reset();
            module.rotationVector = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
            float[] angle = RotationManager.calculateAngle(module.rotationVector);
            module.yaw = angle[0];
            module.pitch = angle[1];
            return EnumFacing.DOWN;
        }
        module.rotationTimer.reset();
        module.rotationVector = new Vec3d(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        float[] angle = RotationManager.calculateAngle(module.rotationVector);
        module.yaw = angle[0];
        module.pitch = angle[1];
        return EnumFacing.UP;
    }

    public void handleBreakRotation(double x, double y, double z) {
        double delay = module.breakSpeed.getValue() * 5;
        if (module.rotationVector != null && !module.rotationTimer.passedMs(100 - delay)) {
            if (module.rotationVector.y < y - 0.1) {
                module.rotationVector = new Vec3d(module.rotationVector.x, y, module.rotationVector.z);
            }
            float[] angle = RotationManager.calculateAngle(module.rotationVector);
            module.yaw = angle[0];
            module.pitch = angle[1];
            module.rotationTimer.reset();
            return;
        }

        AxisAlignedBB bb = new AxisAlignedBB(x - 1D, y, z - 1D, x + 1D, y + 2D, z + 1D);

        Vec3d gEyesPos = new Vec3d(mc.player.posX, (mc.player.getEntityBoundingBox().minY + mc.player.getEyeHeight()), mc.player.posZ);

        double increment = 0.1D;
        double start = 0.15D;
        double end = 0.85D;

        if (bb.intersects(mc.player.getEntityBoundingBox())) {
            start = 0.4D;
            end = 0.6D;
            increment = 0.05D;
        }

        Vec3d finalVec = null;
        double[] finalRotation = null;
        boolean finalVisible = false;

        for (double xS = start; xS <= end; xS += increment) {
            for (double yS = start; yS <= end; yS += increment) {
                for (double zS = start; zS <= end; zS += increment) {
                    Vec3d tempVec = new Vec3d(bb.minX + ((bb.maxX - bb.minX) * xS), bb.minY + ((bb.maxY - bb.minY) * yS), bb.minZ + ((bb.maxZ - bb.minZ) * zS));
                    double diffX = tempVec.x - gEyesPos.x;
                    double diffY = tempVec.y - gEyesPos.y;
                    double diffZ = tempVec.z - gEyesPos.z;
                    double[] tempRotation = new double[]{MathHelper.wrapDegrees((float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F), MathHelper.wrapDegrees((float) -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ))))};

                    boolean isVisible = true;

                    if (module.directionMode.getValue() != DirectionMode.Vanilla) {
                        if (!Combatutil.isVisible(tempVec)) {
                            isVisible = false;
                        }
                    }

                    if (module.strictDirection.getValue()) {
                        if (finalVec != null) {
                            if ((isVisible || !finalVisible)) {
                                if (mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(tempVec) < mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0).distanceTo(finalVec)) {
                                    finalVec = tempVec;
                                    finalRotation = tempRotation;
                                }
                            }
                        } else {
                            finalVec = tempVec;
                            finalRotation = tempRotation;
                            finalVisible = isVisible;
                        }
                    } else {
                        if (finalVec != null) {
                            if (isVisible || !finalVisible) {
                                if (Math.hypot((((tempRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (tempRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch())) <
                                        Math.hypot((((finalRotation[0] - ((IEntityPlayerSP) mc.player).getLastReportedYaw()) % 360.0F + 540.0F) % 360.0F - 180.0F), (finalRotation[1] - ((IEntityPlayerSP) mc.player).getLastReportedPitch()))) {
                                    finalVec = tempVec;
                                    finalRotation = tempRotation;
                                }
                            }
                        } else {
                            finalVec = tempVec;
                            finalRotation = tempRotation;
                            finalVisible = isVisible;
                        }
                    }
                }
            }
        }
        module.rotationTimer.reset();
        module.rotationVector = finalVec;
        float[] angle = RotationManager.calculateAngle(module.rotationVector);
        module.yaw = angle[0];
        module.pitch = angle[1];
    }
}