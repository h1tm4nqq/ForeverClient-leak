package we.devs.forever.client.modules.impl.combat.autocrystalold.listeners;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.*;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.network.PacketEvent;
import we.devs.forever.api.event.eventsys.handler.ListenerPriority;
import we.devs.forever.client.modules.api.listener.ModuleListener;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.Inhibit;
import we.devs.forever.client.modules.impl.combat.autocrystalold.util.PosInfo;

import java.util.ArrayList;
import java.util.List;

public class ListenerPacketReceive extends ModuleListener<AutoCrystal, PacketEvent.Receive> {



    public ListenerPacketReceive(AutoCrystal module) {
        super(module, PacketEvent.Receive.class,ListenerPriority.HIGH);
    }

    @Override
    public void invoke(PacketEvent.Receive event) {

        boolean externalExplosion = false;

        try {
            if (event.getPacket() instanceof SPacketBlockChange) {
                SPacketBlockChange packetBlockChange = event.getPacket();
                BlockPos expected = module.seqHelper.expecting;
                if (expected != null && expected.equals(packetBlockChange.getBlockPosition()) && module.seqHelper.crystalPos == null) {

                    module.placeTimer.setTime(0);
                    module.seqHelper.setExpecting(null);
                }
            }
            // packet that confirms crystal removal
            if (event.getPacket() instanceof SPacketSoundEffect && ((SPacketSoundEffect) event.getPacket()).getCategory().equals(SoundCategory.BLOCKS)
                    && ((SPacketSoundEffect) event.getPacket()).getSound().equals(SoundEvents.ENTITY_GENERIC_EXPLODE) && module.setDead.getValue()) {

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
                SPacketSoundEffect packet = event.getPacket();
                Vec3d cPos = module.seqHelper.crystalPos;
                if (packet.getCategory() == SoundCategory.BLOCKS
                        && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE
                        && cPos != null && cPos.squareDistanceTo(packet.getX(), packet.getY(), packet.getZ()) < 144.0) {
                    module.seqHelper.setExpecting(null);
                }
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
                        module.placeLocations.remove(new BlockPos(crystal.getPositionVector()).down());
                        // don't attack these crystals they're going to be exploded anyways
                        module.inhibitEntities.add(crystal);

                        // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                        crystal.setDead();

                        // force remove entity
                        mc.world.removeEntity(crystal);

                        // ignore
                        if (module.inhibit.getValue() == Inhibit.Strict) {
                            module.deadCrystals.add(crystal.getEntityId());
                        } else {
                            mc.world.removeEntityDangerously(crystal);
                        }
                    }
                });
            }

            if (event.getPacket() instanceof SPacketExplosion) {

                // crystal entities within the packet position
                List<EntityEnderCrystal> explosionCrystals = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(new BlockPos(((SPacketExplosion) event.getPacket()).getX(), ((SPacketExplosion) event.getPacket()).getY(), ((SPacketExplosion) event.getPacket()).getZ())));

                // check all entities
                for (EntityEnderCrystal crystal : explosionCrystals) {

                    // check if we have already counted this explosion
                    if (!module.inhibitEntities.contains(crystal)) {
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
                        module.inhibitEntities.add((EntityEnderCrystal) crystal);
                        module.placeLocations.remove(new BlockPos(crystal.getPositionVector()).down());
                        // the world sets the crystal dead one tick after this packet, but we can speed up the placements by setting it dead here
                        crystal.setDead();

                        // force remove entity
                        mc.world.removeEntity(crystal);

                        // ignore
                        if (module.inhibit.getValue() == Inhibit.Strict) {
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
                    if(crystal == null) continue;
                    // make sure its a crystal
                    if (crystal instanceof EntityEnderCrystal) {

                        // check if we have already counted this explosion
                        if (!module.inhibitEntities.contains(crystal)) {
                            externalExplosion = true;
                        }
                        module.placeLocations.remove(new BlockPos(crystal.getPositionVector()).down());
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
                if (module.sequential.getValue()) {

                    // we found a placement
                    if (module.placePosInfo != null) {

                        // face the placement
//                        module.listenerOnUpdateWalkingPlayer.invoke();
                        PosInfo<BlockPos> temp = module.placePosInfo;
                        // place the crystal
                        if(temp != null) {
                            if (module.onPlace(temp.getValue())) {

                                // add it to our list of attacked crystals
                                module.placeLocations.put(module.placePosInfo.getValue(), System.currentTimeMillis());
                            }
                        }
                    }
                }
            }

            // packet for crystal spawns
            if (event.getPacket() instanceof SPacketSpawnObject && ((SPacketSpawnObject) event.getPacket()).getType() == 51) {
                SPacketSpawnObject packet =  event.getPacket();
                if (packet.getType() == 51 && new BlockPos(packet.getX(), packet.getY(), packet.getZ()).down().equals(module.seqHelper.expecting)) {
                    module.seqHelper.setExpecting(null);
                }

                // position of the spawned crystal
                BlockPos spawnPosition = new BlockPos(((SPacketSpawnObject) event.getPacket()).getX() - 0.5, ((SPacketSpawnObject) event.getPacket()).getY(), ((SPacketSpawnObject) event.getPacket()).getZ() - 0.5);

                // clear timer
                if (module.await.getValue()) {

                    // check if we have placed this crystal
                    if (module.placeLocations.containsKey(spawnPosition.down())) {

                        // prevents attacks faster than our ping will al;ow
                        if (module.inhibitFactor.getValue() > module.inhibitFactor.getMin() && module.inhibit.getValue().equals(Inhibit.Strict)) {

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
                            if (module.onBreak(mc.world.getEntityByID(((SPacketSpawnObject) event.getPacket()).getEntityID()))) {

                                // add it to our list of attacked crystals
                                module.breakLocations.put(((SPacketSpawnObject) event.getPacket()).getEntityID(), System.currentTimeMillis());

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
                                module.breakTimer.reset();
                            }
                        }

                        // reset
//                        module.placeLocations.clear();

                        // accounted for
                    }
                }
            }


        } catch (Throwable exception) {
            exception.printStackTrace();
        }

    }


    private double getDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        double d0 = (x1 - x2);
        double d1 = (y1 - y2);
        double d2 = (z1 - z2);
        return Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }
}
