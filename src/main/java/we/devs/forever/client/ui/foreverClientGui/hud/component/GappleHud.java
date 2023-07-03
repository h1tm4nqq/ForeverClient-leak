package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class GappleHud extends Hud {

    private static final ItemStack goldenapple = new ItemStack(Items.GOLDEN_APPLE);

    public GappleHud() {
        super("Gapple", true);
        width = 16;
        height = 16;
    }

    @Override
    public void onRenderHud() {
        int gapples = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.GOLDEN_APPLE).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE)
            gapples += mc.player.getHeldItemOffhand().getCount();
        if (gapples > 0) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(goldenapple, X.getValue().intValue(), Y.getValue().intValue());
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, goldenapple, X.getValue().intValue(), Y.getValue().intValue(), "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(gapples + "", X.getValue() + 19 - 2 - renderer.getStringWidth(gapples + ""), Y.getValue() + 9, 0xffffff);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }
}
