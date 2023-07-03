package we.devs.forever.client.modules.impl.movement;

import net.minecraft.init.MobEffects;
import we.devs.forever.api.event.events.player.TravelEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.MathUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public final class Levitation extends Module {
    public Levitation() {
        super("Levitation", "Modify levitation effect", Category.MOVEMENT);
    }
    private final Setting<Mode> mode = (new Setting<>("Mode", Mode.Anti));
    private final Setting<Float> verticalSpeed = (new Setting<>("VerticalSpeed", 0.5f, 0.1f, 1.0f, v -> mode.getValue() == Mode.Control));
    private final Setting<Float> horizontalSpeed = (new Setting<>("HorizontalSpeed", 0.5f, 0.1f, 1.0f, v -> mode.getValue() == Mode.Control));

    @Override
    public void onUpdate() {
        if (this.mode.getValue() == Mode.Anti) {
            if (mc.player != null) {
                if (mc.player.isPotionActive(MobEffects.LEVITATION)) {
                    mc.player.removeActivePotionEffect(MobEffects.LEVITATION);
                }
            }
        }
    }

    @EventListener
    public void onTravel(TravelEvent event) {
        if (this.mode.getValue() == Mode.Control) {
            if (mc.player == null || mc.player.isRiding())
                return;
            if (!mc.player.isPotionActive(MobEffects.LEVITATION))
                return;
            mc.player.setVelocity(0, 0, 0);
            final double[] dir = MathUtil.directionSpeed(horizontalSpeed.getValue());
            if (mc.player.movementInput.moveStrafe != 0 || mc.player.movementInput.moveForward != 0) {
                mc.player.motionX = dir[0];
                mc.player.motionZ = dir[1];
            }
            if (mc.player.movementInput.jump && !mc.player.isElytraFlying())
                mc.player.motionY = verticalSpeed.getValue();
            if (mc.player.movementInput.sneak)
                mc.player.motionY = -verticalSpeed.getValue();
            event.cancel();
            mc.player.prevLimbSwingAmount = 0;
            mc.player.limbSwingAmount = 0;
            mc.player.limbSwing = 0;
        }
    }

    public enum Mode {
        Anti,
        Control
    }
}