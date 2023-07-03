package we.devs.forever.api.event.events.player;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import we.devs.forever.api.event.EventStage;

public class ItemUsedEvent
        extends EventStage {
    private final EntityLivingBase entityLiving;
    private final ItemStack stack;

    public ItemUsedEvent(EntityLivingBase entityLiving, ItemStack stack) {
        this.entityLiving = entityLiving;
        this.stack = stack;
    }

    public EntityLivingBase getEntityLiving() {
        return this.entityLiving;
    }

    public ItemStack getStack() {
        return this.stack;
    }
}
