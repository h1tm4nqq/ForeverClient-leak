package we.devs.forever.client.modules.impl.render.shader;

import net.minecraft.util.math.Vec3d;
import we.devs.forever.api.event.events.render.Render3DEvent;
import we.devs.forever.client.modules.api.ModuleThread;
import we.devs.forever.client.modules.impl.combat.autocrystalold.AutoCrystal;
import we.devs.forever.client.modules.impl.render.BreakHighlight;
import we.devs.forever.client.modules.impl.render.BurrowHighlight;
import we.devs.forever.client.modules.impl.render.PopChams;
import we.devs.forever.client.modules.impl.render.breadcrumbs.BreadCrumbs;
import we.devs.forever.client.modules.impl.render.holeesp.HoleESP;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class RenderThread extends ModuleThread<Shader> {

    public RenderThread(Shader module) {
        super(module,25);
    }

    @SuppressWarnings("all")
    @Override
    public void invoke() {
                if (fullNullCheck()) return;

                List<Runnable> temp1 = new CopyOnWriteArrayList<>();
                List<Runnable> temp2 = new CopyOnWriteArrayList<>();
                new LinkedList<>(mc.world.loadedEntityList)
                        .stream()
                        .forEach(entity -> {
                            if (module.renderPlayersOutLine(entity)) {
                                temp1.add(() -> {
                                    Vec3d vector = module.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                                    Objects.requireNonNull(mc.getRenderManager().getEntityRenderObject(entity)).doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, mc.getRenderPartialTicks());
                                });
                            }
                            if (module.renderPlayersFill(entity)) {
                                temp2.add(() -> {
                                    Vec3d vector = module.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
                                    Objects.requireNonNull(mc.getRenderManager().getEntityRenderObject(entity)).doRender(entity, vector.x, vector.y, vector.z, entity.rotationYaw, mc.getRenderPartialTicks());
                                });
                            }
                        });
                module.entitiesOutLine = temp1;
                module.entitiesFill = temp2;
                if (module.checkFill(HoleESP.esp))
                    module.entitiesFill.add(() -> HoleESP.esp.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));
                if (module.checkOutLine(HoleESP.esp))
                    module.entitiesOutLine.add(() -> HoleESP.esp.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));

                if (module.checkFill(AutoCrystal.INSTANCE))
                    module.entitiesFill.add(() -> AutoCrystal.INSTANCE.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));
                if (module.checkOutLine(AutoCrystal.INSTANCE))
                    module.entitiesOutLine.add(() -> AutoCrystal.INSTANCE.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));


                if (module.checkFill(PopChams.popChams))
                    module.entitiesFill.add(() -> PopChams.popChams.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));
                if (module.checkOutLine(PopChams.popChams))
                    module.entitiesOutLine.add(() -> PopChams.popChams.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));

                if (module.checkFill(BreadCrumbs.breadCrumbs))
                    module.entitiesFill.add(() -> BreadCrumbs.breadCrumbs.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));
                if (module.checkOutLine(BreadCrumbs.breadCrumbs))
                    module.entitiesOutLine.add(() -> BreadCrumbs.breadCrumbs.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));

                if (module.checkFill(BreakHighlight.breakHighlight))
                    module.entitiesFill.add(() -> BreakHighlight.breakHighlight.onAltRender3D(mc.getRenderPartialTicks()));
                if (module.checkOutLine(BreakHighlight.breakHighlight))
                    module.entitiesOutLine.add(() -> BreakHighlight.breakHighlight.onAltRender3D(mc.getRenderPartialTicks()));

                if (module.checkFill(BurrowHighlight.burrowHighlight))
                    module.entitiesFill.add(() -> BurrowHighlight.burrowHighlight.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));
                if (module.checkOutLine(BurrowHighlight.burrowHighlight))
                    module.entitiesOutLine.add(() -> BurrowHighlight.burrowHighlight.onRender3D(new Render3DEvent(mc.getRenderPartialTicks())));

    }
}
