package we.devs.forever.api.event.events.player;

import we.devs.forever.api.event.EventStage;

public class RenderRotationsEvent extends EventStage {


    float rotationPitch, prevRotationPitch, rotationYawHead, prevRotationYawHead, renderYawOffset, prevRenderYawOffset;
    private boolean isRotate;

    public RenderRotationsEvent(float yaw, float prevYaw, float yawOffset, float prevYawOffset, float pitch, float prevPitch) {
        this.rotationYawHead = yaw;
        this.prevRotationYawHead = prevYaw;
        this.renderYawOffset = yawOffset;
        this.prevRenderYawOffset = prevYawOffset;
        this.rotationPitch = pitch;
        this.prevRotationPitch = prevPitch;
        isRotate = false;
    }

    public void setRotations(float yaw, float pitch) {
        rotationYawHead = renderYawOffset = yaw;
        rotationPitch =  pitch;
    }
    public void setPrevRotations(float prevYaw, float prevPitch) {
        prevRotationYawHead = prevRenderYawOffset = prevYaw;
        prevRotationPitch = prevPitch;
    }
    public void doRotate() {
        isRotate = true;
    }
    public boolean isRotate() {
        return isRotate;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public void setRotationPitch(float rotationPitch) {
        this.rotationPitch = rotationPitch;
    }
    public float getPrevRotationPitch() {
        return prevRotationPitch;
    }

    public void setPrevRotationPitch(float prevRotationPitch) {
        this.prevRotationPitch = prevRotationPitch;
    }

    public float getRotationYawHead() {
        return rotationYawHead;
    }

    public void setRotationYawHead(float rotationYawHead) {
        this.rotationYawHead = rotationYawHead;
    }

    public float getPrevRotationYawHead() {
        return prevRotationYawHead;
    }

    public void setPrevRotationYawHead(float prevRotationYawHead) {
        this.prevRotationYawHead = prevRotationYawHead;
    }

    public float getRenderYawOffset() {
        return renderYawOffset;
    }

    public void setRenderYawOffset(float renderYawOffset) {
        this.renderYawOffset = renderYawOffset;
    }

    public float getPrevRenderYawOffset() {
        return prevRenderYawOffset;
    }

    public void setPrevRenderYawOffset(float prevRenderYawOffset) {
        this.prevRenderYawOffset = prevRenderYawOffset;
    }
}
