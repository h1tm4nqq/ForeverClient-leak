package we.devs.forever.api.event.events.player;

import net.minecraft.entity.MoverType;
import we.devs.forever.api.event.EventStage;

public
class MoveEvent extends EventStage {

    private MoverType type;
    private double motionX, motionY, motionZ;

    public MoveEvent(MoverType type, double x, double y, double z) {
        this.type = type;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
    }

    public MoverType getType() {
        return type;
    }

    public void setType(MoverType type) {
        this.type = type;
    }

    public double getX() {
        return motionX;
    }

    public void setX(double motionX) {
        this.motionX = motionX;
    }

    public double getY() {
        return motionY;
    }

    public void setY(double motionY) {
        this.motionY = motionY;
    }

    public double getZ() {
        return motionZ;
    }

    public void setZ(double motionZ) {
        this.motionZ = motionZ;
    }
}
