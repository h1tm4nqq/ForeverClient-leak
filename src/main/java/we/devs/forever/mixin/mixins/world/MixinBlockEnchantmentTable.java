package we.devs.forever.mixin.mixins.world;

import net.minecraft.block.BlockEnchantmentTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.modules.impl.render.NoRender;

import java.util.Random;

@Mixin(BlockEnchantmentTable.class)
public class MixinBlockEnchantmentTable {

    @Inject(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDDDD[I)V"), cancellable = true)
    public void enchantmentParticle(IBlockState stateIn, World worldIn, BlockPos pos, Random rand, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().tileEntity.getValue()) {
            if (NoRender.getInstance().enchantmentTable.getValue() && pos.getDistance((int) Minecraft.getMinecraft().player.posX, (int) Minecraft.getMinecraft().player.posY, (int) Minecraft.getMinecraft().player.posZ) > NoRender.getInstance().tileRange.getValue() ) {
                info.cancel();
            }
        }
    }
}