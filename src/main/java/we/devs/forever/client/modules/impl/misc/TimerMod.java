package we.devs.forever.client.modules.impl.misc;

import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;

public
class TimerMod extends Module {

    private final TimerUtil timerUtil = new TimerUtil();
    private final TimerUtil turnOffTimerUtil = new TimerUtil();
    public Setting<Boolean> autoOff = (new Setting<>("AutoOff", false));
    public Setting<Integer> timeLimit = (new Setting<>("Limit", 250, 1, 2500, v -> autoOff.getValue()));
    public Setting<TimerMode> mode = (new Setting<>("Mode", TimerMode.NORMAL));
    public Setting<Float> timerSpeed = (new Setting<>("Speed", 4.0f, 0.1f, 20.0f));
    public Setting<Float> fastSpeed = (new Setting<>("Fast", 10.0f, 0.1f, 100.0f, "Fast Speed for switch.", v -> mode.getValue() == TimerMode.SWITCH));
    public Setting<Integer> fastTime = (new Setting<>("FastTime", 20, 1, 500, "How long you want to go fast.(ms * 10)", v -> mode.getValue() == TimerMode.SWITCH));
    public Setting<Integer> slowTime = (new Setting<>("SlowTime", 20, 1, 500, "Recover from too fast.(ms * 10)", v -> mode.getValue() == TimerMode.SWITCH));
    public Setting<Boolean> startFast = (new Setting<>("StartFast", false, v -> mode.getValue() == TimerMode.SWITCH));
    public Setting<Boolean> stopInAir = (new Setting<>("StopInAir", false));

    public float speed = 1.0f;
    private boolean fast = false;

    public TimerMod() {
        super("Timer", "Will speed up the game.", Category.MISC);
    }

    @Override
    public void onEnable() {
        turnOffTimerUtil.reset();
        this.speed = timerSpeed.getValue();
        if (!startFast.getValue()) {
            timerUtil.reset();
        }
    }

    @Override
    public void onUpdate() {
        if(stopInAir.getValue()&& (mc.player.motionY > 0.0 || !mc.player.onGround || mc.player.isOnLadder() || mc.player.capabilities.isFlying || mc.player.fallDistance > 2.0f)) {
            timerManager.reset(12);
            return;
        }
        if (autoOff.getValue() && turnOffTimerUtil.passedMs(timeLimit.getValue())) {
            this.disable();
            return;
        }

        if (mode.getValue() == TimerMode.NORMAL) {
            timerManager.setTimer(timerSpeed.getValue(),12);
            return;
        }

        if (!fast && timerUtil.passedDms(slowTime.getValue())) {
            fast = true;
            timerManager.setTimer(fastSpeed.getValue(),12);
            timerUtil.reset();
        }

        if (fast && timerUtil.passedDms(fastTime.getValue())) {
            fast = false;
            timerManager.setTimer(timerSpeed.getValue(),12);
            timerUtil.reset();
        }
    }

    @Override
    public void onDisable() {
        timerManager.reset(12);
        fast = false;
    }

    @Override
    public String getDisplayInfo() {
        return timerSpeed.getValueAsString();
    }

    public
    enum TimerMode {
        NORMAL,
        SWITCH
    }
}
