package we.devs.forever.api.manager.impl.player;


import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.events.player.DeathEvent;
import we.devs.forever.api.event.events.render.TotemPopEvent;
import we.devs.forever.api.event.eventsys.annotated.handler.annotation.EventListener;
import we.devs.forever.api.manager.api.AbstractManager;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.main.ForeverClient;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TargetManager extends AbstractManager {
    public TargetManager() {
        super("TargetManager");
    }

    @Override
    protected void onLoad() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    protected void onUnload() {

    }
    private final Map<EntityPlayer, Long> targets = new ConcurrentHashMap<>();

    public static Map<String, Integer> targetPopsList = new HashMap<>();

    private final TimerUtil timer = new TimerUtil();

    public void addTarget(EntityPlayer target) {
        targets.put(target, System.currentTimeMillis());
    }

    public void removeTarget(EntityPlayer target) {
        targets.remove(target);
    }

    public Set<EntityPlayer> getTargets() {
        return targets.keySet();
    }

    public boolean isTarget(EntityPlayer suspect) {
        return targets.containsKey(suspect);
    }

    public static int getPops(String name) {
        if(targetPopsList.get(name) == null) {
            return 0;
        }
        return targetPopsList.get(name);
    }

    // We can use this for cool  tracers and esp
    public int getTargetLifespanColor(EntityPlayer entity) {
        try {
            if (!targets.containsKey(entity)) return 255;
            int targetColor = (int) (System.currentTimeMillis() - targets.get(entity)) / 118;
            return Math.min(targetColor, 255); // targets are only updated ever 10 sec so we have to check
        } catch (NullPointerException npe) {
            return 255;
        }
    }

    // Totally not pedo
    public EntityPlayer getYoungestTarget() {
        if(!targets.isEmpty()) {
            return Collections.max(targets.entrySet(), Map.Entry.comparingByValue()).getKey();
        }
        return null;
    }

    public void refreshTargets() {
        targets.forEach((entity, time) -> {
            if (System.currentTimeMillis() - time > TimeUnit.SECONDS.toMillis(30L)) {
                targets.remove(entity);
            }
        });
    }

    public void resetTargets() {
        targets.clear();
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (timer.passedMs(10000L)) {
            refreshTargets();
            timer.reset();
        }
        if(mc.currentScreen instanceof GuiGameOver) {
            resetTargets();
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Send event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = (CPacketUseEntity) event.getPacket();
            if (packet.getAction().equals(CPacketUseEntity.Action.ATTACK) && packet.getEntityFromWorld(mc.world) instanceof EntityPlayer) {
                EntityPlayer attackedEntity = (EntityPlayer) packet.getEntityFromWorld(mc.world);
                assert attackedEntity != null;
                addTarget(attackedEntity);
            }
        }
    }

    @EventListener
    public void onPacket(PacketEvent.Receive event) {
        if (mc.world == null || mc.player == null) return;
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = event.getPacket();
            if (packet.getOpCode() == 35) {
                EntityPlayer entity = (EntityPlayer) packet.getEntity(mc.world);
                if (targetPopsList == null) {
                    targetPopsList = new HashMap<>();
                }
                if (targetPopsList.getOrDefault(entity.getName(), null) != null) {
                    targetPopsList.put(entity.getName(), targetPopsList.get(entity.getName()) + 1);
                } else {
                    targetPopsList.put(entity.getName(), 1);
                }
                TotemPopEvent totemPopEvent = new TotemPopEvent((EntityPlayer) entity, targetPopsList.get(entity.getName()));
                ForeverClient.EVENT_BUS.post(totemPopEvent);
            }
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (mc.world == null || mc.player == null) {
            return;
        }
        for (EntityPlayer player : mc.world.playerEntities) {
          //  if (FakePlayerManager.isFake(player)) continue;
            if (player.getHealth() <= 0
                    && targetPopsList.containsKey(player.getName())) {
                TargetManager.targetPopsList.remove(player.getName(), TargetManager.targetPopsList.get(player.getName()));
            }
        }
        getTargets().forEach(target -> {
            if (target != null) {
                if (target.getHealth() <= 0) {
                    DeathEvent deathEvent = new DeathEvent(target,getPops(target.getName()));
                    ForeverClient.EVENT_BUS.post(deathEvent);
                    removeTarget(target);
                }
            }
        });
    }
}