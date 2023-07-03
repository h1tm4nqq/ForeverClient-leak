package we.devs.forever.mixin.mixins.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.tileentity.TileEntityEnchantmentTableRenderer;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.forever.client.modules.impl.render.NoRender;

@Mixin(TileEntityEnchantmentTableRenderer.class)
public class MixinTileEntityEnchantmentTableRenderer {

    @Inject(method = "render*", at = @At("HEAD"), cancellable = true)
    public void renderBook(TileEntityEnchantmentTable te, double x, double y, double z, float partialTicks, int destroyStage, float alpha, CallbackInfo info) {
        if (NoRender.getInstance().isEnabled() && NoRender.getInstance().tileEntity.getValue()) {
            if (NoRender.getInstance().enchantmentTable.getValue() && new BlockPos(x, y, z).getDistance((int) Minecraft.getMinecraft().player.posX, (int) Minecraft.getMinecraft().player.posY, (int) Minecraft.getMinecraft().player.posZ) > NoRender.getInstance().tileRange.getValue()) {
                info.cancel();
            }
        }
    }
}