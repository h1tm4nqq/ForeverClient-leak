package we.devs.forever.api.util.combat;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockAir;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.util.math.MathUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.ui.alts.tools.Pair;

import java.util.UUID;

public class PredictUtil extends Client {
    public static Pair<EntityOtherPlayerMP, EntityPlayer> getPredictedEntity(EntityPlayer entity, int ticks, boolean collision) {
        Vec3d pos = entity.getPositionVector().add(MathUtil.predict(entity, ticks,collision ));
        EntityOtherPlayerMP clonedPlayer = new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("fdee323e-7f0c-4c15-8d1c-0f277442342a"), entity.getName()));
        clonedPlayer.setPosition(pos.x, pos.y, pos.z);
        clonedPlayer.inventory.copyInventory(entity.inventory);
        clonedPlayer.setHealth(entity.getHealth());
        clonedPlayer.prevPosX = entity.prevPosX;
        clonedPlayer.prevPosY = entity.prevPosY;
        clonedPlayer.prevPosZ = entity.prevPosZ;
        for (PotionEffect effect : entity.getActivePotionEffects()) {
            clonedPlayer.addPotionEffect(effect);
        }
        return new Pair<>(clonedPlayer,entity);
    }

    public static Vec3d getMotionVec(Entity entity, int ticks, boolean collision) {
        double dX = entity.posX - entity.prevPosX;
        double dZ = entity.posZ - entity.prevPosZ;
        double entityMotionPosX = 0;
        double entityMotionPosZ = 0;
        if (collision) {
            for (int i = 1; i <= ticks; i++) {
                if (mc.world.getBlockState(new BlockPos(entity.posX + dX * i, entity.posY, entity.posZ + dZ * i)).getBlock() instanceof BlockAir) {
                    entityMotionPosX = dX * i;
                    entityMotionPosZ = dZ * i;
                } else {
                    break;
                }
            }
        } else {
            entityMotionPosX = dX * ticks;
            entityMotionPosZ = dZ * ticks;
        }

        return new Vec3d(entityMotionPosX, 0, entityMotionPosZ);
    }


}
