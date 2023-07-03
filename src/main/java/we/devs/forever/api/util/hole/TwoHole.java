package we.devs.forever.api.util.hole;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.client.Client;

public class TwoHole extends Client {

    private final BlockPos pos;
    private final BlockPos secondPos;

    public TwoHole(BlockPos pos, BlockPos secondPos) {
        this.pos = pos;
        this.secondPos = secondPos;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockPos getSecondPos() {
        return secondPos;
    }

    public static TwoHole validTwoBlockObiXZ(BlockPos pos) {
        return validTwoBlockObiXZ(pos, false, true);
    }

    public static TwoHole validTwoBlockObiXZ(BlockPos pos, boolean ignoreBedrockCheck, boolean ignoreOtherOffsets) {
        if(pos == null) return null;
        if (
                isObyHole(mc.world.getBlockState(pos.down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.west()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.south()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.north()).getBlock())
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && isObyHole(mc.world.getBlockState(pos.east().down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.east(2)).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.east().south()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.east().north()).getBlock())
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null || ignoreBedrockCheck ? new TwoHole(pos, pos.add(1, 0, 0)) : null;
        } else if (
                isObyHole(mc.world.getBlockState(pos.down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.west()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.east()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.north()).getBlock())
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && isObyHole(mc.world.getBlockState(pos.south().down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.south(2)).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.south().east()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.south().west()).getBlock())
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null || ignoreBedrockCheck ? new TwoHole(pos, pos.add(0, 0, 1)) : null;
        } else if (
                isObyHole(mc.world.getBlockState(pos.down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.east()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.south()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.north()).getBlock())
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && isObyHole(mc.world.getBlockState(pos.west().down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.west(2)).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.west().south()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.west().north()).getBlock())
                        && mc.world.getBlockState(pos.west()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.west().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.west().up(2)).getMaterial() == Material.AIR
                        && !ignoreOtherOffsets
        ) {
            return validTwoBlockBedrockXZ(pos) == null || ignoreBedrockCheck ? new TwoHole(pos, pos.add(-1, 0, 0)) : null;
        } else if (
                isObyHole(mc.world.getBlockState(pos.down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.west()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.east()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.south()).getBlock())
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && isObyHole(mc.world.getBlockState(pos.north().down()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.north(2)).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.north().east()).getBlock())
                        && isObyHole(mc.world.getBlockState(pos.north().west()).getBlock())
                        && mc.world.getBlockState(pos.north()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.north().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.north().up(2)).getMaterial() == Material.AIR
                        && !ignoreOtherOffsets

        ) {
            return validTwoBlockBedrockXZ(pos) == null || ignoreBedrockCheck ? new TwoHole(pos, pos.add(0, 0, -1)) : null;
        }


        return null;
    }

    public static TwoHole validTwoBlockBedrockXZ(BlockPos pos) {
        return validTwoBlockBedrockXZ(pos,true);
    }


    public static TwoHole validTwoBlockBedrockXZ(BlockPos pos, boolean ignoreOtherOffsets) {
        if(pos == null) return null;
        if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return new TwoHole(pos, pos.add(1, 0, 0));
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return new TwoHole(pos, pos.add(0, 0, 1));
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.west().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.west()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.west().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.west().up(2)).getMaterial() == Material.AIR
                        && !ignoreOtherOffsets
        ) {
            return new TwoHole(pos, pos.add(-1, 0, 0));
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.north().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.north()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.north().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.north().up(2)).getMaterial() == Material.AIR
                        && !ignoreOtherOffsets

        ) {
            return new TwoHole(pos, pos.add(0, 0, -1));
        }
        return null;
    }

    public static boolean isObyHole(Block block) {
        return block.equals(Blocks.BEDROCK) || block.equals(Blocks.OBSIDIAN) || block.equals(Blocks.ANVIL);
    }
}
