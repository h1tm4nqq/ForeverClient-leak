package we.devs.forever.client.modules.impl.combat.autocrystal;

import com.mojang.realmsclient.util.Pair;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.util.math.BlockPos;
import we.devs.forever.api.util.math.path.BasePath;
import we.devs.forever.client.modules.api.ModuleThread;

import java.util.List;
import java.util.Map;

public class CalcThread extends ModuleThread<AutoCrystal> {
    public CalcThread(AutoCrystal module, int delay) {
        super(module, delay);
    }

    @Override
    public void invoke() {
        if (fullNullCheck()) return;
        // search ideal processes
        AutoCrystal.DamageHolder<EntityEnderCrystal> searchExplosion = module.getCrystal();
        AutoCrystal.DamageHolder<BlockPos> searchPlacement = module.getPlacement();


        // only search when enabled
        if (searchExplosion != null) {

            // update
            module.explosion = searchExplosion;

        }

        // check queue
        else if (!module.queuedCrystals.isEmpty()) {

            // get first item in queue
            AutoCrystal.DamageHolder<EntityEnderCrystal> next = new AutoCrystal.DamageHolder<>(module.queuedCrystals.stream().findFirst().orElse(null), null, 0, 0);

            // set explosion & update queue
            if (next.getDamageSource() != null) {
                module.explosion = next;
            }
        } else {
            module.explosion = null;
        }

        // update placement
        module.placement = searchPlacement;
        module.odsidianPlace = module.getObyPlacement();
        // check number of crystals in the last second
        if (module.crystalTimer.passedS(1)) {

            // make space for new val
            if (module.crystalCounts.length - 1 >= 0) {
                System.arraycopy(module.crystalCounts, 1, module.crystalCounts, 0, module.crystalCounts.length - 1);
            }

            // add to crystal counts
            module.crystalCounts[module.crystalCounts.length - 1] = module.lastCrystalCount;

            // reset
            module.lastCrystalCount = 0;
            module.crystalTimer.reset();
        }

        // we are cleared to process our calculations
        if (module.rotateTicks <= 0) {

            // needs the extra wait time
            if (module.inhibitFactor.getValue() > module.inhibitFactor.getMin() && module.inhibit.getValue().equals(AutoCrystal.Inhibit.Full)) {

                // spawned crystal
                Map.Entry<Long, Integer> latestSpawn = module.spawnedCrystals.firstEntry();

                // attack latest spawn if waited
                if (latestSpawn != null) {

                    // calculate if we have passed delays (old delays)
                    // place delay based on place speeds
                    double explodeDelay = module.inhibitFactor.getValue() * 50;

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long switchDelay = module.explodeSwitchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    boolean delayed = System.currentTimeMillis() - latestSpawn.getKey() >= explodeDelay && module.switchTimer.passedMs(switchDelay);
                    Entity entity = mc.world.getEntityByID(latestSpawn.getValue());
                    // check if we have passed the explode time
                    if ((module.explodeClearance || delayed) && entity != null) {

                        // face the crystal
                        module.angleVector = Pair.of(entity.getPositionVector(), AutoCrystal.YawStep.Full);

                        // attack crystal
                        if (module.attackCrystal(latestSpawn.getValue())) {

                            // add it to our list of attacked crystals
                            module.attackedCrystals.put(latestSpawn.getValue(), System.currentTimeMillis());

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

                            // clear
                            module.explodeClearance = false;
                            module.explodeTimer.reset();

                            // reset spawned crystals
                            module.spawnedCrystals.clear();
                        }
                    }
                }
            }

            // place on thread for faster response time
            else if (module.attackDelay.getValue() > module.attackDelay.getMin()) {

                // we found crystals to explode
                if (module.explosion != null) {
                    AutoCrystal.DamageHolder<EntityEnderCrystal> explosion = module.explosion;
                    if(explosion == null) return;

                    // calculate if we have passed delays (old delays)
                    // place delay based on place speeds
                    long explodeDelay = (long) (module.attackDelay.getValue() * 25);

                    // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                    long switchDelay = module.explodeSwitchDelay.getValue().longValue() * 25L;

                    // we have waited the proper time ???
                    boolean delayed = module.explodeTimer.passedMs(explodeDelay) && module.switchTimer.passedMs(switchDelay);

                    // check if we have passed the explode time
                    if (module.explodeClearance || delayed) {

                        // face the crystal
                        module.angleVector = Pair.of(explosion.getDamageSource().getPositionVector(), AutoCrystal.YawStep.Full);

                        // attack crystal
                        if (module.attackCrystal(explosion.getDamageSource())) {

                            // add it to our list of attacked crystals
                            module.attackedCrystals.put(explosion.getDamageSource().getEntityId(), System.currentTimeMillis());

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

                            // clear
                            module.explodeClearance = false;
                            module.explodeTimer.reset();
                        }
                    }
                }
            }
        }
    }
}
