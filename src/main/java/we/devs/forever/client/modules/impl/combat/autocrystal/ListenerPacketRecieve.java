package we.devs.forever.client.modules.impl.combat.autocrystal;

import com.mojang.realmsclient.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.client.modules.api.listener.ModuleListener;

import java.util.ArrayList;
import java.util.List;

public class ListenerPacketRecieve extends ModuleListener<AutoCrystal, PacketEvent.Receive> {
    public ListenerPacketRecieve(AutoCrystal module) {
        super(module, PacketEvent.Receive.class, ListenerPriority.HIGHEST);
    }

    @Override
    public void invoke(PacketEvent.Receive event) {
        // check if there has been any external explosions (i.e. crystals not broken by the local player)
        boolean externalExplosion = false;

        // packet that confirms crystal removal
        if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS) && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE)) {

            // crystal entities within the packet position
            // List<EntityEnderCrystal> soundCrystals = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(((SPacketSoundEffect) event.getPacket()).getX(), ((SPacketSoundEffect) event.getPacket()).getY(), ((SPacketSoundEffect) event.getPacket()).getZ())));

            // check all entities
            // for (EntityEnderCrystal crystal : soundCrystals) {

            // check if we have already counted this explosion
            // if (!inhibitCrystals.contains(crystal)) {
            //    externalExplosion = true;
            //
            // }

            // with this on the main thread there's no reason
            // why we need all the concurrency stuff...?
            mc.addScheduledTask(() -> {

                // attempt to clear crystals
                for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

                    // make sure the entity actually exists
                    if (crystal == null || crystal.isDead) {
                        continue;
                    }

                    // make sure it's a crystal
                    if (!(crystal instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    // entity distance from sound
                    double soundRange = crystal.getDistance(((SPacketSoundEffect) event.getPacket()).getX() + 0.5, ((SPacketSoundEffect) event.getPacket()).getY() + 0.5, ((SPacketSoundEffect) event.getPacket()).getZ() + 0.5);

                    // make sure the crystal is in range from the sound to be destroyed
                    if (soundRange > 11) {
                        continue;
                    }

                    // don't attack these crystals they're going to be exploded anyways
                    module.inhibitCrystals.add((EntityEnderCrystal) crystal);

                    // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                    crystal.setDead();

                    // force remove entity
                    mc.world.removeEntity(crystal);

                    // ignore
                    if (module.sequential.getValue().equals(AutoCrystal.Sequential.Strict)) {
                        module.deadCrystals.add(crystal.getEntityId());
                    } else {
                        mc.world.removeEntityDangerously(crystal);
                    }
                }
            });
        }

        // packet for general explosions
        if (event.getPacket() instanceof SPacketExplosion) {

            // crystal entities within the packet position
            List<EntityEnderCrystal> explosionCrystals = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(((SPacketExplosion) event.getPacket()).getX(), ((SPacketExplosion) event.getPacket()).getY(), ((SPacketExplosion) event.getPacket()).getZ())));

            // check all entities
            for (EntityEnderCrystal crystal : explosionCrystals) {

                // check if we have already counted this explosion
                if (!module.inhibitCrystals.contains(crystal)) {
                    externalExplosion = true;
                    break;
                }
            }

            // with this on the main thread there's no reason
            // why we need all the concurrency stuff...?
            mc.addScheduledTask(() -> {

                // attempt to clear crystals
                for (Entity crystal : new ArrayList<>(mc.world.loadedEntityList)) {

                    // make sure the entity actually exists
                    if (crystal == null || crystal.isDead) {
                        continue;
                    }

                    // make sure it's a crystal
                    if (!(crystal instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    // entity distance from sound
                    double soundRange = crystal.getDistance(((SPacketExplosion) event.getPacket()).getX() + 0.5, ((SPacketExplosion) event.getPacket()).getY() + 0.5, ((SPacketExplosion) event.getPacket()).getZ() + 0.5);

                    // make sure the crystal is in range from the sound to be destroyed
                    if (soundRange > ((SPacketExplosion) event.getPacket()).getStrength()) {
                        continue;
                    }

                    // don't attack these crystals they're going to be exploded anyways
                    module.inhibitCrystals.add((EntityEnderCrystal) crystal);

                    // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                    crystal.setDead();

                    // force remove entity
                    mc.world.removeEntity(crystal);

                    // ignore
                    if (module.sequential.getValue().equals(AutoCrystal.Sequential.Strict)) {
                        module.deadCrystals.add(crystal.getEntityId());
                    } else {
                        mc.world.removeEntityDangerously(crystal);
                    }
                }
            });
        }

        // packet for destroyed entities
        if (event.getPacket() instanceof SPacketDestroyEntities) {

            // check all entities being destroyed by the packet
            for (int entityId : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {

                // get entity from id
                Entity crystal = mc.world.getEntityByID(entityId);

                // make sure its a crystal
                if (crystal instanceof EntityEnderCrystal) {

                    // check if we have already counted this explosion
                    if (!module.inhibitCrystals.contains(crystal)) {
                        externalExplosion = true;
                    }

                    crystal.setDead();

                    // remove quicker to make the autocrystal look faster (as the world will remove these entities anyway
                    mc.addScheduledTask(() -> {
                        mc.world.removeEntity(crystal);
                        mc.world.removeEntityDangerously(crystal);
                    });
                }
            }
        }

        // if there's been an external explosion then we can place again
        if (externalExplosion) {

            // clear place
            if (module.sequential.getValue().equals(AutoCrystal.Sequential.Normal)) {


                // we found a placement
                AutoCrystal.DamageHolder<BlockPos> placement = module.placement;
                if (placement != null) {

                    // face the placement
                    module.angleVector = Pair.of(new Vec3d(placement.getDamageSource()).add(0.5, 0.5, 0.5), AutoCrystal.YawStep.None);

                    // place the crystal
                    if (module.placeCrystal(placement.getDamageSource())) {

                        // add it to our list of attacked crystals
                        module.placedCrystals.put(placement.getDamageSource(), System.currentTimeMillis());
                    }
                }
            }
        }

        // packet for crystal spawns
        if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {

            // position of the spawned crystal
            BlockPos spawnPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX() - 0.5, ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ() - 0.5);

            // clear timer
            if (module.await.getValue()) {

                // face the crystal
                module.angleVector = Pair.of(new Vec3d(spawnPosition), AutoCrystal.YawStep.Full);

                // check if we have placed this crystal
                if (module.placementPackets.contains(spawnPosition.down())) {

                    // prevents attacks faster than our ping will al;ow
                    if (module.inhibitFactor.getValue() > module.inhibitFactor.getMin() && module.inhibit.getValue().equals(AutoCrystal.Inhibit.Full)) {

                        // add to map of spawned crystals
                        module.spawnedCrystals.put(System.currentTimeMillis(), ((SPacketSpawnObject) event.getPacket()).getEntityID());
                    }

                    // mark it as our current explosion
                    else if (module.attackDelay.getValue() > module.attackDelay.getMin()) {

                        // since it's been confirmed that the crystal spawned, we can move on to our next process
                        module.explodeClearance = true;
                    }

                    // clear the next explosion
                    else {

                        // attack spawned crystal
                        if (module.attackCrystal(((SPacketSpawnObject) event.getPacket()).getEntityID())) {

                            // add it to our list of attacked crystals
                            module.attackedCrystals.put(((SPacketSpawnObject) event.getPacket()).getEntityID(), System.currentTimeMillis());

                            // clamp
                            if (module.lastAttackTime <= 0) {
                                module.lastAttackTime = System.currentTimeMillis();
                            }

                            // make space for new val
                            if (module.attackTimes.length - 1 >= 0) {
                                System.arraycopy(module.attackTimes, 1, module.attackTimes, 0, module.attackTimes.length - 1);
                            }

                            // add to attack times
                            module.attackTimes[module.attackTimes.length - 1] = System.currentTimeMillis() - module.lastAttackTime;

                            // mark attack flag
                            module.lastAttackTime = System.currentTimeMillis();

                            module.explodeClearance = false;
                            module.explodeTimer.reset();
                        }
                    }

                    // reset
                    module.placementPackets.clear();

                    // accounted for
                    module.placedCrystals.remove(spawnPosition.down());
                }
            }
        }
    }
}
