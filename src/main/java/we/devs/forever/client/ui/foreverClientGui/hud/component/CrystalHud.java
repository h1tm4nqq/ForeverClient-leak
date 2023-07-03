package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class CrystalHud extends Hud {

    private static final ItemStack endcrystal = new ItemStack(Items.END_CRYSTAL);

    public CrystalHud() {
        super("Crystal", true);
        width = 16;
        height = 16;
    }

    @Override
    public void onRenderHud() {
        int crystals = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.END_CRYSTAL).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL)
            crystals += mc.player.getHeldItemOffhand().getCount();
        if (crystals > 0) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(endcrystal, X.getValue().intValue(), Y.getValue().intValue());
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, endcrystal, X.getValue().intValue(), Y.getValue().intValue(), "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(crystals + "", X.getValue() + 19 - 2 - renderer.getStringWidth(crystals + ""), Y.getValue() + 9, 0xffffff);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }
}
