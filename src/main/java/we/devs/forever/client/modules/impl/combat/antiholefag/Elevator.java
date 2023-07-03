package we.devs.forever.client.modules.impl.combat.antiholefag;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.combat.antiholefag.enums.Mode;
import we.devs.forever.client.modules.impl.combat.antiholefag.enums.Swing;
import we.devs.forever.client.modules.impl.combat.antiholefag.utils.BlockUtilPa;
import we.devs.forever.client.modules.impl.combat.holefill.HoleFill;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.main.ForeverClient;

import java.util.List;

public class Elevator extends Module {
    public Setting<Swing> swing = (new Setting<>("Swing", Swing.Both));
    Setting<Boolean> rotate = (new Setting<>("Rotate", true));
    Setting<Boolean> holefill = (new Setting<>("HoleFill", true));
    Setting<Mode> mode = (new Setting<>("Mode", Mode.Block));
    Setting<Integer> range = (new Setting<>("Range", 5, 1, 6));
    EntityPlayer target = null;
    int pistonSide;
    boolean pistonPlaced = false, redstonePlaced = false;
    BlockPos pistonToPlace = null, redstoneToPlace = null;
    public Elevator() {
        super("AntiHoleFag", "anti hole camp completely made by me", Category.COMBAT);

    }

    private static boolean isntIntercepted(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox()) && !(entity instanceof EntityItem)) {
                return false;
            }
        }
        return true;
    }

    public static int getHotbarSlot(final Block block) {
        for (int i = 0; i < 9; i++) {
            final Item item = mc.player.inventory.getStackInSlot(i).getItem();
            if (item instanceof ItemBlock && ((ItemBlock) item).getBlock().equals(block)) return i;
        }
        ForeverClient.LOGGER.info("getHotbarSlot");
        return -1;
    }

    public static int getSkullSlot() {
        List<ItemStack> mainInventory = mc.player.inventory.mainInventory;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mainInventory.get(i);
            if (stack != ItemStack.EMPTY && stack.getItem() instanceof ItemSkull)
                return i;
        }
        return -1;
    }

    @Override
    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            try {

                checkFlags();

                /* CHING CHONG CODE ALERT
                if (skull.getValue() && target != null) {
                    if (getSkullSlot() != -1) {
                        BlockUtil.placeBlockIntercepted(target.getPosition(), getSkullSlot(), rotate.getValue(), false, swing);
                    } else {
                        MessageUtil.send_client_message("You don't have SKULL");
                    }
                }
                */

                if (!pistonPlaced && pistonToPlace != null) {
                    if (pistonSide == 1) {
                        BlockUtilPa.rotatePacket(mc.player.posX + 1, mc.player.posY, mc.player.posZ);
                    } else if (pistonSide == 2) {
                        BlockUtilPa.rotatePacket(mc.player.posX, mc.player.posY, mc.player.posZ + 1);
                    } else if (pistonSide == 3) {
                        BlockUtilPa.rotatePacket(mc.player.posX - 1, mc.player.posY, mc.player.posZ);
                    } else if (pistonSide == 4) {
                        BlockUtilPa.rotatePacket(mc.player.posX, mc.player.posY, mc.player.posZ - 1);
                    }
                    BlockUtilPa.placeBlock(pistonToPlace, getPiston(), rotate.getValue(), false, swing.getValue());

                }

                if (!redstonePlaced && redstoneToPlace != null) {
                    BlockUtilPa.placeBlock(redstoneToPlace, getRedstone(), rotate.getValue(), false, swing.getValue());
                }

                setEnabled(false);
                ForeverClient.LOGGER.info("enable");
            } catch (Exception e) {
                ForeverClient.LOGGER.info("enable catch");
                e.printStackTrace();
                Command.sendMessage("An error has ocurred: " + e.getMessage());
                setEnabled(false);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null && mc.world != null) {
            redstonePlaced = false;
            pistonPlaced = false;
            pistonToPlace = null;
            redstoneToPlace = null;
            target = null;
            ForeverClient.LOGGER.info("disable");
            if (holefill.getValue()) {
                HoleFill.INSTANCE.enable();
            }
        }
    }

    private boolean canPlace() {
        return mc.world.getBlockState(new BlockPos(target.posX, target.posY + 1, target.posZ)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(new BlockPos(target.posX, target.posY + 2, target.posZ)).getBlock().equals(Blocks.AIR) && piston() && redstone();
    }

    private boolean piston() {
        boolean canPlacePiston = false;
        pistonSide = 0;

        BlockPos[] posToCheck = {
                new BlockPos(target.posX + 1, target.posY + 1, target.posZ),
                new BlockPos(target.posX, target.posY + 1, target.posZ + 1),
                new BlockPos(target.posX - 1, target.posY + 1, target.posZ),
                new BlockPos(target.posX, target.posY + 1, target.posZ - 1)
        };

        for (BlockPos checkPistons : posToCheck) {
            pistonSide += 1;
            if (mc.world.getBlockState(checkPistons).getBlock().equals(Blocks.PISTON) || mc.world.getBlockState(checkPistons).getBlock().equals(Blocks.STICKY_PISTON)) {
                pistonPlaced = true;
                break;
            }
        }

        if (pistonPlaced) {
            switch (pistonSide) {
                case 1:
                    if (mc.world.getBlockState(posToCheck[2]).getBlock().equals(Blocks.AIR)) {
                        canPlacePiston = true;
                    }
                    break;
                case 2:
                    if (mc.world.getBlockState(posToCheck[3]).getBlock().equals(Blocks.AIR)) {
                        canPlacePiston = true;
                    }
                    break;
                case 3:
                    if (mc.world.getBlockState(posToCheck[0]).getBlock().equals(Blocks.AIR)) {
                        canPlacePiston = true;
                    }
                    break;
                case 4:
                    if (mc.world.getBlockState(posToCheck[1]).getBlock().equals(Blocks.AIR)) {
                        canPlacePiston = true;
                    }
                    break;
            }
        } else {

            if (mc.world.getBlockState(posToCheck[0]).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(posToCheck[2]).getBlock().equals(Blocks.AIR)) {

                if (mc.player.getDistanceSq(posToCheck[0]) < mc.player.getDistanceSq(posToCheck[2])) {
                    if (mc.player.getDistanceSq(posToCheck[2]) <= 9) {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[2];
                        pistonSide = 3;
                    } else {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[0];
                        pistonSide = 1;
                    }
                } else {
                    if (mc.player.getDistanceSq(posToCheck[0]) <= 9) {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[0];
                        pistonSide = 1;
                    } else {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[2];
                        pistonSide = 3;
                    }
                }

            } else if (mc.world.getBlockState(posToCheck[1]).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(posToCheck[3]).getBlock().equals(Blocks.AIR)) {

                if (mc.player.getDistanceSq(posToCheck[1]) < mc.player.getDistanceSq(posToCheck[3])) {
                    if (mc.player.getDistanceSq(posToCheck[3]) <= 9) {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[3];
                        pistonSide = 4;
                    } else {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[1];
                        pistonSide = 2;
                    }
                } else {
                    if (mc.player.getDistanceSq(posToCheck[1]) <= 9) {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[1];
                        pistonSide = 2;
                    } else {
                        canPlacePiston = true;
                        pistonToPlace = posToCheck[3];
                        pistonSide = 4;
                    }
                }
            }

        }

        if (pistonToPlace != null) {
            if (!isntIntercepted(pistonToPlace)) {
                Command.sendMessage("Is intercepted by other entity");

                canPlacePiston = false;
            }
        }

        return canPlacePiston;
    }

    private boolean redstone() {
        boolean canPlaceRedstone = false;

        BlockPos redstoneSideA = null, redstoneSideB = null, redstoneSideC = null, redstoneSideD = null, redstoneSideE = null;

        switch (pistonSide) {
            case 1:
                redstoneSideA = new BlockPos(target.posX + 2, target.posY + 1, target.posZ);
                if (mode.getValue() == Mode.Torch) {
                    redstoneSideB = new BlockPos(target.posX + 1, target.posY + 2, target.posZ);
                }
                redstoneSideC = new BlockPos(target.posX + 1, target.posY, target.posZ);
                redstoneSideD = new BlockPos(target.posX + 1, target.posY + 1, target.posZ + 1);
                redstoneSideE = new BlockPos(target.posX + 1, target.posY + 1, target.posZ - 1);
                break;
            case 2:
                redstoneSideA = new BlockPos(target.posX, target.posY + 1, target.posZ + 2);
                if (mode.getValue() == Mode.Block) {
                    redstoneSideB = new BlockPos(target.posX, target.posY + 2, target.posZ + 1);
                }
                redstoneSideC = new BlockPos(target.posX, target.posY, target.posZ + 1);
                redstoneSideD = new BlockPos(target.posX + 1, target.posY + 1, target.posZ + 1);
                redstoneSideE = new BlockPos(target.posX - 1, target.posY + 1, target.posZ + 1);
                break;
            case 3:
                redstoneSideA = new BlockPos(target.posX - 2, target.posY + 1, target.posZ);
                if (mode.getValue() == Mode.Block) {
                    redstoneSideB = new BlockPos(target.posX - 1, target.posY + 2, target.posZ);
                }
                redstoneSideC = new BlockPos(target.posX - 1, target.posY, target.posZ);
                redstoneSideD = new BlockPos(target.posX - 1, target.posY + 1, target.posZ + 1);
                redstoneSideE = new BlockPos(target.posX - 1, target.posY + 1, target.posZ - 1);
                break;
            case 4:
                redstoneSideA = new BlockPos(target.posX, target.posY + 1, target.posZ - 2);
                if (mode.getValue() == Mode.Block) {
                    redstoneSideB = new BlockPos(target.posX, target.posY + 2, target.posZ - 1);
                }
                redstoneSideC = new BlockPos(target.posX, target.posY, target.posZ - 1);
                redstoneSideD = new BlockPos(target.posX + 1, target.posY + 1, target.posZ - 1);
                redstoneSideE = new BlockPos(target.posX - 1, target.posY + 1, target.posZ - 1);
                break;
        }

        assert redstoneSideA != null;
        BlockPos[] check = {redstoneSideA, redstoneSideB, redstoneSideC, redstoneSideD, redstoneSideE};

        for (BlockPos pos : check) {
            if (pos != null) {
                if (mc.world.getBlockState(pos).getBlock().equals(Blocks.REDSTONE_TORCH)) {
                    canPlaceRedstone = true;
                    redstonePlaced = true;
                    break;
                } else if (mc.world.getBlockState(pos).getBlock().equals(Blocks.REDSTONE_BLOCK)) {
                    canPlaceRedstone = true;
                    redstonePlaced = true;
                    break;
                }
            }
        }

        if (!redstonePlaced) {
            for (BlockPos toPlace : check) {
                if (toPlace != null) {
                    if (mc.world.getBlockState(toPlace).getBlock().equals(Blocks.AIR) && isntIntercepted(toPlace)) {
                        redstoneToPlace = toPlace;
                        canPlaceRedstone = true;
                        break;
                    }
                }
            }
        }

        return canPlaceRedstone;

    }

    private EntityPlayer getTarget(double range) {
        EntityPlayer target = null;
        double distance = Math.pow(range, 2.0) + 1.0;
        for (EntityPlayer player : mc.world.playerEntities) {
            if (EntityUtil.isntValid(player, range) || speedManager.getPlayerSpeed(player) > 10.0)
                continue;
            if (target == null) {
                target = player;
                distance = mc.player.getDistanceSq(player);
                continue;
            }
            if (!(mc.player.getDistanceSq(player) < distance)) continue;
            target = player;
            distance = mc.player.getDistanceSq(player);
        }
        return target;
    }

    private void checkFlags() {
        target = getTarget(range.getValue());

        if (target == null) {
            Command.sendMessage("Can't find target");
            ForeverClient.LOGGER.info("checkFlags");
            setEnabled(false);
            return;
        }

        if (mc.player.posY > target.posY + 1 || (mc.player.getDistance(target) <= 2 && mc.player.posY < target.posY)) {
            Command.sendMessage("You cannot be 2+ blocks under the enemy or 2- above and near");
            setEnabled(false);
            return;
        }

        BlockPos blockPos = new BlockPos(target.posX, target.posY + 0.2, target.posZ);
        if (!(mc.world.getBlockState(blockPos).getBlock().equals(Blocks.BEDROCK)) && !(mc.world.getBlockState(blockPos).getBlock().equals(Blocks.OBSIDIAN) || mc.world.getBlockState(blockPos).getBlock().equals(Blocks.ENDER_CHEST) || mc.world.getBlockState(blockPos).getBlock().equals(Blocks.SKULL)) && BlockUtilPa.isHole(blockPos, true, true).getType() == BlockUtilPa.HoleType.NONE) {
            Command.sendMessage("Player isn't in Hole, Packetflying, Burrowed");
            setEnabled(false);
            return;
        }

        if (getHotbarSlot(Blocks.PISTON) == -1 && getHotbarSlot(Blocks.STICKY_PISTON) == -1) {
            Command.sendMessage("You don't have PISTON or STICKY_PISTON");
            setEnabled(false);
            return;
        }

        if (mode.getValue() == Mode.Torch && getHotbarSlot(Blocks.REDSTONE_TORCH) == -1) {
            Command.sendMessage("You don't have REDSTONE_TORCH");
            setEnabled(false);
            return;
        }

        if (mode.getValue() == Mode.Block && getHotbarSlot(Blocks.REDSTONE_BLOCK) == -1) {
            Command.sendMessage("You don't have REDSTONE_BLOCK");
            setEnabled(false);
            return;
        }

        if (!canPlace()) {
            Command.sendMessage("Don't have space");
            setEnabled(false);
        }
    }

    private int getRedstone() {
        int redstone_slot;
        if (mode.getValue() == Mode.Block) {
            redstone_slot = getHotbarSlot(Blocks.REDSTONE_BLOCK);
        } else {
            redstone_slot = getHotbarSlot(Blocks.REDSTONE_TORCH);
        }
        return redstone_slot;
    }

    private int getPiston() {
        int piston_slot;
        if (getHotbarSlot(Blocks.PISTON) != -1) {
            piston_slot = getHotbarSlot(Blocks.PISTON);
        } else {
            piston_slot = getHotbarSlot(Blocks.STICKY_PISTON);
        }
        return piston_slot;
    }

}