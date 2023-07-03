package we.devs.forever.client.modules.impl.player.fuckplayer.utils;

import net.minecraft.block.BlockAir;
import net.minecraft.entity.Entity;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.impl.player.fuckplayer.enums.MoveMode;

public class movingPlayer {
    public static int id;
    public static MoveMode type;
    public static double speed;
    public static int direction;
    public static double range;
    public static boolean follow;
    public static int rad;

    public movingPlayer(final int id, final MoveMode type, final double speed, final int direction, final double range, final boolean follow) {
        rad = 0;
        movingPlayer.id = id;
        movingPlayer.type = type;
        movingPlayer.speed = speed;
        movingPlayer.direction = Math.abs(direction);
        movingPlayer.range = range;
        movingPlayer.follow = follow;
    }

    public void move() {
        final Entity player = Util.mc.world.getEntityByID(id);
        if (player != null) {
            switch (type) {
                case Line: {
                    double posX = follow ? Util.mc.player.posX : player.posX;
                    double posY = follow ? Util.mc.player.posY : player.posY;
                    double posZ = follow ? Util.mc.player.posZ : player.posZ;
                    switch (direction) {
                        case 0: {
                            posZ += speed;
                            break;
                        }
                        case 1: {
                            posX -= speed / 2.0;
                            posZ += speed / 2.0;
                            break;
                        }
                        case 2: {
                            posX -= speed / 2.0;
                            break;
                        }
                        case 3: {
                            posZ -= speed / 2.0;
                            posX -= speed / 2.0;
                            break;
                        }
                        case 4: {
                            posZ -= speed;
                            break;
                        }
                        case 5: {
                            posX += speed / 2.0;
                            posZ -= speed / 2.0;
                            break;
                        }
                        case 6: {
                            posX += speed;
                            break;
                        }
                        case 7: {
                            posZ += speed / 2.0;
                            posX += speed / 2.0;
                            break;
                        }
                    }
                    if (BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir) {
                        for (int i = 0; i < 5 && BlockUtil.getBlock(posX, posY - 1.0, posZ) instanceof BlockAir; --posY, ++i) {
                        }
                    } else {
                        for (int i = 0; i < 5 && !(BlockUtil.getBlock(posX, posY, posZ) instanceof BlockAir); ++posY, ++i) {
                        }
                    }
                    player.setPositionAndUpdate(posX, posY, posZ);
                    break;
                }
                case Circle: {
                    final double posXCir = Math.cos(rad / 100.0) * range + Util.mc.player.posX;
                    final double posZCir = Math.sin(rad / 100.0) * range + Util.mc.player.posZ;
                    double posYCir = Util.mc.player.posY;
                    if (BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir) {
                        for (int j = 0; j < 5 && BlockUtil.getBlock(posXCir, posYCir - 1.0, posZCir) instanceof BlockAir; --posYCir, ++j) {
                        }
                    } else {
                        for (int j = 0; j < 5 && !(BlockUtil.getBlock(posXCir, posYCir, posZCir) instanceof BlockAir); ++posYCir, ++j) {
                        }
                    }
                    player.setPositionAndUpdate(posXCir, posYCir, posZCir);
                    rad += (int) (speed * 10.0);
                    break;
                }
            }
        }
    }
}
