package we.devs.forever.client.modules.impl.combat;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.util.EnumHand;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class AutoTotem extends Module {
    private final Setting<Integer> delay = (new Setting<>("Delay", 0, 0, 20));
    private final Setting<Boolean> cancelMotion = (new Setting<>("CancelMotion", false));
    private TimerUtil timer = new TimerUtil();
    private boolean hasTotem = false;

    public AutoTotem() {
        super("AutoTotem", "Forces totem into offhand", Category.COMBAT);
    }

    @EventListener(priority = ListenerPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (!hasTotem) {
            timer.reset();
        }
        if (mc.player == null || mc.world == null) return;

        if (!(mc.currentScreen instanceof GuiContainer && !(mc.currentScreen instanceof GuiInventory) || mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.TOTEM_OF_UNDYING || mc.player.isCreative())) {
            int index = 44;
            while (index >= 9) {
                if (mc.player.inventoryContainer.getSlot(index).getStack().getItem() == Items.TOTEM_OF_UNDYING) {
                    hasTotem = true;

                    if (timer.passedMs((long) (delay.getValue() * 100F)) && mc.player.inventory.getItemStack().getItem() != Items.TOTEM_OF_UNDYING) {
                        if (cancelMotion.getValue() && mc.player.motionX * mc.player.motionX + mc.player.motionY * mc.player.motionY + mc.player.motionZ * mc.player.motionZ >= 9.0E-4D) {
                            mc.player.motionX = 0D;
                            mc.player.motionY = 0D;
                            mc.player.motionZ = 0D;
                            return;
                        }
                        mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                    }

                    if (timer.passedMs((long) (delay.getValue() * 200F)) && mc.player.inventory.getItemStack().getItem() == Items.TOTEM_OF_UNDYING) {
                        if (cancelMotion.getValue() && mc.player.motionX * mc.player.motionX + mc.player.motionY * mc.player.motionY + mc.player.motionZ * mc.player.motionZ >= 9.0E-4D) {
                            mc.player.motionX = 0D;
                            mc.player.motionY = 0D;
                            mc.player.motionZ = 0D;
                            return;
                        }
                        mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
                        if (mc.player.inventory.getItemStack().isEmpty()) {
                            hasTotem = false;
                            return;
                        }
                    }

                    if (timer.passedMs((long) (delay.getValue() * 300F)) && !mc.player.inventory.getItemStack().isEmpty() && mc.player.getHeldItem(EnumHand.OFF_HAND).getItem() == Items.TOTEM_OF_UNDYING) {
                        if (cancelMotion.getValue() && mc.player.motionX * mc.player.motionX + mc.player.motionY * mc.player.motionY + mc.player.motionZ * mc.player.motionZ >= 9.0E-4D) {
                            mc.player.motionX = 0D;
                            mc.player.motionY = 0D;
                            mc.player.motionZ = 0D;
                            return;
                        }
                        mc.playerController.windowClick(0, index, 0, ClickType.PICKUP, mc.player);
                        hasTotem = false;
                        return;
                    }
                }
                index--;
            }
        }
    }

    public void onEnable() {
        hasTotem = false;
    }
}