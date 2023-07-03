package we.devs.forever.client.modules.impl.combat.holefill;

import we.devs.forever.client.modules.api.ModuleThread;

public class CalcThread extends ModuleThread<HoleFill> {
    public CalcThread(HoleFill module) {
        super(module, 25);
    }

    @Override
    public void invoke() {
        if (fullNullCheck()) return;
//        if (module.ncpUtil.passedInTicks(3)) getTarget();
//        List<PredictPlayer> targets = new LinkedList<>(mc.world.playerEntities).stream()
//                .filter(player -> EntityUtil.isValid(player,module.range.getValue()))
//                .map(entityPlayer -> module.extrapolationTicks.getValue() > 0 ? MathUtil.predictPlayer(entityPlayer, module.extrapolationTicks.getValue()) : new PredictPlayer(entityPlayer, entityPlayer))
//                .collect(Collectors.toCollection(LinkedList::new));
//        if (targets.isEmpty()) return;
//        PredictPlayer self = module.selfExtrapolationTicks.getValue() > 0 ? MathUtil.predictPlayer(mc.player, module.selfExtrapolationTicks.getValue()) : new PredictPlayer(mc.player, mc.player);
//
//        List<BlockPos> holes = BlockUtil.getSphere(module.range.getValue(), false).stream()
//                .filter(blockPos -> BlockUtil.validBedrock(blockPos)
//                        || BlockUtil.validObi(blockPos)
//                        || BlockUtil.validTwoBlockObiXZ(blockPos) != null
//                        || BlockUtil.validTwoBlockBedrockXZ(blockPos) != null)
//                .filter(BlockUtil::isPosEmpty)
//                .filter(BlockUtil::checkEntity)
//                .filter(blockPos -> placeManager.inRange(blockPos, module.strict.getValue(),module.range.getValue(),module.wallrange.getValue()))
//
//                .collect(Collectors.toList());
//
//        Map<BlockPos,PredictPlayer> blockPoss = new HashMap<>();
//        double length = Double.MAX_VALUE;
//        for (PredictPlayer predictPlayer : targets)
//            for (BlockPos pos : holes) {
//                if (predictPlayer.getTarget().getDistanceSq(pos) > module.smartRange.getValue() * module.smartRange.getValue())
//                    continue;
//                if (module.selfSmart.getValue() && self.getTarget().getDistanceSq(pos) <= module.smartRange.getValue() * module.smartRange.getValue())
//                    continue;
//                if (length > self.getOldPlayer().getDistanceSq(pos)) {
//                    length = self.getTarget().getDistanceSq(pos);
//                    blockPoss.put(pos,predictPlayer);
//                }
//            }
//       module.placePos = blockPoss.entrySet().stream()
//                .sorted(Comparator.comparing(entry -> entry.getValue().getTarget().getDistanceSq(mc.player)))
//               .sorted(Comparator.comparing(entry -> entry.getValue().getTarget().getDistanceSq(entry.getKey())))
//               .map(Map.Entry::getKey)
//               .collect(Collectors.toList());

    }

}
