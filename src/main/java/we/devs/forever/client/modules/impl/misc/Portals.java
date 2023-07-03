package we.devs.forever.client.modules.impl.misc;

import net.minecraft.network.play.client.CPacketConfirmTeleport;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class Portals extends Module {
    private static Portals INSTANCE;

    static {
        Portals.INSTANCE = new Portals();
    }

    public Setting<Boolean> portalChat;
    public Setting<Boolean> godmode;
    public Setting<Boolean> fastPortal;
    public Setting<Integer> cooldown;
    public Setting<Integer> time;

    public Portals() {
        super("Portals", "Tweaks for Portals", Category.MISC);
        this.portalChat = (Setting<Boolean>) (new Setting<>("Chat", true, "Allows you to chat in portals."));
        this.godmode = (Setting<Boolean>) (new Setting<>("Godmode", false, "Portal Godmode."));
        this.fastPortal = (Setting<Boolean>) (new Setting<>("FastPortal", false));
        this.cooldown = (Setting<Integer>) (new Setting<>("Cooldown", 5, 1, 10, "Portal cooldown.", v -> this.fastPortal.getValue()));
        this.time = (Setting<Integer>) (new Setting<>("Time", 5, 0, 80, "Time in Portal", v -> this.fastPortal.getValue()));
    }

    @Override
    public String getDisplayInfo() {
        if (this.godmode.getValue()) {
            return "Godmode";
        }
        return null;
    }

    @EventListener
    public void onPacketSend(final PacketEvent.Send event) {
        if (event.getStage() == 0 && this.godmode.getValue() && event.getPacket() instanceof CPacketConfirmTeleport) {
            event.cancel();
        }
    }
}
