package we.devs.forever.api.util.images;

import net.minecraft.util.ResourceLocation;


public class ImageFrameRes {
    private final int delay;
    private final ResourceLocation image;
    private final String disposal;
    private final int width, height;

    public ImageFrameRes(ResourceLocation image, int delay, String disposal, int width, int height){
        this.image = image;
        this.delay = delay;
        this.disposal = disposal;
        this.width = width;
        this.height = height;
    }

    public ImageFrameRes(ResourceLocation image){
        this.image = image;
        this.delay = -1;
        this.disposal = null;
        this.width = -1;
        this.height = -1;
    }

    public ResourceLocation getImage() {
        return image;
    }

    public int getDelay() {
        return delay;
    }

    public String getDisposal() {
        return disposal;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}