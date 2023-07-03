package we.devs.forever.client.modules.impl.player;

import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.world.BlockEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class BlockTweaks
        extends Module {
    public Setting<Boolean> autoTool = (new Setting<>("AutoTool", false));
    public Setting<Boolean> autoWeapon = (new Setting<>("AutoWeapon", false));
    public Setting<Boolean> noFriendAttack = (new Setting<>("NoFriendAttack", false));
    public Setting<Boolean> noBlock = (new Setting<>("NoHitboxBlock", true));
    public Setting<Boolean> noGhost = (new Setting<>("NoGlitchBlocks", false));
    public Setting<Boolean> destroy = (new Setting<>("Destroy", false, v -> this.noGhost.getValue()));
    private int lastHotbarSlot = -1;
    private int currentTargetSlot = -1;
    private boolean switched;

    public BlockTweaks() {
        super("BlockTweaks", "Some tweaks for blocks.", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        if (this.switched) {
            this.equip(this.lastHotbarSlot, false);
        }
        this.lastHotbarSlot = -1;
        this.currentTargetSlot = -1;
    }

    @EventListener
    public void onBreak(BlockEvent.BreakEvent event) {
        if (BlockTweaks.fullNullCheck() || !this.noGhost.getValue() || !this.destroy.getValue()) {
            return;
        }
        if (!(BlockTweaks.mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock)) {
            BlockPos pos = BlockTweaks.mc.player.getPosition();
            this.removeGlitchBlocks(pos);
        }
    }

 /*     @Listener
    public void onBlockInteract(PlayerInteractEvent.LeftClickBlock event) {
        if (this.autoTool.getValue() && (Speedmine.getInstance().mode.getValue() != Speedmine.Mode.PACKET || Speedmine.getInstance().isDisabled() || !Speedmine.getInstance().tweaks.getValue()) && !BlockTweaks.fullNullCheck()) {
            event.getPos();
            this.equipBestTool(BlockTweaks.mc.world.getBlockState(event.getPos()));
        }
    }

  */

    @EventListener
    public void onAttack(AttackEntityEvent event) {
        if (this.autoWeapon.getValue() && !BlockTweaks.fullNullCheck() && event.getTarget() != null) {
            this.equipBestWeapon(event.getTarget());
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        Entity entity;
        if (BlockTweaks.fullNullCheck()) {
            return;
        }
        if (this.noFriendAttack.getValue() && event.getPacket() instanceof CPacketUseEntity && (entity = ((CPacketUseEntity) event.getPacket()).getEntityFromWorld(BlockTweaks.mc.world)) != null && friendManager.isFriend(entity.getName())) {
            event.cancel();
        }
    }

    @Override
    public void onUpdate() {
        if (!BlockTweaks.fullNullCheck()) {
            if (BlockTweaks.mc.player.inventory.currentItem != this.lastHotbarSlot && BlockTweaks.mc.player.inventory.currentItem != this.currentTargetSlot) {
                this.lastHotbarSlot = BlockTweaks.mc.player.inventory.currentItem;
            }
            if (!BlockTweaks.mc.gameSettings.keyBindAttack.isKeyDown() && this.switched) {
                this.equip(this.lastHotbarSlot, false);
            }
        }
    }

    private void removeGlitchBlocks(BlockPos pos) {
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

    private void equipBestTool(IBlockState blockState) {
        int bestSlot = -1;
        double max = 0.0;
        for (int i = 0; i < 9; ++i) {
            int eff;
            float speed;
            ItemStack stack = BlockTweaks.mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty || !((speed = stack.getDestroySpeed(blockState)) > 1.0f) || !((double) (speed = (float) ((double) speed + ((eff = EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, stack)) > 0 ? Math.pow(eff, 2.0) + 1.0 : 0.0))) > max))
                continue;
            max = speed;
            bestSlot = i;
        }
        this.equip(bestSlot, true);
    }

    public void equipBestWeapon(Entity entity) {
        int bestSlot = -1;
        double maxDamage = 0.0;
        EnumCreatureAttribute creatureAttribute = EnumCreatureAttribute.UNDEFINED;
        if (EntityUtil.isLiving(entity)) {
            EntityLivingBase base = (EntityLivingBase) entity;
            creatureAttribute = base.getCreatureAttribute();
        }
        for (int i = 0; i < 9; ++i) {
            double damage;
            ItemStack stack = BlockTweaks.mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty) continue;
            if (stack.getItem() instanceof ItemTool) {
                damage = (double) ((ItemTool) stack.getItem()).attackDamage + (double) EnchantmentHelper.getModifierForCreature(stack, creatureAttribute);
                if (!(damage > maxDamage)) continue;
                maxDamage = damage;
                bestSlot = i;
                continue;
            }
            if (!(stack.getItem() instanceof ItemSword) || !((damage = (double) ((ItemSword) stack.getItem()).getAttackDamage() + (double) EnchantmentHelper.getModifierForCreature(stack, creatureAttribute)) > maxDamage))
                continue;
            maxDamage = damage;
            bestSlot = i;
        }
        this.equip(bestSlot, true);
    }

    private void equip(int slot, boolean equipTool) {
        if (slot != -1) {
            if (slot != BlockTweaks.mc.player.inventory.currentItem) {
                this.lastHotbarSlot = BlockTweaks.mc.player.inventory.currentItem;
            }
            this.currentTargetSlot = slot;
            BlockTweaks.mc.player.inventory.currentItem = slot;
            BlockTweaks.mc.playerController.syncCurrentPlayItem();
            this.switched = equipTool;
        }
    }
}

