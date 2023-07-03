package we.devs.forever.api.util.world;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.GameType;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.impl.player.BlockTweaks;
import we.devs.forever.mixin.mixins.accessor.IEntityLivingBase;
import we.devs.forever.mixin.mixins.accessor.IMinecraft;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class BlockUtil extends Client implements Util {

    public static final List<Block> blackList = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL,
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR,
            Blocks.ENCHANTING_TABLE
    );
    public static final List<Block> shulkerList = Arrays.asList(
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
    );
    public static final List<Block> shiftBlocks = Arrays.asList(
            Blocks.ENDER_CHEST,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.CRAFTING_TABLE,
            Blocks.ANVIL, // :troll:
            Blocks.BREWING_STAND,
            Blocks.HOPPER,
            Blocks.DROPPER,
            Blocks.DISPENSER,
            Blocks.TRAPDOOR,
            Blocks.ENCHANTING_TABLE,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
            // Blocks.COMMAND_BLOCK,
            // Blocks.CHAIN_COMMAND_BLOCK
    );
    public static List<Block> unSolidBlocks = Arrays.asList(
            Blocks.FLOWING_LAVA,
            Blocks.FLOWER_POT,
            Blocks.SNOW,
            Blocks.CARPET,
            Blocks.END_ROD,
            Blocks.SKULL,
            Blocks.FLOWER_POT,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.WOODEN_BUTTON,
            Blocks.LEVER,
            Blocks.STONE_BUTTON,
            Blocks.LADDER,
            Blocks.UNPOWERED_COMPARATOR,
            Blocks.POWERED_COMPARATOR,
            Blocks.UNPOWERED_REPEATER,
            Blocks.POWERED_REPEATER,
            Blocks.UNLIT_REDSTONE_TORCH,
            Blocks.REDSTONE_TORCH,
            Blocks.REDSTONE_WIRE,
            Blocks.AIR,
            Blocks.PORTAL,
            Blocks.END_PORTAL,
            Blocks.WATER,
            Blocks.FLOWING_WATER,
            Blocks.LAVA,
            Blocks.FLOWING_LAVA,
            Blocks.SAPLING,
            Blocks.RED_FLOWER,
            Blocks.YELLOW_FLOWER,
            Blocks.BROWN_MUSHROOM,
            Blocks.RED_MUSHROOM,
            Blocks.WHEAT,
            Blocks.CARROTS,
            Blocks.POTATOES,
            Blocks.BEETROOTS,
            Blocks.REEDS,
            Blocks.PUMPKIN_STEM,
            Blocks.MELON_STEM,
            Blocks.WATERLILY,
            Blocks.NETHER_WART,
            Blocks.COCOA,
            Blocks.CHORUS_FLOWER,
            Blocks.CHORUS_PLANT,
            Blocks.TALLGRASS,
            Blocks.DEADBUSH,
            Blocks.VINE,
            Blocks.FIRE,
            Blocks.RAIL,
            Blocks.ACTIVATOR_RAIL,
            Blocks.DETECTOR_RAIL,
            Blocks.GOLDEN_RAIL,
            Blocks.TORCH,
            Blocks.WHITE_SHULKER_BOX,
            Blocks.ORANGE_SHULKER_BOX,
            Blocks.MAGENTA_SHULKER_BOX,
            Blocks.LIGHT_BLUE_SHULKER_BOX,
            Blocks.YELLOW_SHULKER_BOX,
            Blocks.LIME_SHULKER_BOX,
            Blocks.PINK_SHULKER_BOX,
            Blocks.GRAY_SHULKER_BOX,
            Blocks.SILVER_SHULKER_BOX,
            Blocks.CYAN_SHULKER_BOX,
            Blocks.PURPLE_SHULKER_BOX,
            Blocks.BLUE_SHULKER_BOX,
            Blocks.BROWN_SHULKER_BOX,
            Blocks.GREEN_SHULKER_BOX,
            Blocks.RED_SHULKER_BOX,
            Blocks.BLACK_SHULKER_BOX
    );

    public static List<Block> empty = Arrays.asList(
            Blocks.AIR,
            Blocks.VINE,
            Blocks.SNOW_LAYER,
            Blocks.TALLGRASS,
            Blocks.FIRE,
            Blocks.LAVA,
            Blocks.FLOWING_LAVA,
            Blocks.FLOWING_WATER,
            Blocks.WATER
    );
    public static List<BlockPos> getBlocksInArea(EntityPlayer player, AxisAlignedBB area) {
        if (player != null) {

            // list of nearby blocks
            List<BlockPos> blocks = new ArrayList<>();

            // iterate through all surrounding blocks
            for (double x = StrictMath.floor(area.minX); x <= StrictMath.ceil(area.maxX); x++) {
                for (double y = StrictMath.floor(area.minY); y <= StrictMath.ceil(area.maxY); y++) {
                    for (double z = StrictMath.floor(area.minZ); z <= StrictMath.ceil(area.maxZ); z++) {

                        // the current position
                        BlockPos position = PlayerUtil.getPosition().add(x, y, z);

                        // check distance to block
                        if (getDistanceToCenter(player, position) >= area.maxX) {
                            continue;
                        }

                        // add the block to our list
                        blocks.add(position);
                    }
                }
            }

            return blocks;
        }

        // rofl, threading is so funny
        return new ArrayList<>();
    }
    public static double getDistanceToCenter(EntityPlayer player, BlockPos in) {

        // distances
        double dX = in.getX() + 0.5 - player.posX;
        double dY = in.getY() + 0.5 - player.posY;
        double dZ = in.getZ() + 0.5 - player.posZ;

        // distance to center
        return StrictMath.sqrt((dX * dX) + (dY * dY) + (dZ * dZ));
    }

    public static void placeBlock(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        for (EnumFacing side : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(side);
            EnumFacing side2 = side.getOpposite();
            if (canBeClicked(neighbor)) {
                Vec3d hitVec = (new Vec3d(neighbor)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(side2.getDirectionVec())).scale(0.5D));
                if (eyesPos.squareDistanceTo(hitVec) <= 18.0625D) {
                    faceVectorPacketInstant(hitVec);
                    processRightClickBlock(neighbor, side2, hitVec);
                    mc.player.swingArm(EnumHand.MAIN_HAND);
                    mc.rightClickDelayTimer = 4;

                    return;
                }
            }
        }
    }

    public static Vec3d getEyesPos() {
        return new Vec3d((mc.player).posX,
                (mc.player).posY + mc.player.getEyeHeight(),
                (mc.player).posZ);
    }

    public static boolean hasNeighbour(final BlockPos blockPos) {
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbour = blockPos.offset(side);
            if (!BlockUtil.mc.world.getBlockState(neighbour).getMaterial().isReplaceable() && !BlockUtil.blackList.contains(getBlock(neighbour))) {
                return true;
            }
        }
        return false;
    }

    private static float[] getLegitRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();
        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;
        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));
        return new float[]{(mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw)), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)}; // test
    }

    private static void processRightClickBlock(BlockPos pos, EnumFacing side, Vec3d hitVec) {
        mc.playerController.processRightClickBlock(mc.player, mc.world, pos, side, hitVec, EnumHand.MAIN_HAND);
    }

    public static void faceVectorPacketInstant(Vec3d vec) {
        float[] rotations = getLegitRotations(vec);
        (mc.player).connection.sendPacket(new CPacketPlayer.Rotation(rotations[0], rotations[1],
                (mc.player).onGround));
    }

    public static void placeBlockScaffold(final BlockPos pos) {
        final Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        for (final EnumFacing side : EnumFacing.values()) {
            final BlockPos neighbor = pos.offset(side);
            final EnumFacing side2 = side.getOpposite();
            final Vec3d hitVec;
            if (canBeClicked(neighbor) && eyesPos.squareDistanceTo(hitVec = new Vec3d(neighbor).add(0.5, 0.5, 0.5).add(new Vec3d(side2.getDirectionVec()).scale(0.5))) <= 18.0625) {
                faceVectorPacketInstant(hitVec);
                processRightClickBlock(neighbor, side2, hitVec);
                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.rightClickDelayTimer = 4;
                return;
            }
        }
    }

    public static boolean isIntercepted(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (!new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) continue;
            return true;
        }
        return false;
    }


    public static List<EnumFacing> getPossibleSides(BlockPos pos, boolean strict) {
        List<EnumFacing> facings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            if (strict && !BlockUtil.getVisibleSides(pos).contains(side.getOpposite())) {
                continue;
            }
            BlockPos neighbour = pos.offset(side);
            if (mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
                IBlockState blockState = mc.world.getBlockState(neighbour);
                if (!blockState.getMaterial().isReplaceable()) {
                    facings.add(side);
                }
            }
        }
        return facings;
    }
    public static EnumFacing getFirstFacing(BlockPos pos) {
        return getFirstFacing(pos,false);
    }
    public static EnumFacing getFirstFacing(BlockPos pos, boolean strict) {
        for (EnumFacing facing : getPossibleSides(pos,strict)) {
            return facing;
        }
        return null;
    }


    public static int isPositionPlaceable(BlockPos pos, boolean rayTrace) {
        return isPositionPlaceable(pos, rayTrace, true);
    }

    public static int isPositionPlaceable(BlockPos pos, boolean rayTrace, boolean entityCheck) {
        Block block = mc.world.getBlockState(pos).getBlock();
        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFire) && !(block instanceof BlockDeadBush) && !(block instanceof BlockSnow)) {
            return 0;
        }

        if (!rayTracePlaceCheck(pos, rayTrace, 0.0f)) {
            return -1;
        }

        if (entityCheck) {
            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos))) {
                if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityEnderCrystal)) {
                    return 1;
                }
            }
        }

        for (EnumFacing side : getPossibleSides(pos,false)) {
            if (canBeClicked(pos.offset(side))) {
                return 3;
            }
        }

        return 2;
    }

    public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
        if (pos == null) return;
        if (packet) {
            float f = (float) (vec.x - (double) pos.getX());
            float f1 = (float) (vec.y - (double) pos.getY());
            float f2 = (float) (vec.z - (double) pos.getZ());
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, direction, vec, hand);
        }
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4; //?
    }


    public static boolean placeBlock(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking) {
        boolean sneaking = false;
        EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return isSneaking;
        }

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();

        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
            sneaking = true;
        }

        if (rotate) {
            Client.rotationManager.doRotation(RotationType.Packet, hitVec,  11);
        }

        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4; //?
        return sneaking || isSneaking;
    }

    public static boolean placeBlockSmartRotate(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean isSneaking) {
        boolean sneaking = false;
        EnumFacing side = getFirstFacing(pos);
        assert side != null;
        Command.sendMessage(side.toString());

        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();

        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();

        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            sneaking = true;
        }

        if (rotate) {
            Client.rotationManager.doRotation(RotationType.Packet, hitVec, 9);
        }

        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.player.swingArm(EnumHand.MAIN_HAND);
        mc.rightClickDelayTimer = 4; //?
        return sneaking || isSneaking;
    }


    public static Vec3d[] getHelpingBlocks(Vec3d vec3d) {
        return new Vec3d[]{
                new Vec3d(vec3d.x, vec3d.y - 1, vec3d.z),
                new Vec3d(vec3d.x != 0 ? vec3d.x * 2 : vec3d.x, vec3d.y, vec3d.x != 0 ? vec3d.z : vec3d.z * 2),
                new Vec3d(vec3d.x == 0 ? vec3d.x + 1 : vec3d.x, vec3d.y, vec3d.x == 0 ? vec3d.z : vec3d.z + 1),
                new Vec3d(vec3d.x == 0 ? vec3d.x - 1 : vec3d.x, vec3d.y, vec3d.x == 0 ? vec3d.z : vec3d.z - 1),
                new Vec3d(vec3d.x, vec3d.y + 1, vec3d.z)
        };
    }

//    public static boolean canPlaceBlock(BlockPos pos) {
//        EnumFacing facing = getFacing(pos.up());
//        boolean b = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0;
//
//        return b
//               && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.up())).isEmpty()
//                        && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.offset(facing))).isEmpty()
//                        && mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.offset(facing).up())).isEmpty();
//    }

    public static EnumFacing getFacing(BlockPos pos) {
        Vec3d eyesPos = new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ);
        Vec3d hitVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(EnumFacing.DOWN.getDirectionVec()).scale(0.5));
        double diffX = hitVec.x - eyesPos.x;
        double diffZ = hitVec.z - eyesPos.z;
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float yaw2 = mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw);
        if (Math.abs(mc.player.posX - (double) ((float) pos.getX() + 0.5F)) < 2.0D && Math.abs(mc.player.posZ - (double) ((float) pos
                .getZ() + 0.5F)) < 2.0D) {
            double d0 = mc.player.posY + (double) mc.player.getEyeHeight();
            if (d0 - (double) pos.getY() > 2.0D) {
                return EnumFacing.UP;
            }
            if ((double) pos.getY() - d0 > 0.0D) {
                return EnumFacing.DOWN;
            }
        }
        return EnumFacing.byHorizontalIndex(MathHelper.floor((double) (yaw2 * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
    }

    public static boolean canPlace(BlockPos pos, Block block, boolean checkEntity) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos blockPos = pos.offset(facing);
            Block posBlock = mc.world.getBlockState(blockPos).getBlock();
            if (posBlock != Blocks.AIR && !(posBlock instanceof BlockLiquid) && block.canPlaceBlockAt(mc.world, pos)) {
                if (checkEntity) {
                    if (mc.world.checkNoEntityCollision(new AxisAlignedBB(pos))) {
                        return true;
                    }
                } else return true;
            }
        }
        return false;
    }

    public static boolean canPlace(BlockPos pos, boolean checkEntity) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos blockPos = pos.offset(facing);
            Block posBlock = mc.world.getBlockState(blockPos).getBlock();
            if (posBlock != Blocks.AIR && !(posBlock instanceof BlockLiquid)) {
                if (checkEntity) {
                    if (mc.world.checkNoEntityCollision(new AxisAlignedBB(pos))) {
                        return true;
                    }
                } else return true;
            }
        }
        return false;
    }

    public static List<BlockPos> possiblePlacePositions(final float placeRange, final boolean specialEntityCheck, final boolean oneDot15) {
        final NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(EntityUtil.getPlayerPos(mc.player), placeRange, (int) placeRange, false, true, 0).stream().filter(pos -> canPlaceCrystal(pos, specialEntityCheck, oneDot15)).collect(Collectors.toList()));
        return positions;
    }

    public static List<BlockPos> possiblePlacePositions(final float placeRange, final float placeRangeWall, final boolean specialEntityCheck, final boolean oneDot15) {
        final NonNullList<BlockPos> positions = NonNullList.create();
        positions.addAll(getSphere(EntityUtil.getPlayerPos(mc.player), placeRange, (int) placeRange, false, true, 0).parallelStream()
                .filter(pos -> canPlaceCrysta1l(pos, oneDot15, specialEntityCheck))
                .filter(pos -> mc.player.getDistanceSq(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5) <= (canSee(pos) ? placeRange * placeRange : placeRangeWall * placeRangeWall))
                .collect(Collectors.toList()));
        return positions;
    }

    public static boolean canPlaceCrysta1l(BlockPos blockPos, boolean check, boolean entity) {
        if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
            return false;
        }
        BlockPos boost = blockPos.add(0, 1, 0);
        if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR || mc.world.getBlockState(blockPos.add(0, 2, 0)).getBlock() != Blocks.AIR) {
            return false;
        }
        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() + 1, boost.getY() + (check ? 2 : 1), boost.getZ() + 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0/* &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() - 1, boost.getY() + (check ? 2 : 1), boost.getZ() + 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0 &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() + 1, boost.getY() + (check ? 2 : 1), boost.getZ() - 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0 &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() - 1, boost.getY() + (check ? 2 : 1), boost.getZ() - 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0 &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() + 1, boost.getY() + (check ? 2 : 1), boost.getZ() ), e -> !(e instanceof EntityEnderCrystal)).size() == 0 &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() - 1, boost.getY() + (check ? 2 : 1), boost.getZ()), e -> !(e instanceof EntityEnderCrystal)).size() == 0 &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX() , boost.getY() + (check ? 2 : 1), boost.getZ() + 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0 &&
                mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost.getX(), boost.getY(), boost.getZ(), boost.getX(), boost.getY() + (check ? 2 : 1), boost.getZ() - 1), e -> !(e instanceof EntityEnderCrystal)).size() == 0*/;
    }

    public static boolean canSee(BlockPos blockPos) {
        return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 1.5, blockPos.getZ() + 0.5), false, true, false) == null;
    }

    public static List<BlockPos> getSphere(float radius, boolean ignoreAir) {
        ArrayList<BlockPos> sphere = new ArrayList<BlockPos>();
        BlockPos pos = new BlockPos(mc.player.getPositionVector());
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        int radiuss = (int) radius;
        int x = posX - radiuss;
        while ((float) x <= (float) posX + radius) {
            int z = posZ - radiuss;
            while ((float) z <= (float) posZ + radius) {
                int y = posY - radiuss;
                while ((float) y < (float) posY + radius) {
                    BlockPos position;
                    double dist = (posX - x) * (posX - x) + (posZ - z) * (posZ - z) + (posY - y) * (posY - y);
                    if (dist < (double) (radius * radius) && (mc.world.getBlockState(position = new BlockPos(x, y, z)).getBlock() != Blocks.AIR || !ignoreAir)) {
                        sphere.add(position);
                    }
                    ++y;
                }
                ++z;
            }
            ++x;
        }
        return sphere;
    }

    public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = pos.getX();
        int cy = pos.getY();
        int cz = pos.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++) {
            for (int z = cz - (int) r; z <= cz + r; z++) {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
                        BlockPos l = new BlockPos(x, y + plus_y, z);
                        circleblocks.add(l);
                    }
                }
            }
        }
        return circleblocks;
    }

    public static boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            return (mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK
                    || mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN)
                    && mc.world.getBlockState(boost).getBlock() == Blocks.AIR
                    && mc.world.getBlockState(boost2).getBlock() == Blocks.AIR
                    && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty()
                    && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    public static void placeBlockss(BlockPos blockPos2, Swing swing, boolean packet, boolean rotate) {
        for (EnumFacing enumFacing : EnumFacing.values()) {
            if (mc.world.getBlockState(blockPos2.offset(enumFacing)).getBlock().equals(Blocks.AIR) || isIntercepted(blockPos2))
                continue;
            if (packet) {
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(blockPos2.offset(enumFacing), enumFacing.getOpposite(), EnumHand.MAIN_HAND, Float.intBitsToFloat(Float.floatToIntBits(2.7f)), Float.intBitsToFloat(Float.floatToIntBits(3.8f)), Float.intBitsToFloat(Float.floatToIntBits(30.0f))));
            } else {
                mc.playerController.processRightClickBlock(mc.player, mc.world, blockPos2.offset(enumFacing), enumFacing.getOpposite(), new Vec3d(blockPos2), EnumHand.MAIN_HAND);
            }
            if (rotate) {
                RotationUtil.faceVector(new Vec3d(blockPos2), true);
            }
            return;
        }
    }

    public static boolean canPlaceCrystal(final BlockPos blockPos, final boolean cc, final boolean oneDot15) {
        final BlockPos boost = blockPos.add(0, 1, 0);
        final BlockPos boost2 = blockPos.add(0, 2, 0);
        try {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
                return false;
            }
            if ((mc.world.getBlockState(boost).getBlock() != Blocks.AIR || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) && !oneDot15) {
                return false;
            }
            if (!cc) {
                return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && (oneDot15 || mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty());
            }
            for (final Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost))) {
                if (entity.isDead || !(entity instanceof EntityEnderCrystal || entity instanceof EntityPlayer))
                    continue;
                return false;
            }
            if (!oneDot15) {
                for (final Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2))) {
                    if (entity.isDead || entity instanceof EntityEnderCrystal) continue;
                    return false;
                }
            }
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public static boolean canPlace(BlockPos pos) {
        for (EnumFacing side : BlockUtil.getPossibleSides(pos,false)) {
            if (BlockUtil.canBeClicked(pos.offset(side))) {
                return true;
            }
        }
        return false;
    }
    public static boolean checkEntity(BlockPos pos) {
        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos, pos.add(1, 1, 1))).stream()
                .filter(Objects::nonNull)
                .filter(entity -> !entity.isDead).allMatch(entity -> entity instanceof EntityXPOrb || entity instanceof EntityItem || entity instanceof EntityArrow || entity instanceof EntityEnderCrystal);

    }
        public static boolean canBeClicked(BlockPos pos) {
        return getBlock(pos).canCollideCheck(getState(pos), false);
    }

    public static Block getBlock(BlockPos pos) {
        return getState(pos).getBlock();
    }

    private static IBlockState getState(BlockPos pos) {
        return mc.world.getBlockState(pos);
    }

    public static boolean isBlockAboveEntitySolid(Entity entity) {
        if (entity != null) {
            final BlockPos pos = new BlockPos(entity.posX, entity.posY + 2.0, entity.posZ);
            return isBlockSolid(pos);
        }
        return false;
    }


    public static BlockPos[] toBlockPos(Vec3d[] vec3ds) {
        BlockPos[] list = new BlockPos[vec3ds.length];
        for (int i = 0; i < vec3ds.length; i++) {
            list[i] = new BlockPos(vec3ds[i]);
        }
        return list;
    }


    public static boolean isBlockSolid(BlockPos pos) {
        return !isBlockUnSolid(pos);
    }

    public static boolean isBlockUnSolid(BlockPos pos) {
        return isBlockUnSolid(mc.world.getBlockState(pos).getBlock());
    }

    public static boolean isBlockUnSolid(Block block) {
        return unSolidBlocks.contains(block);
    }

    public static Vec3d[] convertVec3ds(Vec3d vec3d, Vec3d[] input) {
        Vec3d[] output = new Vec3d[input.length];
        for (int i = 0; i < input.length; i++) {
            output[i] = vec3d.add(input[i]);
        }
        return output;
    }


    @SuppressWarnings("deprecation")
    public static boolean isValidBlock(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();
        return !(block instanceof BlockLiquid) && block.getMaterial(null) != Material.AIR;
    }

    public static boolean isScaffoldPos(BlockPos pos) {
        return mc.world.isAirBlock(pos)
                || mc.world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER
                || mc.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS
                || mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid;
    }

    public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
        return !shouldCheck || mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double) mc.player.getEyeHeight(), mc.player.posZ), new Vec3d(pos.getX(), pos.getY() + height, pos.getZ()), false, true, false) == null;
    }


    public static boolean isBothHole(BlockPos blockPos) {
        for (BlockPos pos : getTouchingBlocks(blockPos)) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && (touchingState.getBlock() == Blocks.BEDROCK || touchingState.getBlock() == Blocks.OBSIDIAN))
                continue;
            return false;
        }
        return true;
    }

    public static BlockPos[] getTouchingBlocks(BlockPos blockPos) {
        return new BlockPos[]{blockPos.north(), blockPos.south(), blockPos.east(), blockPos.west(), blockPos.down()};
    }


    public static void placeBlockNotRetarded(BlockPos pos, EnumHand hand, boolean rotate, boolean packet) {
        EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        if (!mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            mc.player.setSneaking(true);
        }
        if (rotate) {
            rotationManager.doRotation(RotationType.Packet, hitVec, 11);
        }
        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        mc.player.swingArm(EnumHand.MAIN_HAND);
    }

    public static void placeBlockNotRetarded(BlockPos pos, EnumHand hand, boolean rotate, boolean packet, boolean verify) {
        EnumFacing side = getFirstFacing(pos);
        if (side == null) {
            return;
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        Vec3d hitVec = new Vec3d(neighbour).add(0.5, 0.5, 0.5).add(new Vec3d(opposite.getDirectionVec()).scale(0.5));
        Block neighbourBlock = mc.world.getBlockState(neighbour).getBlock();
        float f = (float) (hitVec.x - (double) pos.getX());
        float f1 = (float) (hitVec.y - (double) pos.getY());
        float f2 = (float) (hitVec.z - (double) pos.getZ());
//        if (verify) BlockUtil.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(neighbour, opposite, hand, f, f1, f2));
        if (!BlockUtil.mc.player.isSneaking() && (blackList.contains(neighbourBlock) || shulkerList.contains(neighbourBlock))) {
            BlockUtil.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtil.mc.player, CPacketEntityAction.Action.START_SNEAKING));
            BlockUtil.mc.player.setSneaking(true);
        }
        if (rotate) {
            rotationManager.doRotation(RotationType.Packet, hitVec, 11);
        }
        if (verify)
            BlockUtil.placeClient(neighbour, hand, opposite, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z);
        BlockUtil.mc.playerController.processRightClickBlock(BlockUtil.mc.player, BlockUtil.mc.world, neighbour, opposite, hitVec, hand);
        rightClickBlock(neighbour, hitVec, hand, opposite, packet);
        BlockUtil.mc.playerController.processRightClickBlock(BlockUtil.mc.player, BlockUtil.mc.world, neighbour, opposite, hitVec, hand);
        BlockUtil.mc.player.connection.sendPacket(new CPacketEntityAction(BlockUtil.mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
        mc.player.swingArm(EnumHand.MAIN_HAND);
//        mc.playerController.clickBlock(pos, opposite);
        if (verify)
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, pos, EnumFacing.DOWN));
    }


    public static void placeClient(BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = BlockUtil.mc.player.getHeldItemMainhand();
        if (stack.getItem() instanceof ItemBlock) {
            int i;
            IBlockState placeState;
            ItemBlock itemBlock = (ItemBlock) stack.getItem();
            Block block = itemBlock.getBlock();
            IBlockState iblockstate = BlockUtil.mc.world.getBlockState(pos);
            Block iBlock = iblockstate.getBlock();
            if (!iBlock.isReplaceable(BlockUtil.mc.world, pos)) {
                pos = pos.offset(facing);
            }
            if (!stack.isEmpty() && BlockUtil.mc.player.canPlayerEdit(pos, facing, stack) && BlockUtil.mc.world.mayPlace(block, pos, false, facing, null) && itemBlock.placeBlockAt(stack, BlockUtil.mc.player, BlockUtil.mc.world, pos, facing, hitX, hitY, hitZ, placeState = block.getStateForPlacement(BlockUtil.mc.world, pos, facing, hitX, hitY, hitZ, i = itemBlock.getMetadata(stack.getMetadata()), BlockUtil.mc.player, hand))) {
                placeState = BlockUtil.mc.world.getBlockState(pos);
                SoundType soundtype = placeState.getBlock().getSoundType(placeState, BlockUtil.mc.world, pos, BlockUtil.mc.player);
                BlockUtil.mc.world.playSound(BlockUtil.mc.player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0f) / 2.0f, soundtype.getPitch() * 0.8f);
                if (!BlockUtil.mc.player.isCreative()) {
                    stack.shrink(1);
                }
            }
        }
    }


    public static void placeBlock(BlockPos position, boolean rotate, boolean strict, boolean packet, boolean verify/*, List<Class<? extends Entity>> safeEntities*/) {
        for (EnumFacing direction : EnumFacing.values()) {

            // find a block to place against
            BlockPos directionOffset = position.offset(direction);

            // the opposite facing value
            EnumFacing oppositeFacing = direction.getOpposite();

            // make sure the side is visible, strict NCP flags for non-visible interactions
            if (strict && !getVisibleSides(directionOffset).contains(direction.getOpposite())) {
                continue;
            }

            // make sure there is no entity on the block
//            for (Entity entity : mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(position))) {
//                if (!safeEntities.contains(entity.getClass())) {
//                    return;
//                }
//            }

            // make sure the offset is empty
            if (mc.world.getBlockState(directionOffset).getMaterial().isReplaceable()) {
                continue;
            }

//            placing = true;

            // stop sprinting before preforming actions
            boolean sprint = mc.player.isSprinting();
            if (sprint) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                mc.player.setSprinting(false);
            }

            // sneak if the block is not right-clickable
            boolean sneak = shiftBlocks.contains(mc.world.getBlockState(directionOffset).getBlock()) && !mc.player.isSneaking();
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                // mc.player.setSneaking(true);
            }

            // our rotation
            float[] rotation = RotationUtil.getAnglesToBlock(directionOffset, oppositeFacing);

            // vector to the block
            Vec3d interactVector = null; //= new Vec3d(directionOffset).addVector(0.5, 0.5, 0.5).add(new Vec3d(direction.getOpposite().getDirectionVec()).scale(0.5));

            if (strict) {
                RayTraceResult result = getTraceResult(getReachDistance(), rotation);
                if (result != null && result.typeOfHit.equals(RayTraceResult.Type.BLOCK)) {
                    interactVector = result.hitVec;
                }
            }

            if (interactVector == null) {
                interactVector = new Vec3d(directionOffset).add(0.5, 0.5, 0.5);
            }

            // Rotation oldRotation = getCosmos().getRotationManager().getServerRotation();

            // rotate to block
            if (rotate) {
//                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation[0], rotation[1], mc.player.onGround));

//                rotationManager.updateRotations(rotation[0], rotation[1]);
                rotationManager.lookAt(rotation[0], rotation[1], false, true);
                // rotate via packet, server should confirm instantly?
//                switch (rotate) {
//                    case CLIENT:
//                        mc.player.rotationYaw = rotation[0];
//                        mc.player.rotationYawHead = rotation[0];
//                        mc.player.rotationPitch = rotation[1];
//                        break;
//                    case PACKET:
//
//                        // force a rotation - should this be done?
//                        mc.player.connection.sendPacket(new CPacketPlayer.Rotation(rotation[0], rotation[1], mc.player.onGround));
//
//                        // submit to rotation manager
//                        // getCosmos().getRotationManager().setRotation(blockAngles);
//
//                        // ((IEntityPlayerSP) mc.player).setLastReportedYaw(blockAngles[0]);
//                        // ((IEntityPlayerSP) mc.player).setLastReportedPitch(blockAngles[1]);
//                        break;
//                }
            }

            float facingX = (float) (interactVector.x - directionOffset.getZ());
            float facingY = (float) (interactVector.y - directionOffset.getY());
            float facingZ = (float) (interactVector.z - directionOffset.getZ());

            // sync item
            //((IPlayerControllerMP) mc.playerController).hookSyncCurrentPlayItem();

            // ip
            String ip = mc.getCurrentServerData() != null ? mc.getCurrentServerData().serverIP : "";

            // right click direction offset block
            if (!packet) {

                // send our place packet
                // todo: fuckery with playerController
                //mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(directionOffset, direction.getOpposite(), EnumHand.MAIN_HAND, facingX, facingY, facingZ));
                mc.playerController.processRightClickBlock(
                        mc.player,
                        mc.world,
                        directionOffset,
                        direction.getOpposite(),
                        interactVector,
                        EnumHand.MAIN_HAND
                );
//                if (verify) placeClient(directionOffset, EnumHand.MAIN_HAND, direction.getOpposite(), facingX, facingY, facingZ);
            } else {

                // place
                mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(directionOffset, direction.getOpposite(), EnumHand.MAIN_HAND, facingX, facingY, facingZ));
//                if (verify) placeClient(directionOffset, EnumHand.MAIN_HAND, direction.getOpposite(), facingX, facingY, facingZ);
            }
            if (verify) {
                placeClient(directionOffset, EnumHand.MAIN_HAND, direction.getOpposite(), facingX, facingY, facingZ);
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, directionOffset, EnumFacing.DOWN));
            }


            // reset sneak
            if (sneak) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                mc.player.setSneaking(false);
            }

            // reset sprint
            if (sprint) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                mc.player.setSprinting(true);
            }

            // swing hand
            // swing the player's arm
            // held item stack
            ItemStack stack = mc.player.getHeldItem(EnumHand.MAIN_HAND);

            // check stack
            if (!stack.isEmpty()) {
                if (!stack.getItem().onEntitySwing(mc.player, stack)) {

                    // apply swing progress
                    if (!mc.player.isSwingInProgress || mc.player.swingProgressInt >= ((IEntityLivingBase) mc.player).hookGetArmSwingAnimationEnd() / 2 || mc.player.swingProgressInt < 0) {
                        mc.player.swingProgressInt = -1;
                        mc.player.isSwingInProgress = true;
                        mc.player.swingingHand = /*SwingModule.INSTANCE.isEnabled() ? SwingModule.INSTANCE.getHand() : */EnumHand.MAIN_HAND;

                        // send animation packet
                        if (mc.player.world instanceof WorldServer) {
                            ((WorldServer) mc.player.world).getEntityTracker().sendToTracking(mc.player, new SPacketAnimation(mc.player, 0));
                        }
                    }
                }
            }

            // swing with packets
            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            ((IMinecraft) mc).setRightClickDelayTimer(4);
            break;

            /*
            if (!mc.playerController.getCurrentGameType().equals(GameType.CREATIVE)) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, directionOffset, EnumFacing.UP));
            }
             */
        }

//        placing = false;
    }

    public static float getReachDistance() {
        return mc.playerController.getBlockReachDistance();
    }

    public static RayTraceResult getTraceResult(double distance, float[] rotation) {
        Vec3d eyes = mc.player.getPositionEyes(1.0f);

        if (!rotationManager.isSet()) {
            rotation = rotationManager.getRotations();
        }

        Vec3d rotationVector = RotationUtil.getVectorForRotation(rotation);

        return mc.world.rayTraceBlocks(
                eyes,
                eyes.add(rotationVector.x * distance, rotationVector.y * distance, rotationVector.z * distance),
                false,
                false,
                true
        );
    }

    public static void removeGlitchBlocks(BlockPos pos) {
        for (int dx = -4; dx <= 4; ++dx) {
            for (int dy = -4; dy <= 4; ++dy) {
                for (int dz = -4; dz <= 4; ++dz) {
                    BlockPos blockPos = new BlockPos(pos.getX() + dx, pos.getY() + dy, pos.getZ() + dz);
                    if (!BlockTweaks.mc.world.getBlockState(blockPos).getBlock().equals(Blocks.AIR)) continue;
                    BlockTweaks.mc.playerController.processRightClickBlock(BlockTweaks.mc.player, BlockTweaks.mc.world, blockPos, EnumFacing.DOWN, new Vec3d(0.5, 0.5, 0.5), EnumHand.MAIN_HAND);
                }
            }
        }
    }


    public static boolean isObbyHole(BlockPos blockPos) {
        for (BlockPos pos : getTouchingBlocks(blockPos)) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.getBlock() == Blocks.OBSIDIAN) continue;
            return false;
        }
        return true;
    }

    public static boolean isBedrockHole(BlockPos blockPos) {
        for (BlockPos pos : getTouchingBlocks(blockPos)) {
            IBlockState touchingState = mc.world.getBlockState(pos);
            if (touchingState.getBlock() != Blocks.AIR && touchingState.getBlock() == Blocks.BEDROCK) continue;
            return false;
        }
        return true;
    }

    public static void placeCrystalOnBlock(BlockPos pos, EnumHand hand, Swing swing, AutoSwitch silent, boolean packet, EnumFacing facing) {
        placeCrystalOnBlock(pos, hand, swing, silent, packet, facing, false);
    }


    public static void placeCrystalOnBlock(BlockPos pos, EnumHand hand, Swing swing, AutoSwitch silent, boolean packet, EnumFacing facing, boolean switchBack) {
        int old = mc.player.inventory.currentItem;
        int crystal = InventoryUtil.getItemHotbar(Items.END_CRYSTAL);

        InventoryUtil.switchSilent(crystal, crystal, old, silent);
        if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.5F, 1.0F, 0.5F));
        } else {
            mc.playerController.processRightClickBlock(mc.player, mc.world, pos, facing, new Vec3d(0.5, 1.0, 0.5), hand);
        }
        if (switchBack || silent != AutoSwitch.Normal) InventoryUtil.switchSilent(old, crystal, old, silent);
        EntityUtil.swing(swing);
    }


    public static boolean shouldSneakWhileRightClicking(final BlockPos blockPos) {
        final Block block = mc.world.getBlockState(blockPos).getBlock();
        TileEntity tileEntity = null;
        for (final TileEntity tE : mc.world.loadedTileEntityList) {
            if (!tE.getPos().equals(blockPos)) {
                continue;
            }
            tileEntity = tE;
            break;
        }
        return tileEntity != null || block instanceof BlockBed || block instanceof BlockContainer || block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate || block instanceof BlockButton || block instanceof BlockAnvil || block instanceof BlockWorkbench || block instanceof BlockCake || block instanceof BlockRedstoneDiode;
    }

    public static Block getBlock(final double x, final double y, final double z) {
        return mc.world.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static float getBlockDamage(BlockPos pos) {
        try {
            Field f = ReflectionHelper.findField(RenderGlobal.class, "damagedBlocks", "field_72738_E");
            f.setAccessible(true);
            HashMap map = (HashMap) f.get(Minecraft.getMinecraft().renderGlobal);
            for (Object var : map.values()) {
                DestroyBlockProgress destroyBlockProgress = (DestroyBlockProgress) var;
                if (!destroyBlockProgress.getPosition().equals(pos) || destroyBlockProgress.getPartialBlockDamage() < 0 || destroyBlockProgress.getPartialBlockDamage() > 10)
                    continue;
                return (float) destroyBlockProgress.getPartialBlockDamage() / 10.0f;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0.0f;
    }

    public static List<EnumFacing> getVisibleSides(BlockPos position) {
        List<EnumFacing> visibleSides = new ArrayList<>();

        // pos vector
        Vec3d positionVector = new Vec3d(position).add(0.5, 0.5, 0.5);

        // facing
        double facingX = mc.player.getPositionEyes(1).x - positionVector.x;
        double facingY = mc.player.getPositionEyes(1).y - positionVector.y;
        double facingZ = mc.player.getPositionEyes(1).z - positionVector.z;

        // x
        {
            if (facingX < -0.5) {
                visibleSides.add(EnumFacing.WEST);
            } else if (facingX > 0.5) {
                visibleSides.add(EnumFacing.EAST);
            } else if (!mc.world.getBlockState(position).isFullBlock() || !mc.world.isAirBlock(position)) {
                visibleSides.add(EnumFacing.WEST);
                visibleSides.add(EnumFacing.EAST);
            }
        }

        // y
        {
            if (facingY < -0.5) {
                visibleSides.add(EnumFacing.DOWN);
            } else if (facingY > 0.5) {
                visibleSides.add(EnumFacing.UP);
            } else {
                visibleSides.add(EnumFacing.DOWN);
                visibleSides.add(EnumFacing.UP);
            }
        }

        // z
        {
            if (facingZ < -0.5) {
                visibleSides.add(EnumFacing.NORTH);
            } else if (facingZ > 0.5) {
                visibleSides.add(EnumFacing.SOUTH);
            } else if (!mc.world.getBlockState(position).isFullBlock() || !mc.world.isAirBlock(position)) {
                visibleSides.add(EnumFacing.NORTH);
                visibleSides.add(EnumFacing.SOUTH);
            }
        }

        return visibleSides;
    }

    public static void damageBlock(BlockPos position, boolean packet, boolean rotations) {
        damageBlock(position, EnumFacing.getDirectionFromEntityLiving(position, mc.player), packet, rotations);
    }

    public static void damageBlock(BlockPos position, EnumFacing facing, boolean packet, boolean rotations) {
        if (rotations) {
            float[] r = MathUtil.calculateLookAt((double) position.getX() + 0.5D, (double) position.getY() + 0.5D, (double) position.getZ() + 0.5D, mc.player);
            Client.rotationManager.desiredYaw = r[0];
            Client.rotationManager.desiredPitch = r[1];
        }
        mc.player.swingArm(EnumHand.MAIN_HAND);
        if (packet) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, position, facing));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, position, facing));
        } else if (mc.getConnection().getPlayerInfo(mc.player.getUniqueID()).getGameType() == GameType.CREATIVE) {
            mc.playerController.clickBlock(position, facing);
        } else {
            mc.playerController.onPlayerDamageBlock(position, facing);
        }
    }

    public static Boolean isPosInFov(final BlockPos pos) {
        final int dirnumber = RotationUtil.getDirection4D();
        if (dirnumber == 0 && pos.getZ() - mc.player.getPositionVector().z < 0.0) {
            return false;
        }
        if (dirnumber == 1 && pos.getX() - mc.player.getPositionVector().x > 0.0) {
            return false;
        }
        if (dirnumber == 2 && pos.getZ() - mc.player.getPositionVector().z > 0.0) {
            return false;
        }
        return dirnumber != 3 || pos.getX() - mc.player.getPositionVector().x >= 0.0;
    }

    public static boolean validObi(BlockPos pos) {
        return !validBedrock(pos)
                && (mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK)
                && (mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK)
                && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
    }

    public static boolean validBedrock(BlockPos pos) {
        return mc.world.getBlockState(pos.add(0, -1, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK
                && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR
                && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
    }

    public static BlockPos validTwoBlockObiXZ(BlockPos pos) {
        if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().south()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east().north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.east()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.east().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null ? new BlockPos(1, 0, 0) : null;
        } else if (
                (mc.world.getBlockState(pos.down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.north()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.up(2)).getMaterial() == Material.AIR
                        && (mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().down()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south(2)).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().east()).getBlock() == Blocks.BEDROCK)
                        && (mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(pos.south().west()).getBlock() == Blocks.BEDROCK)
                        && mc.world.getBlockState(pos.south()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up()).getMaterial() == Material.AIR
                        && mc.world.getBlockState(pos.south().up(2)).getMaterial() == Material.AIR
        ) {
            return validTwoBlockBedrockXZ(pos) == null ? new BlockPos(0, 0, 1) : null;
        }
        return null;
    }

    public static BlockPos validTwoBlockBedrockXZ(BlockPos pos) {
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
            return new BlockPos(1, 0, 0);
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
            return new BlockPos(0, 0, 1);
        }
        return null;
    }


    public static boolean checkForEntities(BlockPos blockPos) {
        for (Entity entity : mc.world.loadedEntityList) {

            if (entity instanceof EntityItem
                    || entity instanceof EntityEnderCrystal
                    || entity instanceof EntityXPOrb
                    || entity instanceof EntityExpBottle
                    || entity instanceof EntityArrow) {

                continue;
            }

            if (new AxisAlignedBB(blockPos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static boolean canReplace(BlockPos pos) {
        return getState(pos).getMaterial().isReplaceable();
    }

    public static boolean isPosEmpty(BlockPos pos) {
        return empty.contains(mc.world.getBlockState(pos).getBlock());
    }


    public static EnumActionResult packetClick(EntityPlayer player, World worldIn, ItemStack stack, EnumHand hand, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (mc.playerController.currentGameType == GameType.SPECTATOR) {
            TileEntity tileentity = worldIn.getTileEntity(pos);

            if (tileentity instanceof ILockableContainer) {
                Block block1 = worldIn.getBlockState(pos).getBlock();
                ILockableContainer ilockablecontainer = (ILockableContainer) tileentity;

                if (ilockablecontainer instanceof TileEntityChest && block1 instanceof BlockChest) {
                    ilockablecontainer = ((BlockChest) block1).getLockableContainer(worldIn, pos);
                }

                if (ilockablecontainer != null) {
                    player.displayGUIChest(ilockablecontainer);
                    return EnumActionResult.SUCCESS;
                }
            } else if (tileentity instanceof IInventory) {
                player.displayGUIChest((IInventory) tileentity);
                return EnumActionResult.SUCCESS;
            }

            return EnumActionResult.PASS;
        } else {
            double reachDist = player.getEntityAttribute(EntityPlayer.REACH_DISTANCE).getAttributeValue();
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event = net.minecraftforge.common.ForgeHooks
                    .onRightClickBlock(player, hand, pos, facing, net.minecraftforge.common.ForgeHooks.rayTraceEyeHitVec(player, reachDist + 1));
            if (event.isCanceled()) return event.getCancellationResult();

            EnumActionResult result = EnumActionResult.PASS;
            if (event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY) {
                result = stack.onItemUseFirst(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                if (result != EnumActionResult.PASS) return result;
            }

            boolean bypass = player.getHeldItemMainhand().doesSneakBypassUse(worldIn, pos, player) && player.getHeldItemOffhand().doesSneakBypassUse(worldIn, pos, player);

            if (!player.isSneaking() || bypass || event.getUseBlock() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                IBlockState iblockstate = worldIn.getBlockState(pos);
                if (event.getUseBlock() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY)
                    if (iblockstate.getBlock().onBlockActivated(worldIn, pos, iblockstate, player, hand, facing, hitX, hitY, hitZ)) {
                        result = EnumActionResult.SUCCESS;
                    }
            }

            if (stack.isEmpty()) {
                return EnumActionResult.PASS;
            } else if (player.getCooldownTracker().hasCooldown(stack.getItem())) {
                return EnumActionResult.PASS;
            } else {
                if (stack.getItem() instanceof ItemBlock && !player.canUseCommandBlock()) {
                    Block block = ((ItemBlock) stack.getItem()).getBlock();

                    if (block instanceof BlockCommandBlock || block instanceof BlockStructure) {
                        return EnumActionResult.FAIL;
                    }
                }

                if (mc.player.isCreative()) {
                    int j = stack.getMetadata();
                    int i = stack.getCount();
                    if (result != EnumActionResult.SUCCESS && event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY
                            || result == EnumActionResult.SUCCESS && event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                        EnumActionResult enumactionresult = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                        stack.setItemDamage(j);
                        stack.setCount(i);
                        return enumactionresult;
                    } else return result;
                } else {
                    if (result != EnumActionResult.SUCCESS && event.getUseItem() != net.minecraftforge.fml.common.eventhandler.Event.Result.DENY
                            || result == EnumActionResult.SUCCESS && event.getUseItem() == net.minecraftforge.fml.common.eventhandler.Event.Result.ALLOW) {
                        ItemStack copyBeforeUse = stack.copy();
                        result = stack.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
                        if (stack.isEmpty())
                            net.minecraftforge.event.ForgeEventFactory.onPlayerDestroyItem(player, copyBeforeUse, hand);
                    }
                    return result;
                }
            }
        }
    }

    public static boolean isAir(BlockPos pos) {
        return BlockUtil.mc.world.getBlockState(pos).getBlock() == Blocks.AIR;
    }


   public static boolean isInterceptedByOther(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {

            if (entity instanceof EntityOtherPlayerMP || entity instanceof EntityItem || entity instanceof EntityEnderCrystal || entity instanceof EntityXPOrb || entity instanceof EntityExpBottle || entity instanceof EntityArrow) {
                continue;
            }

            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return false;
            }
        }
        return true;
    }
}