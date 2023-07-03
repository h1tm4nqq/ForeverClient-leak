package we.devs.forever.api.manager.impl.player;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.RotationUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.setting.Setting;

import java.util.*;
import java.util.stream.Collectors;

public class InteractionManager extends AbstractManager {
    private final TimerUtil attackTimer = new TimerUtil();
    private CPacketPlayerTryUseItemOnBlock placePacket;

    public InteractionManager() {
        super("Interact Manager");
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }

    //Block placements

    public void placeBlock(BlockPos pos, RotationType rotate, boolean packet, boolean strict, boolean attackCrystal, Swing swing) {
        placeBlock(pos, rotate, packet, attackCrystal, strict, false, false, true, swing);
    }

    public void placeBlock(BlockPos pos, RotationType rotate, boolean packet, boolean attackCrystal) {
        placeBlock(pos, rotate, packet, attackCrystal, false, false);
    }

    public void placeBlock(BlockPos pos, RotationType rotate, boolean packet, boolean strict, boolean boost, boolean attackCrystal) {
        placeBlock(pos, rotate, packet, attackCrystal, strict, boost, false, false, Swing.Mainhand);
    }

    public void placeBlockIgnore(BlockPos pos, RotationType rotate, boolean packet, boolean strict, boolean boost, boolean attackCrystal) {
        placeBlock(pos, rotate, packet, attackCrystal, strict, boost, false, true, Swing.Mainhand);
    }

    public void placeBlock(BlockPos pos, RotationType rotate, boolean packet, boolean strict, boolean boost, boolean confirm, boolean attackCrystal) {
        placeBlock(pos, rotate, packet, attackCrystal, strict, boost, confirm, false, Swing.Mainhand);
    }

    public void placeBlock(BlockPos pos, RotationType rotate, boolean packet, boolean attackCrystal, boolean strict, boolean boost, boolean confirm, boolean ignoreEntities, Swing swing) {

        if (fullNullCheck()) return;

        if (BlockUtil.canReplace(pos)) {

//            boolean isPiston = InventoryUtil.findHotbarBlock(Blocks.STICKY_PISTON) == mc.player.inventory.currentItem || InventoryUtil.findHotbarBlock(Blocks.PISTON) == mc.player.inventory.currentItem;

            Optional<ClickLocation> posCL = getClickLocation(pos, ignoreEntities, false, attackCrystal, strict);

            if (posCL.isPresent()) {

                BlockPos currentPos = posCL.get().neighbour;
                EnumFacing currentFace = posCL.get().opposite;

                if (attackCrystal) {
//                    if (!attackCrystals(pos, RotationType.Adaptive, strict, Swing.Mainhand)) return;
                    attackCrystals(pos, RotationType.Packet, strict, Swing.PacketMainhand);
//                    if (!BlockUtil.canPlace(pos, true)) return;
                }

                boolean shouldSneak = shouldShiftClick(currentPos);

                if (shouldSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
                }

                boolean sprint = mc.player.isSprinting();

                if (sprint) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                }

                // our rotation
                float[] rotation = RotationUtil.getAnglesToBlock(currentPos, currentFace);


                // vector to the block
                Vec3d hitVec = new Vec3d(currentPos).add(0.5, 0.5, 0.5).add(new Vec3d(currentFace.getDirectionVec()).scale(0.5));

                if (strict && rotate == RotationType.Adaptive) rotationManager.setStrict();
                rotationManager.doRotation(rotate, rotation);

                if (packet) {
//                    if (boost) placeClient(currentPos, EnumHand.MAIN_HAND, currentFace, x, y, z);

                    float x = (float) (hitVec.x - currentPos.getX());
                    float y = (float) (hitVec.y - currentPos.getY());
                    float z = (float) (hitVec.z - currentPos.getZ());
                    mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(currentPos, currentFace, EnumHand.MAIN_HAND, x, y, z));
                } else {
                    mc.playerController.processRightClickBlock(mc.player, mc.world, currentPos, currentFace, hitVec, EnumHand.MAIN_HAND);
                }


                mc.player.swingArm(EnumHand.MAIN_HAND);
                mc.player.connection.sendPacket((Packet) new CPacketAnimation(EnumHand.MAIN_HAND));

                if (confirm) rotationManager.resetRotations();

                if (sprint) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }

                if (shouldSneak) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                }

                IBlockState blockState = Blocks.OBSIDIAN.getStateForPlacement(
                        mc.world, currentPos, currentFace, (float) hitVec.x, (float) hitVec.y, (float) hitVec.z, 0, mc.player, EnumHand.MAIN_HAND
                );
            }
        }
    }

//    @EventListener
//    public void onPacketReceive(PacketEvent.Receive event) {
//        if (event.getPacket() instanceof SPacketSoundEffect) {
//            SPacketSoundEffect packet = event.getPacket();
//            if (packet.getCategory() == SoundCategory.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
//
//
//                mc.addScheduledTask(() -> {
//                    mc.world.loadedEntityList.stream()
//                            .filter(Objects::nonNull)
//                            .filter(crystal -> !crystal.isDead)
//                            .filter(o -> o instanceof EntityEnderCrystal)
//                            .filter(o -> o.getDistanceSq(packet.getX(), packet.getY(), packet.getZ()) < 36)
//                            .collect(Collectors.toCollection(LinkedList::new))
//                            .forEach(entity -> {
//                                entity.setDead();
//                                mc.world.removeEntity(entity);
//                                mc.world.removeEntityDangerously(entity);
//                            });
//
//                });
//            }
//        }
//
//        if (event.getPacket() instanceof SPacketExplosion) {
//            // crystal entities within the packet position
//            List<EntityEnderCrystal> explosionCrystals = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(((SPacketExplosion) event.getPacket()).getX(), ((SPacketExplosion) event.getPacket()).getY(), ((SPacketExplosion) event.getPacket()).getZ())));
//
//            mc.addScheduledTask(() -> explosionCrystals.stream()
//                    .filter(Objects::nonNull)
//                    .filter(crystal -> !crystal.isDead)
//                    .filter(entityEnderCrystal -> entityEnderCrystal.getDistanceSq(((SPacketExplosion) event.getPacket()).getX() + 0.5, ((SPacketExplosion) event.getPacket()).getY() + 0.5, ((SPacketExplosion) event.getPacket()).getZ() + 0.5) <= 36)
//                    .forEach(entityEnderCrystal -> {
//                        entityEnderCrystal.setDead();
//                        mc.world.removeEntity(entityEnderCrystal);
//                        mc.world.removeEntityDangerously(entityEnderCrystal);
//                    })
//            );
//
//        }
//
//        if (event.getPacket() instanceof SPacketDestroyEntities) {
//            SPacketDestroyEntities packet4 = event.getPacket();
//            for (int id : packet4.getEntityIDs()) {
//                Entity entity = AutoCrystal.mc.world.getEntityByID(id);
//                if (!(entity instanceof EntityEnderCrystal)) continue;
//
//                entity.setDead();
//                mc.addScheduledTask(() -> {
//                    mc.world.removeEntity(entity);
//                    mc.world.removeEntityDangerously(entity);
//                });
//            }
//        }
//    }


    public EnumFacing getFacing(BlockPos pos, boolean strictDirection) {
        if (pos == null) return null;
        List<EnumFacing> validAxis = new ArrayList<>();
        Vec3d eyePos = mc.player.getPositionEyes(1.0f);
        if (strictDirection) {
            Vec3d blockCenter = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            IBlockState blockState = mc.world.getBlockState(pos);
            boolean isFullBox = blockState.getBlock() == Blocks.AIR || blockState.isFullBlock();
            validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
            validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
            validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
            validAxis = validAxis.stream().filter(facing -> mc.world.rayTraceBlocks(eyePos, new Vec3d(pos)
                    .add(0.5, 0.5, 0.5)
                    .add(new Vec3d(facing.getDirectionVec()).scale(0.5))) == null).collect(Collectors.toList());
            if (validAxis.isEmpty()) {
                validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
                validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
                validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
            }
        } else {
            validAxis = Arrays.asList(EnumFacing.values());
        }
        return validAxis.stream().min(Comparator.comparing(enumFacing -> new Vec3d(pos)
                .add(0.5, 0.5, 0.5)
                .add(new Vec3d(enumFacing.getDirectionVec()).scale(0.5)).distanceTo(eyePos))).orElse(null);
    }

    public List<EnumFacing> getPlacableFacings(BlockPos pos, boolean strictDirection, boolean rayTrace) {
        ArrayList<EnumFacing> validFacings = new ArrayList<>();
        for (EnumFacing side : EnumFacing.values()) {
            if (rayTrace) {
                Vec3d testVec = new Vec3d(pos).add(0.5, 0.5, 0.5).add(new Vec3d(side.getDirectionVec()).scale(0.5));
                RayTraceResult result = mc.world.rayTraceBlocks(mc.player.getPositionEyes(1F), testVec);
                if (result != null && result.typeOfHit != RayTraceResult.Type.MISS) {
                    continue;
                }
            }
            BlockPos neighbour = pos.offset(side);
            if (strictDirection) {
                Vec3d eyePos = mc.player.getPositionEyes(1.0f);
                Vec3d blockCenter = new Vec3d(neighbour.getX() + 0.5, neighbour.getY() + 0.5, neighbour.getZ() + 0.5);
                IBlockState blockState = mc.world.getBlockState(neighbour);
                boolean isFullBox = blockState.getBlock() == Blocks.AIR || blockState.isFullBlock();
                ArrayList<EnumFacing> validAxis = new ArrayList<>();
                validAxis.addAll(checkAxis(eyePos.x - blockCenter.x, EnumFacing.WEST, EnumFacing.EAST, !isFullBox));
                validAxis.addAll(checkAxis(eyePos.y - blockCenter.y, EnumFacing.DOWN, EnumFacing.UP, true));
                validAxis.addAll(checkAxis(eyePos.z - blockCenter.z, EnumFacing.NORTH, EnumFacing.SOUTH, !isFullBox));
                if (!validAxis.contains(side.getOpposite())) continue;
            }
            IBlockState blockState = mc.world.getBlockState(neighbour);
            if ((blockState == null || !blockState.getBlock().canCollideCheck(blockState, false) || blockState.getMaterial().isReplaceable()))
                continue;
            validFacings.add(side);
        }
        return validFacings;
    }

    public ArrayList<EnumFacing> checkAxis(double diff, EnumFacing negativeSide, EnumFacing positiveSide, boolean bothIfInRange) {
        ArrayList<EnumFacing> valid = new ArrayList<>();
        if (diff < -0.5) {
            valid.add(negativeSide);
        }
        if (diff > 0.5) {
            valid.add(positiveSide);
        }
        if (bothIfInRange) {
            if (!valid.contains(negativeSide)) valid.add(negativeSide);
            if (!valid.contains(positiveSide)) valid.add(positiveSide);
        }
        return valid;
    }


    //Entity interactions

    public void attackEntity(Entity entity, boolean packet, Swing swing) {

        if (packet) {
            mc.player.connection.sendPacket(new CPacketUseEntity(entity));
        } else {
            mc.playerController.attackEntity(mc.player, entity);
        }
        EntityUtil.swing(swing);
    }

    public synchronized void attackCrystal(int entity, boolean strictDirection, Swing swing) {

        boolean sprint = mc.player.isSprinting();

        int ping = serverManager.getPing();

        double delay = strictDirection ? 2 : 1.5;

//        if (attackTimer.passedMs(ping <= 50 ? ping : ping * delay)) {

        if (sprint && strictDirection) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            mc.player.setSprinting(false);
        }
        CPacketUseEntity breakPacket = new CPacketUseEntity();
        breakPacket.entityId = entity;
        breakPacket.action = CPacketUseEntity.Action.ATTACK;
        mc.player.connection.sendPacket(breakPacket);
        EntityUtil.swing(swing);

        if (sprint && strictDirection) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            mc.player.setSprinting(true);
        }

        attackTimer.reset();


//        }
    }

    @SuppressWarnings("all")
    public void attackCrystals(BlockPos pos, RotationType rotate, boolean strict, Swing swing) {
        boolean canPlace = true;
        boolean sprint = mc.player.isSprinting();


        int ping = serverManager.getPing();
        double delay = strict ? 3 : 1.5;

        for (EntityEnderCrystal crystal : mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos))) {
            if (attackTimer.passedMs(ping <= 50 ? 50 : ping * delay)) {

                if (sprint) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                    mc.player.setSprinting(false);
                }

                if (strict && rotate == RotationType.Adaptive) rotationManager.setStrict();
                if (rotate != RotationType.Off) rotationManager.doRotation(rotate, crystal.getPositionVector());

                attackEntity(crystal, false, swing);

                if (sprint) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                    mc.player.setSprinting(true);
                }


                attackTimer.reset();
            }
        }

    }

    public boolean isPosePlaceable(BlockPos pos, Setting<Float> range, Setting<Float> wallRange) {
        return isPosePlaceable(pos, range.getValue(), wallRange.getValue());
    }

    public boolean isPosePlaceable(BlockPos pos, float range, float wallRange) {
        if (!BlockUtil.isPosEmpty(pos)) return false;

        Vec3d eyesPos = mc.player.getPositionVector().add(0, mc.player.getEyeHeight(), 0);
        if (BlockUtil.canSee(pos)
                ? eyesPos.squareDistanceTo(new Vec3d(pos)) > range * range
                : eyesPos.squareDistanceTo(new Vec3d(pos)) > wallRange * wallRange) return false;

        return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).stream()
                .filter(entity -> !(entity instanceof EntityXPOrb))
                .filter(entity -> !(entity instanceof EntityItem))
                .filter(entity -> !(entity instanceof EntityArrow))
                .filter(entity -> !(entity instanceof EntityEgg))
                .filter(entity -> !(entity instanceof EntityFishHook))
                .filter(entity -> !(entity instanceof EntityEnderPearl)).allMatch(entity -> entity instanceof EntitySnowball);
    }

    public boolean crystalCheck(BlockPos pos) {
        return mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(pos)).isEmpty();
    }

    //Getters & variable methods

    public static class ClickLocation {
        public final BlockPos neighbour;
        public final EnumFacing opposite;

        public ClickLocation(BlockPos neighbour, EnumFacing opposite) {
            this.neighbour = neighbour;
            this.opposite = opposite;
        }
    }

    public Optional<ClickLocation> getClickLocation(BlockPos pos, boolean ignoreEntities, boolean noPistons, boolean onlyCrystals, boolean strict) {
        Block block = mc.world.getBlockState(pos).getBlock();

        if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) {
            return Optional.empty();
        }

        if (!ignoreEntities) {
            for (Entity entity : mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos))) {
                if (onlyCrystals && entity instanceof EntityEnderCrystal) continue;
                if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb) && !(entity instanceof EntityArrow)) {
                    return Optional.empty();
                }
            }
//            if (!canPlace(pos, true)) return Optional.empty();
        }

        EnumFacing side = null;

        for (EnumFacing blockSide : EnumFacing.values()) {
            BlockPos sidePos = pos.offset(blockSide);
            if (noPistons) {
                if (mc.world.getBlockState(sidePos).getBlock() == Blocks.PISTON) continue;
            }
            if (!mc.world.getBlockState(sidePos).getBlock().canCollideCheck(mc.world.getBlockState(sidePos), false)) {
                continue;
            }

            if (strict && !BlockUtil.getVisibleSides(pos).contains(blockSide.getOpposite())) {
                continue;
            }

            IBlockState blockState = mc.world.getBlockState(sidePos);
            if (!blockState.getMaterial().isReplaceable()) {
                side = blockSide;
                break;
            }
        }
        if (side == null) {
            return Optional.empty();
        }
        BlockPos neighbour = pos.offset(side);
        EnumFacing opposite = side.getOpposite();
        if (!mc.world.getBlockState(neighbour).getBlock().canCollideCheck(mc.world.getBlockState(neighbour), false)) {
            return Optional.empty();
        }

        return Optional.of(new ClickLocation(neighbour, opposite));
    }

    public boolean shouldShiftClick(BlockPos pos) {
        Block block = mc.world.getBlockState(pos).getBlock();

        TileEntity tileEntity = null;

        for (TileEntity entity : mc.world.loadedTileEntityList) {
            if (!entity.getPos().equals(pos)) continue;
            tileEntity = entity;
            break;
        }
        return tileEntity != null || block instanceof BlockBed || block instanceof BlockContainer || block instanceof BlockDoor || block instanceof BlockTrapDoor || block instanceof BlockFenceGate || block instanceof BlockButton || block instanceof BlockAnvil || block instanceof BlockWorkbench || block instanceof BlockCake || block instanceof BlockRedstoneDiode;
    }

    public void placeClient(BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
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
}
