package we.devs.forever.client.modules.impl.combat.autocrystal;

import com.mojang.realmsclient.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.util.math.MathUtil;
import we.devs.forever.client.modules.api.listener.ModuleListener;
import we.devs.forever.mixin.mixins.accessor.IEntityPlayerSP;

public class ListenerMotionPre extends ModuleListener<AutoCrystal, MotionEvent.Pre> {
    public ListenerMotionPre(AutoCrystal module) {
        super(module, MotionEvent.Pre.class);
    }

    @Override
    public void invoke(MotionEvent.Pre event) {

        // rotate
        if (module.rotate.getValue()) {

            // manipulate packets if process are trying to complete
            if (module.isActive()) {

                // rotate only if we have an interaction vector to rotate to
                if (module.angleVector != null) {

                    // cancel the existing rotations, we'll send our own
                    event.setCanceled(true);

                    // yaw and pitch to the angle vector
                    module.rotateAngles =module. calculateAngles(module.angleVector.first());

                    // yaw step requires slower rotations, so we ease into the target rotation, requires some silly math
                    if (!module.yawStep.getValue().equals(AutoCrystal.YawStep.None)) {

                        // rotation that we have serverside

                        // wrapped yaw value
                        float yaw = ((IEntityPlayerSP) mc.player).getLastReportedYaw();


                        // difference between current and upcoming rotation
                        float angleDifference = module.rotateAngles.first() - yaw;

                        // should never be over 180 since the angles are at max 180 and if it's greater than 180 this means we'll be doing a less than ideal turn
                        // (i.e current = 180, required = -180 -> the turn will be 360 degrees instead of just no turn since 180 and -180 are equivalent)
                        // at worst scenario, current = 90, required = -90 creates a turn of 180 degrees, so this will be our max
                        if (Math.abs(angleDifference) > 180) {

                            // adjust yaw, since this is not the true angle difference until we rotate again
                            float adjust = angleDifference > 0 ? -360 : 360;
                            angleDifference += adjust;
                        }

                        // use absolute angle diff
                        // rotating too fast
                        if (Math.abs(angleDifference) > module.yawStepThreshold.getValue()) {

                            // check if we need to yaw step
                            if (module.yawStep.getValue().equals(AutoCrystal.YawStep.Full) || (module.yawStep.getValue().equals(AutoCrystal.YawStep.Semi) && module.angleVector.second().equals(AutoCrystal.YawStep.Full))) {

                                // ideal rotation direction, so we don't turn in the wrong direction
                                int rotationDirection = angleDifference > 0 ? 1 : -1;

                                // add max angle
                                yaw += module.yawStepThreshold.getValue() * rotationDirection;

                                // update rotation
                                module.rotateAngles = Pair.of(yaw, module.rotateAngles.second());


                                // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                                event.setRotations(module.rotateAngles.first(), module.rotateAngles.second());

                                // we need to wait till we reach our rotation
                                module.rotateTicks++;
                            }
                        } else {


                            // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                            event.setRotations(module.rotateAngles.first(), module.rotateAngles.second());
                        }
                    }

                    // rotate to target instantly
                    else {


                        // add our rotation to our client rotations, AutoCrystal has priority over all other rotations
                        event.setRotations(module.rotateAngles.first(), module.rotateAngles.second());
                    }
                }
            }
        }
    }




}
