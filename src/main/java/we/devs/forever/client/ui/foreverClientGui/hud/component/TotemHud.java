package we.devs.forever.client.ui.foreverClientGui.hud.component;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.util.render.RenderUtil;
import we.devs.forever.client.ui.foreverClientGui.hud.Hud;

public class TotemHud extends Hud {

    private static final ItemStack totemofundying = new ItemStack(Items.TOTEM_OF_UNDYING);

    public TotemHud() {
        super("Totem", true);
        width = 16;
        height = 16;
    }

    @Override
    public void onRenderHud() {
        int totems = mc.player.inventory.mainInventory.stream().filter(itemStack -> itemStack.getItem() == Items.TOTEM_OF_UNDYING).mapToInt(ItemStack::getCount).sum();
        if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING)
            totems += mc.player.getHeldItemOffhand().getCount();
        if (totems > 0) {
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(totemofundying, X.getValue().intValue(), Y.getValue().intValue());
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, totemofundying, X.getValue().intValue(), Y.getValue().intValue(), "");
            RenderUtil.itemRender.zLevel = 0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            renderer.drawStringWithShadow(totems + "", X.getValue() + 19 - 2 - renderer.getStringWidth(totems + ""), Y.getValue() + 9, 0xffffff);
            GlStateManager.enableDepth();
            GlStateManager.disableLighting();
        }
    }
}
