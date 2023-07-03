package we.devs.forever.api.event.events.player;

import we.devs.forever.api.event.EventStage;

public
class MotionEvent extends EventStage {
    private final double initialX;
    private final double initialY;
    private final double initialZ;
    private final float initialYaw;
    private final float initialPitch;
    private final boolean initialOnGround;

    private double x;
    private double y;
    private double z;
    private float rotationYaw;
    private float rotationPitch;
    private boolean onGround;
    protected boolean modified;

    public MotionEvent()
    {
        this(0, 0, 0, 0, 0, false);
    }


    public MotionEvent(MotionEvent event)
    {
        this(event.x,
                event.y,
                event.z,
                event.rotationYaw,
                event.rotationPitch,
                event.onGround);
    }

    public MotionEvent(double x,
                             double y,
                             double z,
                             float rotationYaw,
                             float rotationPitch,
                             boolean onGround)
    {
        super();
        this.x = x;
        this.y = y;
        this.z = z;
        this.rotationYaw = rotationYaw;
        this.rotationPitch = rotationPitch;
        this.onGround = onGround;
        this.initialX = x;
        this.initialY = y;
        this.initialZ = z;
        this.initialYaw = rotationYaw;
        this.initialPitch = rotationPitch;
        this.initialOnGround = onGround;
    }

    public static class Pre extends MotionEvent {
        public Pre(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            super(x, y, z, yaw, pitch, onGround);
        }
        public Pre()
        {
            this(0, 0, 0, 0, 0, false);
        }
        public Pre(MotionEvent event)
        {
            this(event.x,
                    event.y,
                    event.z,
                    event.rotationYaw,
                    event.rotationPitch,
                    event.onGround);
        }
    }

    public static class Post extends MotionEvent {
        public Post(double x, double y, double z, float yaw, float pitch, boolean onGround) {
            super(x, y, z, yaw, pitch, onGround);
        }
        public Post()
        {
            this(0, 0, 0, 0, 0, false);
        }
        public Post(MotionEvent event)
        {
            this(event.x,
                    event.y,
                    event.z,
                    event.rotationYaw,
                    event.rotationPitch,
                    event.onGround);
        }
    }

    public double getInitialX()
    {
        return initialX;
    }

    public double getInitialY()
    {
        return initialY;
    }

    public double getInitialZ()
    {
        return initialZ;
    }

    public float getInitialYaw()
    {
        return initialYaw;
    }

    public float getInitialPitch()
    {
        return initialPitch;
    }

    public boolean isInitialOnGround()
    {
        return initialOnGround;
    }

    public float getRotationYaw()
    {
        return rotationYaw;
    }

    public float getRotationPitch()
    {
        return rotationPitch;
    }

    public boolean isModified()
    {
        return modified;
    }

    public double getX()
    {
        return x;
    }

    public void setX(double x)
    {
        this.modified = true;
        this.x = x;
    }

    public double getY()
    {
        return y;
    }

    public void setY(double y)
    {
        this.modified = true;
        this.y = y;
    }

    public double getZ()
    {
        return z;
    }

    public void setZ(double z)
    {
        this.modified = true;
        this.z = z;
    }

    public float getYaw()
    {
        return rotationYaw;
    }

    public void setYaw(float rotationYaw)
    {
        this.modified = true;
        this.rotationYaw = rotationYaw;
    }

    public float getPitch()
    {
        return rotationPitch;
    }

    public void setPitch(float rotationPitch)
    {
        this.modified = true;
        this.rotationPitch = rotationPitch;
    }

    public void setRotations(float yaw, float pitch) {
        setYaw(yaw);
        setPitch(pitch);
    }

    public boolean isOnGround()
    {
        return onGround;
    }

    public void setOnGround(boolean onGround)
    {
        this.modified = true;
        this.onGround = onGround;
    }


}
