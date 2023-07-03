package we.devs.forever.mixin.mixins.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Session;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface IMinecraft {
    @Accessor(value = "timer")
    Timer getTimer();

    @Accessor("session")
    void setSession(Session session);

    @Invoker("rightClickMouse")
    void rightClickMouse();

    @Accessor("rightClickDelayTimer")
    void setRightClickDelayTimer(int rightClickDelayTimer);

    @Accessor("renderViewEntity")
    void hookSetRenderViewEntity(Entity renderViewEntity);
}
