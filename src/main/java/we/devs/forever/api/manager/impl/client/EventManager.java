package we.devs.forever.api.manager.impl.client;

import com.google.common.base.Strings;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraftforge.client.event.*;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import we.devs.forever.api.event.events.network.ConnectionEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.ChorusEvent;
import we.devs.forever.api.event.events.render.Render2DEvent;
import we.devs.forever.api.event.events.render.RenderFogDensityEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.Util;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.render.util.TextUtil;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.modules.api.Module;
import we.devs.forever.main.ForeverClient;

import java.util.Objects;
import java.util.UUID;

public
class EventManager extends AbstractManager {

    private final TimerUtil timerUtil = new TimerUtil();
    private final TimerUtil logoutTimerUtil = new TimerUtil();

    private final TimerUtil logoutTimer = new TimerUtil();
    private final TimerUtil chorusTimer = new TimerUtil();

    public EventManager() {
        super("EventManager");
    }


    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (!fullNullCheck() && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(Util.mc.player)) {
            Client.potionManager.update();

            Client.moduleManager.onUpdate();
            if (timerUtil.passedMs(0)) {
                Client.moduleManager.sortModules(true);
                timerUtil.reset();
            }
        }
    }

    @SubscribeEvent
    public void onFogRender(EntityViewRenderEvent.FogDensity event) {
        RenderFogDensityEvent renderFogDensityEvent = new RenderFogDensityEvent(event.getRenderer(), event.getEntity(), event.getState(), event.getRenderPartialTicks());
        ForeverClient.EVENT_BUS.post(renderFogDensityEvent);
        if (renderFogDensityEvent.isCanceled()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlay(RenderGameOverlayEvent.Text event) {
        ForeverClient.EVENT_BUS.post(event);

    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        ForeverClient.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onRenderPost(RenderGameOverlayEvent.Post event) {
        ForeverClient.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onCameraSetup(final EntityViewRenderEvent.CameraSetup event) {
        ForeverClient.EVENT_BUS.post(event);
    }

    @SubscribeEvent
    public void onInput(InputUpdateEvent event) {
        ForeverClient.EVENT_BUS.post(event);
    }


    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        logoutTimerUtil.reset();

        Client.moduleManager.onLogin();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        Client.moduleManager.onLogout();
        Client.potionManager.onLogout();
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (ForeverClient.IsDisabledAcc) {
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ignored) {
            }
        }

        ForeverClient.EVENT_BUS.post(event);
        Module.tickOngoing.set(event.phase == TickEvent.Phase.START);
        Client.moduleManager.onTick();
    }

//    @EventListener(priority = ListenerPriority.HIGHEST)
//    public void onMotionPre(MotionEvent.Pre event) {
//        updateManagers();
//    }
//
//    @EventListener
//    public void onMotionPost(MotionEvent.Post event) {
//        resetManagers();
//    }

    public void updateManagers() {
        if (fullNullCheck()) return;
//        Command.sendMessage("I do update");
        Client.speedManager.updateValues();
        Client.rotationManager.updateRotations();
        Client.positionManager.updatePosition();
    }

    public void resetManagers() {
        if (fullNullCheck()) return;
//        Command.sendMessage("I do reset");
        Client.positionManager.restorePosition();
        Client.rotationManager.resetRotations();
    }


    @EventListener
    public void onPacketReceive(PacketEvent.Receive event) {

        if (event.getStage() != 0) {
            return;
        }
        Client.serverManager.onPacketReceived();
        if (event.getPacket() instanceof SPacketPlayerListItem && !EventManager.fullNullCheck() && this.logoutTimer.passedS(1.0)) {
            SPacketPlayerListItem packet = event.getPacket();
            if (!SPacketPlayerListItem.Action.ADD_PLAYER.equals(packet.getAction()) && !SPacketPlayerListItem.Action.REMOVE_PLAYER.equals(packet.getAction())) {
                return;
            }
            packet.getEntries().stream().filter(Objects::nonNull).filter(data -> !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null).forEach(data -> {
                UUID id = data.getProfile().getId();
                switch (packet.getAction()) {
                    case ADD_PLAYER: {
                        String name = data.getProfile().getName();
                        ForeverClient.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                        break;
                    }
                    case REMOVE_PLAYER: {
                        EntityPlayer entity = EventManager.mc.world.getPlayerEntityByUUID(id);
                        if (entity != null) {
                            String logoutName = entity.getName();
                            ForeverClient.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                            break;
                        }
                        ForeverClient.EVENT_BUS.post(new ConnectionEvent(2, id, null));
                    }
                }
            });
        } else if (event.getPacket() instanceof SPacketTimeUpdate) {
            Client.serverManager.update();
        } else if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT) {
            if (!this.chorusTimer.passedMs(100L)) {
                ForeverClient.EVENT_BUS.post(new ChorusEvent(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ()));
            }
            this.chorusTimer.reset();
        }
    }


    @SubscribeEvent
    public void renderHUD(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR) {
            Client.textManager.updateResolution();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void onRenderGameOverlayEvent(RenderGameOverlayEvent.Text event) {
        if (event.getType().equals(RenderGameOverlayEvent.ElementType.TEXT)) {
            final ScaledResolution resolution = new ScaledResolution(Util.mc);
            Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
            Client.moduleManager.onRender2D(render2DEvent);
            GlStateManager.color(1.f, 1.f, 1.f, 1.f);
        }
    }


    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatRecieve(ClientChatReceivedEvent event) {

        ForeverClient.EVENT_BUS.post(event);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onChatSent(ClientChatEvent event) {
        if (event.getMessage().startsWith(Command.getCommandPrefix())) {
            event.setCanceled(true);
            try {
                Util.mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
                if (event.getMessage().length() > 1) {
                    Client.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
                } else {
                    Command.sendMessage("Please enter a command.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                //Command.sendMessage(TextUtil.RED + "An error occurred while running this command. Check the log!");
            }
            event.setMessage("");
        }
    }


    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
//        if (event.isCanceled()) return;

        Client.moduleManager.onAltRender3D(event.getPartialTicks());
    }



    @Override
    public void onLoad() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onkey(InputEvent.KeyInputEvent event) {
        ForeverClient.EVENT_BUS.post(event);
    }

    //    @SubscribeEvent
//    public void onMouse(MouseEvent event) {
//        ForeverClient.EVENT_BUS.post( new KeyEvent(1,event.isButtonstate(), event.getButton() - 100,'a'));
//    }
    @Override
    public void onUnload() {

    }
//    @SubscribeEvent
//    public void onRenderHand(RenderHandEvent event) {
//        if(ShaderChams.INSTANCE.isEnabled()) {
//            if(!ShaderChams.items.getValue().equals(ShaderChams.Mode.None)  && !ShaderChams.INSTANCE.crit) event.setCanceled(true);
//
//        }
//
//
//    }


}
