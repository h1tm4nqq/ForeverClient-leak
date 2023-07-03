package we.devs.forever.client.modules.impl.combat.autocrystalold.util;

import net.minecraft.entity.player.EntityPlayer;
import we.devs.forever.api.util.combat.PredictPlayer;

public class PosInfo<T> {
    private final T value;
    private final PredictPlayer target;
    private float damage;
    private float selfDamage;
    private final boolean byQueue;
    private boolean facePlace = false;

    public PosInfo(T value, PredictPlayer target, float damage, float selfDamage) {
        this.value = value;
        this.target = target;
        this.damage = damage;
        this.selfDamage = selfDamage;
        byQueue = false;
    }

    public PosInfo(T value, PredictPlayer target, float damage, float selfDamage, boolean byQueue) {
        this.value = value;
        this.target = target;
        this.damage = damage;
        this.selfDamage = selfDamage;
        this.byQueue = byQueue;
    }

    public EntityPlayer getOldPlayer() {
        if (target == null) return null;
        return target.getOldPlayer();
    }

    public boolean isByQueue() {
        return byQueue;
    }

    public T getValue() {
        return value;
    }

    public EntityPlayer getTarget() {
        if (target == null) return null;
        return target.getTarget();
    }

    public boolean isFacePlace() {
        return facePlace;
    }

    public void setFacePlace(boolean facePlace) {
        this.facePlace = facePlace;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getSelfDamage() {
        return selfDamage;
    }

    public void setSelfDamage(float selfDamage) {
        this.selfDamage = selfDamage;
    }

    @Override
    public String toString() {
        return "PosInfo{" +
                "value=" + value +
                ", target=" + target +
                ", damage=" + damage +
                ", selfDamage=" + selfDamage +
                ", byQueue=" + byQueue +
                '}';
    }
}
