package we.devs.forever.mixin.mixins.accessor;

import net.minecraft.network.play.client.CPacketUseEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = {CPacketUseEntity.class})
public
interface ICPacketUseEntity {
    @Accessor("entityId")
    int getId();
    @Accessor(value = "entityId")
    void setID(int var1);

    @Accessor(value = "action")
    void setAction(CPacketUseEntity.Action var1);

}
