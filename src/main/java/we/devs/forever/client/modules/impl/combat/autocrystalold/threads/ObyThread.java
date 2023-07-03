package we.devs.forever.client.modules.impl.combat.autocrystalold.threads;

import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;

public class ObyThread extends ModuleThread<AutoCrystal> {
    public ObyThread(AutoCrystal module) {
        super(module, 25);
    }

    @Override
    public void invoke() {




    }

//
//    public PlacePosInfo calcObyPos() {
//
//
//
//
//
//
//
//
//
//
//
//
//
//        PlacePosInfo max = new PlacePosInfo(null, null, 0, 36);
//        List<BlockPos> poss = BlockUtil.getSphere(placeRange.getValue(), false).parallelStream()
//                .filter(BlockUtil::isPosEmpty)
//                .filter(blockPos -> BlockUtil.canSee(blockPos) || mc.player.getPositionEyes(1F).squareDistanceTo(new Vec3d(blockPos.add(0.5, 0, 0.5))) < module.placeWallRange.getValue() * module.placeWallRange.getValue())
//                .filter(blockPos ->module.canPlaceCrystal(blockPos, false))
//                .collect(Collectors.toList());
//        for (PredictPlayer predictPlayer : targets) {
//            for (BlockPos pos : poss) {
//                module.calcUtil.addBlockState(pos, Blocks.OBSIDIAN);
//                PlacePosInfo placePosInfo = new PlacePosInfo(pos, predictPlayer, calcUtil.calculateDamage(pos, predictPlayer.getTarget()), module.calcUtil.calculateDamage(pos, mc.player));
//                if (placePosInfo.getDamage() >= placeMinDamage.getValue() && placePosInfo.getSelfDamage() <= module.maxSelfPlace.getValue()) {
//                    if (placePosInfo.getDamage() >= max.getDamage() && EntityUtil.getHealth(mc.player) - placePosInfo.getSelfDamage() >= module.stopHealth.getValue()) {
//                        max = placePosInfo;
//
//                    }
//                }
//                module.calcUtil.clearAllStates();
//            }
//        }
//        return max;
//    }



}
