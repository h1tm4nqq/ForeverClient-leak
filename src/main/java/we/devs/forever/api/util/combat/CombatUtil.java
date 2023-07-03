package we.devs.forever.api.util.combat;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.entity.EntityUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public
class CombatUtil implements Util {
    public static EntityPlayer getTarget(float range) {
        EntityPlayer currentTarget = null;
        int size = CombatUtil.mc.world.playerEntities.size();
        for (int i = 0; i < size; ++i) {
            EntityPlayer player = CombatUtil.mc.world.playerEntities.get(i);
            if (EntityUtil.isntValid(player, range)) continue;
            if (currentTarget == null) {
                currentTarget = player;
                continue;
            }
            if (!(CombatUtil.mc.player.getDistanceSq(player) < CombatUtil.mc.player.getDistanceSq(currentTarget)))
                continue;
            currentTarget = player;
        }
        return currentTarget;
    }

    public static List<BlockPos> getSphere(float f, boolean bl, boolean bl2) {
        float range = f;
        ArrayList<BlockPos> blocks = new ArrayList<>();
        int x = CombatUtil.mc.player.getPosition().getX() - (int) range;
        while ((float) x <= (float) CombatUtil.mc.player.getPosition().getX() + range) {
            int z = CombatUtil.mc.player.getPosition().getZ() - (int) range;
            while ((float) z <= (float) CombatUtil.mc.player.getPosition().getZ() + range) {
                boolean sphere = bl;
                int y = sphere ? CombatUtil.mc.player.getPosition().getY() - (int) range : CombatUtil.mc.player.getPosition().getY();
                int n = y;
                while ((float) y < (float) CombatUtil.mc.player.getPosition().getY() + range) {
                    boolean hollow = bl2;
                    double distance = (CombatUtil.mc.player.getPosition().getX() - x) * (CombatUtil.mc.player.getPosition().getX() - x) + (CombatUtil.mc.player.getPosition().getZ() - z) * (CombatUtil.mc.player.getPosition().getZ() - z) + (sphere ? (CombatUtil.mc.player.getPosition().getY() - y) * (CombatUtil.mc.player.getPosition().getY() - y) : 0);
                    if (distance < (double) (range * range) && (!hollow || distance >= ((double) range - Double.longBitsToDouble(Double.doubleToLongBits(638.4060856917202) ^ 0x7F73F33FA9DAEA7FL)) * ((double) range - Double.longBitsToDouble(Double.doubleToLongBits(13.015128470890444) ^ 0x7FDA07BEEB3F6D07L)))) {
                        blocks.add(new BlockPos(x, y, z));
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return blocks;
    }


    public static boolean isInHole(EntityPlayer entity) {
        return CombatUtil.isBlockValid(new BlockPos(entity.posX, entity.posY, entity.posZ));
    }

    public static boolean isBlockValid(BlockPos blockPos) {
        return CombatUtil.isBedrockHole(blockPos) || CombatUtil.isObbyHole(blockPos) || CombatUtil.isBothHole(blockPos);
    }

    public static EnumFacing getCorrectEnumFacing(EntityPlayer target) {
        boolean ableToRunOnCurrentFacing = true;
        EnumFacing correctEnumFacing = null;
        EnumFacing[] aenumfacing = EnumFacing.values();
        int i = aenumfacing.length;

        for (int j = 0; j < i; ++j) {
            EnumFacing facing = aenumfacing[j];
            BlockPos posToCheck = target.getPosition().offset(facing).add(0, 1, 0);
            BlockPos posToCheck1 = target.getPosition().offset(facing).offset(facing).add(0, 1, 0);
            BlockPos posToCheck2 = target.getPosition().offset(facing).offset(facing).offset(facing).add(0, 1, 0);

            if (!CombatUtil.mc.world.getBlockState(posToCheck).getBlock().equals(Blocks.AIR) || !CombatUtil.mc.world.getBlockState(posToCheck1).getBlock().equals(Blocks.AIR) || !CombatUtil.mc.world.getBlockState(posToCheck2).getBlock().equals(Blocks.AIR)) {
                ableToRunOnCurrentFacing = false;
            }

            if (ableToRunOnCurrentFacing) {
                correctEnumFacing = facing;
            }
        }

        return correctEnumFacing;
    }

    public static List<EntityPlayer> getPlayersSorted(float range) {
        if (CombatUtil.mc.world != null && CombatUtil.mc.player != null) {

            synchronized (CombatUtil.mc.world.playerEntities) {
                List<EntityPlayer> playerList = new ArrayList();

                for (EntityPlayer player : CombatUtil.mc.world.playerEntities) {
                    if (CombatUtil.mc.player != player && CombatUtil.mc.player.getDistance(player) <= range) {
                        playerList.add(player);
                    }
                }

                playerList.sort(Comparator.comparing((eP) -> CombatUtil.mc.player.getDistance((Entity) eP)));
                return playerList;
            }
        } else {
            return null;
        }
    }

    public static int isInHoleInt(EntityPlayer entity) {
        BlockPos playerPos = new BlockPos(entity.getPositionVector());
        if (CombatUtil.isBedrockHole(playerPos)) {
            return 1;
        }
        if (CombatUtil.isObbyHole(playerPos) || CombatUtil.isBothHole(playerPos)) {
            return 2;
        }
        return 0;
    }

    public static boolean isObbyHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks;
        for (BlockPos pos : touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()}) {
            IBlockState touchingState = CombatUtil.mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.OBSIDIAN) continue;
            return false;
        }
        return true;
    }

    public static boolean isBedrockHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks;
        for (BlockPos pos : touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()}) {
            IBlockState touchingState = CombatUtil.mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.BEDROCK) continue;
            return false;
        }
        return true;
    }

    public static boolean isBothHole(BlockPos blockPos) {
        BlockPos[] touchingBlocks;
        for (BlockPos pos : touchingBlocks = new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()}) {
            IBlockState touchingState = CombatUtil.mc.world.getBlockState(pos);
            if (touchingState.getBlock() == Blocks.BEDROCK || touchingState.getBlock() == Blocks.OBSIDIAN)
                continue;
            return false;
        }
        return true;
    }
}
