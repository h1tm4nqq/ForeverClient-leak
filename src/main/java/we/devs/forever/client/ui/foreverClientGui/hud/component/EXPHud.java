package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class EXPHud extends Hud {

    private static final ItemStack exp = new ItemStack(Items.EXPERIENCE_BOTTLE);

    public EXPHud() {
        super("EXP", true);
        width = 16;
        height = 16;
    }

    @Override
    public void onRenderHud() {
        int exps = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.EXPERIENCE_BOTTLE).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.EXPERIENCE_BOTTLE)
            exps += mc.player.getHeldItemOffhand().getCount();
        if (exps > 0) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(exp, X.getValue().intValue(), Y.getValue().intValue());
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, exp, X.getValue().intValue(), Y.getValue().intValue(), "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(exps + "", X.getValue() + 19 - 2 - renderer.getStringWidth(String.valueOf(exps)), Y.getValue() + 9, 0xffffff);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }
}
