package we.devs.forever.client.modules.impl.movement;

import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import we.devs.forever.client.modules.api.Module;

public class SafeWalk extends Module {
    public SafeWalk() {
        super("SafeWalk", "Walks safe", Module.Category.MOVEMENT);
    }
    //public Setting<Mode> mode = (new Setting<>("Mode", Mode.Classic));

    @SubscribeEvent
    public void onUpdate() {
        //if (this.mode.getValue() == Mode.Shift) {
            BlockPos pos = new BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ);
            mc.gameSettings.keyBindSneak.pressed = mc.world.getBlockState(pos).getBlock() == Blocks.AIR;
        //}
    }
/*
    @EventListener
    public void onMove(MoveEvent event) {
        if (this.mode.getValue() == Mode.Classic) {
            if (event.getStage() == 0) {
                double x = event.getMotionX();
                double y = event.getMotionY();
                double z = event.getMotionZ();
                if (SafeWalk.mc.player.onGround) {
                    double increment = 0.05;
                    while (x != 0.0 && this.isOffsetBBEmpty(x, -1.0, 0.0)) {
                        if (x < increment && x >= -increment) {
                            x = 0.0;
                            continue;
                        }
                        if (x > 0.0) {
                            x -= increment;
                            continue;
                        }
                        x += increment;
                    }
                    while (z != 0.0 && this.isOffsetBBEmpty(0.0, -1.0, z)) {
                        if (z < increment && z >= -increment) {
                            z = 0.0;
                            continue;
                        }
                        if (z > 0.0) {
                            z -= increment;
                            continue;
                        }
                        z += increment;
                    }
                    while (x != 0.0 && z != 0.0 && this.isOffsetBBEmpty(x, -1.0, z)) {
                        x = x < increment && x >= -increment ? 0.0 : (x > 0.0 ? (x -= increment) : (x += increment));
                        if (z < increment && z >= -increment) {
                            z = 0.0;
                            continue;
                        }
                        if (z > 0.0) {
                            z -= increment;
                            continue;
                        }
                        z += increment;
                    }
                }
                event.getMotionX();
                event.getMotionY();
                event.getMotionZ();
            }
        }
    }

    public boolean isOffsetBBEmpty(double offsetX, double offsetY, double offsetZ) {
        EntityPlayerSP playerSP = SafeWalk.mc.player;
        return SafeWalk.mc.world.getCollisionBoxes(playerSP, playerSP.getEntityBoundingBox().offset(offsetX, offsetY, offsetZ)).isEmpty();
    }

    public enum Mode {
        Classic,
        Shift
    }
 */
}