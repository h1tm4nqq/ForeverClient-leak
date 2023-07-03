package we.devs.forever.client.modules.impl.combat.autocrystalold.threads;

import net.minecraft.init.Items;
import we.devs.forever.api.util.client.TimerUtil;
import we.devs.forever.api.util.entity.EntityUtil;
import we.devs.forever.api.util.enums.AutoSwitch;
import we.devs.forever.api.util.math.MathUtil;
import we.devs.forever.api.util.player.inventory.InventoryUtil;
import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.modules.impl.combat.autocrystalold.enums.Inhibit;

import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

public class CalcThread extends ModuleThread<AutoCrystal> {
    public CalcThread(AutoCrystal module) {
        super(module, "AutoCrystalThread", 1);
    }

    TimerUtil timerUtil = new TimerUtil();

    @Override
    public void invoke() {

        // Thread.sleep(0,400000);
//                 Thread.sleep(100);
//                if (timerUtil.passedMs(2000L)) {
//                    timerUtil.reset();
//                    System.runFinalization();
//                }
        if (fullNullCheck()) return;
        module.targets = new LinkedList<>(mc.world.playerEntities)
                .stream()
                .filter(player -> EntityUtil.isValid(player, module.range.getValue()))
                .map(entityPlayer -> MathUtil.predictPlayer(entityPlayer, module.targetPredictTicks.getValue()))
                .collect(Collectors.toCollection(LinkedList::new));
        if (module.targets.isEmpty()) {
            module.placePosInfo = null;
            module.breakPosInfo = null;
            return;
        }
        module.offhand = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
        module.mainhand = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL
                || (module.crysSwap.getValue() != AutoSwitch.None && InventoryUtil.getItemHotbar(Items.END_CRYSTAL) != -1);
        module.self = module.selfPredict.getValue() ?  MathUtil.predictPlayer(mc.player, module.selfPredictTicks.getValue()).getTarget() : mc.player;
        module.selfHealth = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        long old = System.nanoTime();
        calc();
        module.placeTime = (float) (System.nanoTime() - old) / 1000000L;
        if (module.inhibit.getValue().equals(Inhibit.Strict) && module.inhibitFactor.getValue() > module.inhibitFactor.getMin()) {
            Map.Entry<Long, Integer> latestSpawn = module.spawnedCrystals.firstEntry();

            // attack latest spawn if waited
            if (latestSpawn != null) {
                float time = System.currentTimeMillis() - latestSpawn.getKey();

//                        System.out.printf("1 Delayed %s , Time %s\n", delay, time);
                // calculate if we have passed delays (old delays)
                // place delay based on place speeds
                double explodeDelay = module.inhibitFactor.getValue() * 50;

                // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                long switchDelay = module.breakSwitchDelay.getValue().longValue() * 25L;

                // we have waited the proper time ???
                boolean delayed = System.currentTimeMillis() - latestSpawn.getKey() >= explodeDelay && module.switchTimer.passedMs(switchDelay);

                // check if we have passed the explode time
                if (module.explodeClearance || delayed) {

                    // face the crystal
//                            angleVector = Pair.of(mc.world.getEntityByID(latestSpawn.getValue()).getPositionVector(), YawStep.FULL);

                    // attack crystal
                    if (module.onBreak(mc.world.getEntityByID(latestSpawn.getValue()))) {
//                                module.counterBreak++;
                        // add it to our list of attacked crystals
                        module.breakLocations.put(latestSpawn.getValue(), System.currentTimeMillis());
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
                        module.breakTimer.reset();

                        // reset spawned crystals
                        module.spawnedCrystals.clear();
                    }
                }
            }
        } else if (module.attackDelay.getValue() > module.attackDelay.getMin()) {
            if (module.breakPosInfo != null) {

                // calculate if we have passed delays (old delays)
                // place delay based on place speeds
                long explodeDelay = (long) (module.attackDelay.getValue() * 25);

                // switch delay based on switch delays (NCP; some servers don't allow attacking right after you've switched your held item)
                long switchDelay = module.breakSwitchDelay.getValue().longValue() * 25L;

                // we have waited the proper time ???
                boolean delayed = module.breakTimer.passedMs(explodeDelay) && module.switchTimer.passedMs(switchDelay);

                // check if we have passed the explode time
                if (module.explodeClearance || delayed) {

                    // face the crystal
//                            angleVector = Pair.of(explosion.getDamageSource().getPositionVector(), YawStep.FULL);

                    // attack crystal
                    if (module.onBreak(module.breakPosInfo.getValue())) {

                        // add it to our list of attacked crystals
                        module.breakLocations.put(module.breakPosInfo.getValue().getEntityId(), System.currentTimeMillis());

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
                        module.breakTimer.reset();
                    }
                }
            }
        }
    }

    private void place() {
        if (!module.place.getValue()) return;
        if (module.cantPlace()) return;
        if (!(module.mainhand || module.offhand)) return;
        if (module.placeTimer.passedMs(500 - module.placeSpeed.getValue() * 25L) || module.placeClearance) {
            module.facing = module.listenerOnUpdateWalkingPlayer.handlePlaceRotation(module.placePosInfo.getValue());
            module.onPlace(module.placePosInfo.getValue());
            module.placeClearance = false;
            if (module.placePosInfo.isByQueue()) {
                module.placePosInfoQueue.clear();
            }
        }
    }

    private void calc() {
        try {
            if (module.breakPosInfoQueue.isEmpty()) {
                module.breakPosInfo = module.calcBreak(true);
            } else {
                module.breakPosInfo = module.breakPosInfoQueue.pollFirst();
            }
            if (module.placePosInfoQueue.isEmpty()) {
                module.placePosInfo = module.calcPlace();
            } else {
                module.placePosInfo = module.placePosInfoQueue.pollFirst();
//                System.out.println(module.placePosInfo);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }
}
