package we.devs.forever.client.modules.impl.movement;

import we.devs.forever.api.event.events.player.MoveEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public class FastSwim extends Module {
    public final Setting<Double> waterHorizontal = (new Setting<>("WaterHorizontal", 3.0, 1.0, 20.0, "Y speed in water"));
    public final Setting<Double> waterVertical = (new Setting<>("WaterVertical", 3.0, 1.0, 20.0, "X speed in water"));
    public final Setting<Double> lavaHorizontal = (new Setting<>("LavaHorizontal", 4.0, 1.0, 20.0, "Y speed in lava"));
    public final Setting<Double> lavaVertical = (new Setting<>("LavaVertical", 4.0, 1.0, 20.0, "X speed in lava"));

    public FastSwim() {
        super("FastSwim", "Swims fast", Module.Category.MOVEMENT);
    }

    @EventListener
    public void onMove(MoveEvent moveEvent) {
        if (FastSwim.mc.player.isInLava() && !FastSwim.mc.player.onGround) {
            moveEvent.setX(moveEvent.getX() * this.lavaHorizontal.getValue());
            moveEvent.setY(moveEvent.getY() * this.lavaVertical.getValue());
            moveEvent.setZ(moveEvent.getZ() * this.lavaVertical.getValue());
        } else if (FastSwim.mc.player.isInWater() && !FastSwim.mc.player.onGround) {
            moveEvent.setX(moveEvent.getX() * this.waterHorizontal.getValue());
            moveEvent.setY(moveEvent.getY() * this.waterVertical.getValue());
            moveEvent.setZ(moveEvent.getZ() * this.waterHorizontal.getValue());
        }
    }
}