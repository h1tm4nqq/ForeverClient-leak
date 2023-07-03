
package we.devs.forever.mixin.mixins.render;


import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.ForgeHooksClient;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import we.devs.forever.client.modules.impl.render.ItemPhysics;

import java.util.Random;

@Mixin(value = {RenderEntityItem.class})
public abstract class MixinRenderEntityItem
        extends MixinRenderer<EntityItem> {
    private final Minecraft mc = Minecraft.getMinecraft();
    @Shadow
    @Final
    private RenderItem itemRenderer;
    @Shadow
    @Final
    private Random random;
    private long tick;

    @Shadow
    protected abstract int getModelCount(ItemStack var1);

    @Shadow
    public abstract boolean shouldSpreadItems();

    @Shadow
    public abstract boolean shouldBob();

    @Shadow
    protected abstract ResourceLocation getEntityTexture(EntityItem var1);

    private double formPositive(float rotationPitch) {
        return rotationPitch > 0.0f ? (double) rotationPitch : (double) (-rotationPitch);
    }


    /**
     * @author hitmanqq and fucking nn
     * @reason idk
     */
    @Overwrite
    private int transformModelCount(EntityItem itemIn, double x, double y, double z, float p_177077_8_, IBakedModel p_177077_9_) {
        if (ItemPhysics.INSTANCE.isEnabled()) {
            ItemStack itemstack = itemIn.getItem();
            itemstack.getItem();
            boolean flag = p_177077_9_.isAmbientOcclusion();
            int i = this.getModelCount(itemstack);
            float f2 = 0.0f;
            GlStateManager.translate((float) x, (float) y + 0.0f + 0.1f, (float) z);
            float f3 = 0.0f;
            if (flag || this.mc.getRenderManager().options != null && this.mc.getRenderManager().options.fancyGraphics) {
                GlStateManager.rotate(f3, 0.0f, 1.0f, 0.0f);
            }
            if (!flag) {
                f3 = -0.0f * (float) (i - 1) * 0.5f;
                float f4 = -0.0f * (float) (i - 1) * 0.5f;
                float f5 = -0.046875f * (float) (i - 1) * 0.5f;
                GlStateManager.translate(f3, f4, f5);
            }
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            return i;
        }
        ItemStack itemstack = itemIn.getItem();
        itemstack.getItem();
        boolean flag = p_177077_9_.isGui3d();
        int i = this.getModelCount(itemstack);
        float f1 = this.shouldBob() ? MathHelper.sin(((float) itemIn.getAge() + p_177077_8_) / 10.0f + itemIn.hoverStart) * 0.1f + 0.1f : 0.0f;
        float f2 = p_177077_9_.getItemCameraTransforms().getTransform(ItemCameraTransforms.TransformType.GROUND).scale.y;
        GlStateManager.translate((float) x, (float) y + f1 + 0.25f * f2, (float) z);
        if (flag || this.renderManager.options != null) {
            float f3 = (((float) itemIn.getAge() + p_177077_8_) / 20.0f + itemIn.hoverStart) * 57.295776f;
            GlStateManager.rotate(f3, 0.0f, 1.0f, 0.0f);
        }
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        return i;
    }

    /**
     * @author hitmanqq and fucking nn
     * @reason idk
     */
    @Overwrite
    public void doRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (ItemPhysics.INSTANCE.isEnabled()) {
            double rotation = (double) (System.nanoTime() - this.tick) / 3000000.0;
            if (!this.mc.inGameHasFocus) {
                rotation = 0.0;
            }
            ItemStack itemstack = entity.getItem();
            itemstack.getItem();
            this.random.setSeed(187L);
            this.mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.mc.getRenderManager().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).setBlurMipmap(false, false);
            GlStateManager.enableRescaleNormal();
            GlStateManager.alphaFunc(516, 0.1f);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.pushMatrix();
            IBakedModel ibakedmodel = this.itemRenderer.getItemModelMesher().getItemModel(itemstack);
            int i = this.transformModelCount(entity, x, y, z, partialTicks, ibakedmodel);
            BlockPos blockpos = new BlockPos(entity);
            if (entity.rotationPitch > 360.0f) {
                entity.rotationPitch = 0.0f;
            }
            if (!(Double.isNaN(entity.posX) || Double.isNaN(entity.posY) || Double.isNaN(entity.posZ) || entity.world == null)) {
                if (entity.onGround) {
                    if (entity.rotationPitch != 0.0f && entity.rotationPitch != 90.0f && entity.rotationPitch != 180.0f && entity.rotationPitch != 270.0f) {
                        double d0 = this.formPositive(entity.rotationPitch);
                        double d2 = this.formPositive(entity.rotationPitch - 90.0f);
                        double d3 = this.formPositive(entity.rotationPitch - 180.0f);
                        double d4 = this.formPositive(entity.rotationPitch - 270.0f);
                        if (d0 <= d2 && d0 <= d3 && d0 <= d4) {
                            entity.rotationPitch = entity.rotationPitch < 0.0f ? (entity.rotationPitch += (float) rotation) : (entity.rotationPitch -= (float) rotation);
                        }
                        if (d2 < d0 && d2 <= d3 && d2 <= d4) {
                            entity.rotationPitch = entity.rotationPitch - 90.0f < 0.0f ? (entity.rotationPitch += (float) rotation) : (entity.rotationPitch -= (float) rotation);
                        }
                        if (d3 < d2 && d3 < d0 && d3 <= d4) {
                            entity.rotationPitch = entity.rotationPitch - 180.0f < 0.0f ? (entity.rotationPitch += (float) rotation) : (entity.rotationPitch -= (float) rotation);
                        }
                        if (d4 < d2 && d4 < d3 && d4 < d0) {
                            entity.rotationPitch = entity.rotationPitch - 270.0f < 0.0f ? (entity.rotationPitch += (float) rotation) : (entity.rotationPitch -= (float) rotation);
                        }
                    }
                } else {
                    BlockPos blockpos2 = new BlockPos(entity);
                    blockpos2.add(0, 1, 0);
                    Material material = entity.world.getBlockState(blockpos2).getMaterial();
                    Material material2 = entity.world.getBlockState(blockpos).getMaterial();
                    boolean flag2 = entity.isInsideOfMaterial(Material.WATER);
                    boolean flag3 = entity.isInWater();
                    entity.rotationPitch = flag2 | material == Material.WATER | material2 == Material.WATER | flag3 ? (entity.rotationPitch += (float) (rotation / 4.0)) : (entity.rotationPitch += (float) (rotation * 2.0));
                }
            }
            GL11.glRotatef(entity.rotationYaw, 0.0f, 1.0f, 0.0f);
            GL11.glRotatef(entity.rotationPitch + 90.0f, 1.0f, 0.0f, 0.0f);
            for (int j = 0; j < i; ++j) {
                if (ibakedmodel.isAmbientOcclusion()) {
                    GlStateManager.pushMatrix();
                    if(entity.getItem().getItem() instanceof ItemBlock) {
                        GlStateManager.scale(ItemPhysics.INSTANCE.scalingBlock.getValue(), ItemPhysics.INSTANCE.scalingBlock.getValue(), ItemPhysics.INSTANCE.scalingBlock.getValue());
                    } else  {
                        GlStateManager.scale(ItemPhysics.INSTANCE.scalingItem.getValue(), ItemPhysics.INSTANCE.scalingItem.getValue(), ItemPhysics.INSTANCE.scalingItem.getValue());

                    }
                    this.itemRenderer.renderItem(itemstack, ibakedmodel);
                    GlStateManager.popMatrix();
                    continue;
                }
                GlStateManager.pushMatrix();
                if (j > 0 && this.shouldSpreadItems()) {
                    GlStateManager.translate(0.0f, 0.0f, 0.046875f * (float) j);
                }
                this.itemRenderer.renderItem(itemstack, ibakedmodel);
                if (!this.shouldSpreadItems()) {
                    GlStateManager.translate(0.0f, 0.0f, 0.046875f);
                }
                GlStateManager.popMatrix();
            }
            GlStateManager.popMatrix();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            this.mc.getRenderManager().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
            this.mc.getRenderManager().renderEngine.getTexture(TextureMap.LOCATION_BLOCKS_TEXTURE).restoreLastBlurMipmap();
            return;
        }
        ItemStack itemstack = entity.getItem();
        int i = itemstack.isEmpty() ? 187 : Item.getIdFromItem(itemstack.getItem()) + itemstack.getMetadata();
        this.random.setSeed(i);
        boolean flag = false;
        if (this.bindEntityTexture(entity)) {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);
            flag = true;
        }
        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1f);
        GlStateManager.enableBlend();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entity.world, null);
        int j = this.transformModelCount(entity, x, y, z, partialTicks, ibakedmodel);
        boolean flag1 = ibakedmodel.isGui3d();
        if (!flag1) {
            float f3 = -0.0f * (float) (j - 1) * 0.5f;
            float f4 = -0.0f * (float) (j - 1) * 0.5f;
            float f5 = -0.09375f * (float) (j - 1) * 0.5f;
            GlStateManager.translate(f3, f4, f5);
        }
        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.enableOutlineMode(this.getTeamColor(entity));
        }
        for (int k = 0; k < j; ++k) {
            IBakedModel transformedModel;
            GlStateManager.pushMatrix();
            if (flag1) {
                if (k > 0) {
                    float f7 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float f9 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    float f6 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f;
                    GlStateManager.translate(this.shouldSpreadItems() ? f7 : 0.0f, this.shouldSpreadItems() ? f9 : 0.0f, f6);
                }
                transformedModel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
                this.itemRenderer.renderItem(itemstack, transformedModel);
                GlStateManager.popMatrix();
                continue;
            }
            if (k > 0) {
                float f8 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                float f10 = (this.random.nextFloat() * 2.0f - 1.0f) * 0.15f * 0.5f;
                GlStateManager.translate(f8, f10, 0.0f);
            }
            transformedModel = ForgeHooksClient.handleCameraTransforms(ibakedmodel, ItemCameraTransforms.TransformType.GROUND, false);
            this.itemRenderer.renderItem(itemstack, transformedModel);
            GlStateManager.popMatrix();
            GlStateManager.translate(0.0f, 0.0f, 0.09375f);
        }
        if (this.renderOutlines) {
            GlStateManager.disableOutlineMode();
            GlStateManager.disableColorMaterial();
        }
        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);
        if (flag) {
            this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
        }
    }
}

