package we.devs.forever.client.modules.impl.movement;

import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.network.play.client.*;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.InputUpdateEvent;
import org.lwjgl.input.Keyboard;
import we.devs.forever.api.event.events.client.ClickWindowEvent;
import we.devs.forever.api.event.events.client.KeyBindingEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.MotionEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.util.player.PlayerUtil;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.client.setting.Setting;
import we.devs.forever.client.ui.foreverClientGui.ForeverClientGui;

public
class NoSlow extends Module {

    private static final KeyBinding[] keys = {
            mc.gameSettings.keyBindForward,
            mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft,
            mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump,
            mc.gameSettings.keyBindSprint
    };
    private static NoSlow INSTANCE = new NoSlow();
    public Setting<Mode> mode = (new Setting<>("NoSlow", Mode.Normal));
    public Setting<Boolean> guiMove = (new Setting<>("GuiMove", true));
    public Setting<Boolean> arrowMove = (new Setting<>("ArrowMove", false, v ->guiMove.getValue() ));
    public Setting<Boolean> soulSand = (new Setting<>("SoulSand", true));
    private final Setting<WebMode> webMode = (new Setting<>("Mode", WebMode.Fast));
    public Setting<Boolean> onSneak = (new Setting<>("onSneak", true, v -> webMode.getValue() != WebMode.None));
    private final Setting<Float> fastSpeed = (new Setting<>("FastSpeed", 3.0f, 0.1f, 9.0f, v -> webMode.getValue() == WebMode.Fast));


    public NoSlow() {
        super("NoSlow", "Prevents you from getting slowed down.", Category.MOVEMENT);
        setInstance();
    }

    public static NoSlow getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NoSlow();
        }
        return INSTANCE;
    }

    private void setInstance() {
        INSTANCE = this;
    }

    @Override
    public void onUpdate() {
        if (fullNullCheck()) {
            return;
        }
        if (guiMove.getValue()) {
            if (mc.currentScreen instanceof GuiOptions
                    || mc.currentScreen instanceof GuiVideoSettings
                    || mc.currentScreen instanceof GuiScreenOptionsSounds
                    || mc.currentScreen instanceof GuiContainer
                    || mc.currentScreen instanceof GuiIngameMenu
                    || mc.currentScreen instanceof ForeverClientGui
            ) {
                for (KeyBinding bind : keys) {
                    KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));
                }
            } else if (mc.currentScreen == null) {
                for (KeyBinding bind : keys) {
                    if (!Keyboard.isKeyDown(bind.getKeyCode())) {
                        KeyBinding.setKeyBindState(bind.getKeyCode(), false);
                    }
                }
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT) && arrowMove.getValue()) mc.player.rotationYaw -= 5;
            if (Keyboard.isKeyDown(Keyboard.KEY_UP) && mc.player.rotationPitch > -84 && arrowMove.getValue()) mc.player.rotationPitch -= 5;
            if (Keyboard.isKeyDown(Keyboard.KEY_DOWN) && mc.player.rotationPitch < 84 && arrowMove.getValue()) mc.player.rotationPitch += 5;
            if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT) && arrowMove.getValue()) mc.player.rotationYaw += 5;
        }
    }

    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {
        if ((mode.getValue() == Mode.NCPStrictOld || mode.getValue() == Mode.NCPStrictNew) && event.getPacket() instanceof CPacketClickWindow) {
            if (!mc.player.isSneaking()) {
                PlayerUtil.send(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
            }
            if (!mc.player.isSprinting()) {
                PlayerUtil.send(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
            }
        }
    }

    @EventListener
    public void onPacketSend(PacketEvent.Send event) {
        if ((mode.getValue() == Mode.NCPStrictOld || mode.getValue() == Mode.NCPStrictNew) && guiMove.getValue() && event.getPacket() instanceof CPacketClickWindow) {
            if (mc.player.isActiveItemStackBlocking()) {
                mc.playerController.onStoppedUsingItem(mc.player);
            }
            if (mc.player.isSneaking()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
            }
            if (mc.player.isSprinting()) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }
        }
        if (event.getPacket() instanceof CPacketPlayer) {
            Item item = mc.player.getActiveItemStack().getItem();
            if ((mode.getValue() == Mode.NCPStrictOld || mode.getValue() == Mode.NCPStrictNew) &&
                    (item instanceof ItemFood
                            || item instanceof ItemBow
                            || item instanceof ItemPotion) && mc.player.isHandActive() && !mc.player.isRiding()) {
                if (mode.getValue() == Mode.NCPStrictOld) {
                    PlayerUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition(), EnumFacing.DOWN));
                } else if(mode.getValue() == Mode.NCPStrictNew){
                    PlayerUtil.send(new CPacketHeldItemChange(mc.player.inventory.currentItem));
                }
            }
        }
    }

    @EventListener
    public void onMotionEvent(MotionEvent.Pre event) {
        if (fullNullCheck() || webMode.getValue() == WebMode.None) return;

        if (mc.player.isInWeb) {

            if (webMode.getValue() == WebMode.Fast && (mc.gameSettings.keyBindSneak.isKeyDown() || !onSneak.getValue())) {
                timerManager.reset(10);
                mc.player.motionY -= fastSpeed.getValue();

            } else if (webMode.getValue() == WebMode.Strict && !mc.player.onGround &&(mc.gameSettings.keyBindSneak.isKeyDown()|| !onSneak.getValue())) {
                timerManager.setTimer(8, 10);

            } else {
                timerManager.reset(10);
            }

        } else {
            timerManager.reset(8);
        }
    }
    @EventListener
    public void onKeyBinding(KeyBindingEvent event) {
        if (mc.world == null || mc.player == null) return;
        if (!(mc.currentScreen instanceof GuiChat) && mc.currentScreen != null) {
            event.holding = event.pressed;
        }
    }

    @EventListener
    public void onWindowClick(ClickWindowEvent event) {
        if ((mode.getValue() == Mode.NCPStrictOld || mode.getValue() == Mode.NCPStrictNew)&& !guiMove.getValue()) return;

        if (mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING));
        }
        if (mc.player.isSprinting()) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
        }
    }
    @EventListener
    public void onInput(InputUpdateEvent event) {
//        if (noSlow.getValue() && mc.player.isHandActive() && !mc.player.isRiding()) {
//            event.getMovementInput().moveStrafe *= 5;
//            event.getMovementInput().moveForward *= 5;
//        }
        if (mode.getValue() != Mode.None && event.getMovementInput() == mc.player.movementInput && mc.player.isHandActive() && !mc.player.isRiding()) {
            event.getMovementInput().moveStrafe /= 0.2f;
            event.getMovementInput().moveForward /= 0.2f;
        }
    }



    public enum Mode {
        None,
        Normal,
        NCPStrictOld,
        NCPStrictNew
    }
    private enum WebMode {
        None,
        Fast,
        Strict
    }
}
