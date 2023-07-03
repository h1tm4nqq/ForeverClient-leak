package we.devs.forever.client.modules.impl.movement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.enums.RenderMode;
import we.devs.forever.api.util.enums.Swing;
import we.devs.forever.api.util.player.RotationType;
import we.devs.forever.api.util.player.inventory.SwitchUtil;
import we.devs.forever.api.util.render.blocks.BlockRenderUtil;
import we.devs.forever.api.util.world.BlockUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Scaffold extends Module {

    public Setting<AutoSwitch> switchMode = (new Setting<>("SwitchMode", AutoSwitch.Silent));

    public Setting<Swing> swing = (new Setting<>("Swing Arm", Swing.Mainhand));
    public Setting<RotationType> rotation = (new Setting<>("Rotate", RotationType.Legit));
    public Setting<Boolean> packet = (new Setting<>("Packet", false));
    public Setting<Boolean> strict = (new Setting<>("Strict", false));
    public Setting<Boolean> tower = (new Setting<>("Tower", true));
    public Setting<Boolean> keepY = (new Setting<>("KeepYLevel", false));
    public Setting<Boolean> sprint = (new Setting<>("UseSprint", true));
    public Setting<Boolean> down = (new Setting<>("Down", true));
    public Setting<Float> expand = (new Setting<>("Expand", 0.0f, 0.0f, 6.0f));



    public List<Block> invalid =
            Arrays.asList(Blocks.ENCHANTING_TABLE,
                    Blocks.FURNACE,
                    Blocks.CARPET,
                    Blocks.CRAFTING_TABLE,
                    Blocks.TRAPPED_CHEST,
                    Blocks.CHEST,
                    Blocks.DISPENSER,
                    Blocks.AIR,
                    Blocks.WATER,
                    Blocks.LAVA,
                    Blocks.FLOWING_WATER,
                    Blocks.FLOWING_LAVA,
                    Blocks.SNOW_LAYER,
                    Blocks.TORCH,
                    Blocks.ANVIL,
                    Blocks.JUKEBOX,
                    Blocks.STONE_BUTTON,
                    Blocks.WOODEN_BUTTON,
                    Blocks.LEVER,
                    Blocks.NOTEBLOCK,
                    Blocks.STONE_PRESSURE_PLATE,
                    Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
                    Blocks.WOODEN_PRESSURE_PLATE,
                    Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
                    Blocks.RED_MUSHROOM,
                    Blocks.BROWN_MUSHROOM,
                    Blocks.YELLOW_FLOWER,
                    Blocks.RED_FLOWER,
                    Blocks.ANVIL,
                    Blocks.CACTUS,
                    Blocks.LADDER,
                    Blocks.ENDER_CHEST);
    public TimerUtil timerMotion = new TimerUtil();
    TimerUtil timer = new TimerUtil();
    public SwitchUtil switchUtil = new SwitchUtil(switchMode);
    BlockPos pos;

    private int lastY;
    private boolean teleported;

    public Scaffold() {
        super("Scaffold", "Places Blocks underneath you.", Category.MOVEMENT);
    }


    public static boolean isMoving(EntityLivingBase entityLivingBase) {
        return entityLivingBase.moveForward == 0.0f && entityLivingBase.moveStrafing == 0.0f;
    }

    @Override
    public String getDisplayInfo() {
        return String.valueOf(mc.player.inventory.currentItem);
    }


    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventListener
    public void onUpdateWalkingPlayerPost(MotionEvent.Pre MotionEvent) {

        if (down.getValue() && mc.gameSettings.keyBindSneak.isKeyDown() && !sprint.getValue()) {
            mc.player.setSprinting(false);
        }
        if (keepY.getValue()) {
            if (isMoving(mc.player) && mc.gameSettings.keyBindJump.isKeyDown() || mc.player.collidedVertically || mc.player.onGround) {
                lastY = MathHelper.floor(mc.player.posY);
            }
        } else {
            lastY = MathHelper.floor(mc.player.posY);
        }

        double posX = mc.player.posX,
                posZ = mc.player.posZ,
                posY = keepY.getValue() ? (double) lastY : mc.player.posY;

        if (!mc.player.collidedHorizontally) {
            double[] temp = getExpandCoords(
                    posX,
                    posZ,
                    mc.player.movementInput.moveForward,
                    mc.player.movementInput.moveStrafe,
                    mc.player.rotationYaw);
            posX = temp[0];
            posZ = temp[1];
        }

        if (!canPlace(mc.world.getBlockState(new BlockPos(posX, mc.player.posY - (double) (mc.gameSettings.keyBindSneak.isKeyDown()
                && down.getValue() ? 2 : 1), posZ)).getBlock())) {
            posX = mc.player.posX;
            posZ = mc.player.posZ;
        }

        pos = new BlockPos(posX, posY - 1.0 , posZ);//

        if (mc.gameSettings.keyBindSneak.isKeyDown() && down.getValue()) {
            pos = new BlockPos(posX, posY - 2.0, posZ);
        }

        if (BlockUtil.isPosEmpty(pos)) {
            if (mc.gameSettings.keyBindJump.isKeyDown() && mc.player.moveForward == 0.0f && mc.player.moveStrafing == 0.0f && !mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                mc.player.motionY = 0.42f;
                mc.player.motionZ = 0.0;
                mc.player.motionX = 0.0;
                if (!tower.getValue()) {
                    mc.player.motionY = -0.28;
                }
            } else {
                timerMotion.reset();
            }
            switchUtil.switchTo(() -> {
                if(mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock) return -1;
                for (int i = 0; i < 9; ++i) {
                    if (!(mc.player.inventory.getStackInSlot(i).getItem() instanceof ItemBlock) || invalid.contains(((ItemBlock) mc.player.inventory.getStackInSlot(i).getItem()).getBlock()))
                        continue;
                    return i;
                }
                return -1;
            });
            placeManager.place(pos,rotation.getValue(), swing.getValue(),packet.getValue(),strict.getValue(),false,true);
            switchUtil.switchBack();
        }
    }

    public double[] getExpandCoords(double x, double z, double forward, double strafe, float YAW) {
        BlockPos underPos = new BlockPos(x, mc.player.posY - (double) (mc.gameSettings.keyBindSneak.isKeyDown() && down.getValue() ? 2 : 1), z);
        Block underBlock = mc.world.getBlockState(underPos).getBlock();
        double xCalc = -999.0D;
        double zCalc = -999.0D;
        double dist = 0.0D;

        for (double expandDist = expand.getValue() * 2.0F; canPlace(underBlock); underBlock = mc.world.getBlockState(underPos).getBlock()) {
            ++dist;
            if (dist > expandDist) {
                dist = expandDist;
            }

            double cos = Math.cos(Math.toRadians(YAW + 90.0F));
            double sin = Math.sin(Math.toRadians(YAW + 90.0F));
            xCalc = x + (forward * 0.45D * cos + strafe * 0.45D * sin) * dist;
            zCalc = z + (forward * 0.45D * sin - strafe * 0.45D * cos) * dist;
            if (dist == expandDist) {
                break;
            }

            underPos = new BlockPos(xCalc, mc.player.posY - (double) (mc.gameSettings.keyBindSneak.isKeyDown() && down.getValue() ? 2 : 1), zCalc);
        }

        return new double[]{xCalc, zCalc};
    }

    public boolean canPlace(Block block) {
        return (!(block instanceof BlockAir) && !(block instanceof BlockLiquid)) || mc.world == null || mc.player == null || pos == null || !mc.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos)).isEmpty();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (!fullNullCheck() && pos != null) {
            BlockRenderUtil.drawBlock(pos, Color.BLACK, Color.PINK, 5F, RenderMode.Wireframe);
        }
    }

    private int getBlockCountHotbar() {
        int n = 0;
        for (int i = 36; i < 45; ++i) {
            if (!mc.player.inventoryContainer.getSlot(i).getHasStack()) continue;
            ItemStack itemStack = mc.player.inventoryContainer.getSlot(i).getStack();
            Item item = itemStack.getItem();
            if (!(itemStack.getItem() instanceof ItemBlock) || invalid.contains(((ItemBlock) item).getBlock()))
                continue;
            n += itemStack.getCount();
        }
        return n;
    }



    private static class BlockData {
        public BlockPos position;
        public EnumFacing face;

        public BlockData(BlockPos blockPos, EnumFacing enumFacing) {
            position = blockPos;
            face = enumFacing;
        }
    }
}