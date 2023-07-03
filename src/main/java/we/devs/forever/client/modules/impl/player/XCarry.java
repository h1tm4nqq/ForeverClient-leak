package we.devs.forever.client.modules.impl.player;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.ReflectionUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class XCarry extends Module {
    public XCarry() {
        super("XCarry", "MoreInventory",Category.PLAYER);
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        if(event.getPacket() instanceof CPacketCloseWindow) {
            event.setCanceled(true);
        }
    }

}
