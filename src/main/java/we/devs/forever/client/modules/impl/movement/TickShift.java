package we.devs.forever.client.modules.impl.movement;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.client.ClientEvent;
import we.devs.forever.api.event.events.client.KeyEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.modules.impl.exploit.packetfly.PacketFly;
import we.devs.forever.client.modules.impl.player.Blink;
import we.devs.forever.client.setting.Bind;
import we.devs.forever.client.setting.Setting;

// this declares the module, usually you will do a constructor method with a super keyword
public class TickShift extends Module {
    // the option to disable after the tickshift happens
    public Setting<Boolean> disable = (new Setting<>("Disable", true));
    public Setting<Boolean> resetOnDisable = (new Setting<>("ResetOnDisable", true));
    public Setting<Boolean> bypass = (new Setting<>("Bypass", false));
    public Setting<Bind> bind = (new Setting<>("Start", Bind.none(), v -> bypass.getValue()));
    public Setting<Boolean> blink = (new Setting<>("Blink", false, "Automatically enables Blink", v -> bypass.getValue()));
    public Setting<Boolean> autoStep = new Setting<>("AutoStep", false, "Automatically enables Step", v -> bypass.getValue());
    public Setting<Integer> waitTime = (new Setting<>("WaitTime", 20, 1, 60, v -> bypass.getValue()));
    public Setting<Integer> disableTicksBypass = (new Setting<>("DisableTicksBypass", 70, 1, 200, v -> bypass.getValue()));
    // the option to only turn on timer when the player moves
    public Setting<Integer> enableTicksBypass = (new Setting<>("EnableTicksBypass", 70, 1, 200, v -> bypass.getValue()));
    public Setting<Float> multiplierBypass = (new Setting<>("MultiplierBypass", 10.0F, 1.0F, 20.0F, v -> bypass.getValue()));

    // the amount of ticks to wait until the module auto-disables
    public Setting<Integer> disableTicks = (new Setting<>("DisableTicks", 20, 1, 100));
    // the option to only turn on timer when the player moves
    public Setting<Integer> enableTicks = (new Setting<>("EnableTicks", 20, 1, 100));
    // the game tick speed multiplier
    public Setting<Float> multiplier = (new Setting<>("Multiplier", 3.0F, 1.0F, 10.0F));
    public Setting<Boolean> stopInAir = (new Setting<>("StopInAir", false));

    private int ticksPassed = 0;
    private int ticksStill = 0;

    TimerUtil timerUtil = new TimerUtil();
    private boolean timerOn = false, changeSpeed = false;

    public TickShift() {
        super("TickShift", "Dicky shit", Category.MOVEMENT);
    }

//    @EventListener
//    public void onPacketReceive(PacketEvent.Receive event) {
//        if (event.getPacket() instanceof CPacketPlayer) {
//            event.setCanceled(true);
//        }
//    }

    // the method that checks if an entity is moving


    @Override
    public void onTick() {
        if (fullNullCheck()) return;
        if (stopInAir.getValue() &&
                (mc.player.motionY > 0.0
                        || !mc.player.onGround
                        || mc.player.isOnLadder()
                        || mc.player.capabilities.isFlying
                        || mc.player.fallDistance > 2.0f)) {
            timerManager.reset(11);
            return;
        }
        if(changeSpeed) {
            bypass();
        } else {
            norm();
        }

    }

    @EventListener
    public void onSettingChange(ClientEvent event) {
        if (event.getStage() == 2) {
            if (event.getSetting().getFeature().equals(this)) {
                if (event.getSetting().equals(enableTicks) || event.getSetting().equals(disableTicks)) {
                    reset();
                }
            }
        }
    }

    @EventListener
    public void onKey(KeyEvent keyEvent) {
        if (Keyboard.getEventKeyState() && bypass.getValue() && bind.getValue().getKey() == Keyboard.getEventKey() && timerUtil.passedMs(waitTime.getValue() * 1000)) {
            timerManager.reset(15);
            changeSpeed = true;
            timerOn = false;
            ticksStill = enableTicksBypass.getValue();
            ticksPassed = 0;
            timerUtil.reset();
            mc.getConnection().sendPacket(new CPacketPlayer.Position(Math.floor(mc.player.posX) + 0.5D,Math.floor(mc.player.posY),Math.floor(mc.player.posZ) + 0.5D,true));
//            if(autoStep.getValue()) Step.INSTANCE.setEnabled(true);
            if(blink.getValue())  moduleManager.getModuleByClass(Blink.class).enable();
            Command.sendMessage("Enabled bypass");
        }
    }

    // the method that gets called when the module is disabled
    @Override
    public void onDisable() {
        // set the client tick length back to the default
        if (resetOnDisable.getValue()) {
            reset();
        }

    }


    public void norm() {
        // make sure we dont have timer on
        if (!timerOn && !mc.player.isElytraFlying() && PacketFly.getInstance().isDisabled()) {
            // if they are moving remove 1 from the amount of ticks they are standing still for
            if (PlayerUtil.isPlayerMoving()) {
                // prevent the amount of ticks still to go below 0
                if (ticksStill >= 1) {
                    ticksStill--;
                }

            } else {
                // if they arent increase it by 1
                ticksStill++;
            }
        }
        // if the amount of ticks the player is standing still is equal to the amount of ticks for the timer to start do it
        if (ticksStill >= enableTicks.getValue()) {
            // set timer on to true which stops the movement checks
            timerOn = true;
            // change how the module enables based on the movementenable setting
            // if the player is pressing any movement keys enable the timer
            if (PlayerUtil.isMoving()) {
                // change the client side tick speed to something greater
                timerManager.setTimer(multiplier.getValue(), 11);
                // increase the number of ticks passed
                ticksPassed++;
            }

        }

        // if the amount of ticks passed is greater than or equal to the amount of ticks that need to pass for the module to disable, disable the module (duh)
        if (ticksPassed >= disableTicks.getValue()) {
            ticksPassed = 0;
            // decide whether to disable or reset the module
            if (disable.getValue()) {
                // disable the module
                disable();
            } else {
                // reset the module
                reset();
            }
        }
    }

    public void bypass() {
        // make sure we dont have timer on
        if (!timerOn && !mc.player.isElytraFlying() && PacketFly.getInstance().isDisabled()) {
            // if they are moving remove 1 from the amount of ticks they are standing still for
            if (PlayerUtil.isPlayerMoving()) {
                // prevent the amount of ticks still to go below 0
                if (ticksStill >= 1) {
                    ticksStill--;
                }

            } else {
                // if they arent increase it by 1
                ticksStill++;
            }
        }
        // if the amount of ticks the player is standing still is equal to the amount of ticks for the timer to start do it
        if (ticksStill >= enableTicksBypass.getValue()) {
            // set timer on to true which stops the movement checks
            timerOn = true;
            // change how the module enables based on the movementenable setting
            // if the player is pressing any movement keys enable the timer
            if (PlayerUtil.isMoving()) {
                // change the client side tick speed to something greater
                timerManager.setTimer(multiplierBypass.getValue(), 15);
                // increase the number of ticks passed
                ticksPassed++;
            }

        }

        // if the amount of ticks passed is greater than or equal to the amount of ticks that need to pass for the module to disable, disable the module (duh)
        if (ticksPassed >= disableTicksBypass.getValue()) {
            ticksPassed = 0;
//            if(autoStep.getValue()) Step.INSTANCE.setEnabled(false);

            // decide whether to disable or reset the module
            if (disable.getValue()) {
                // disable the module
                changeSpeed = false;
                disable();
            } else {
                // reset the module
                changeSpeed = false;
                reset();
            }
        }
    }

    // the method that gets called on reset
    public void reset() {
        timerManager.reset(11);
        changeSpeed = false;
        timerOn = false;
        ticksStill = 0;
        ticksPassed = 0;
    }

    // the method that gets called to update the hud info
    @Override
    public String getDisplayInfo() {
        return timerOn ? String.valueOf(ticksStill - ticksPassed) :  String.valueOf(ticksStill);
    }
}