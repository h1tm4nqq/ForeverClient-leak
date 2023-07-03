package we.devs.forever.api.util.images;

import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.Sys;
import we.devs.forever.api.util.Util;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class Image implements Util {

    public List<ImageFrameRes> getImages() {
        return images;
    }

    private List<ImageFrameRes> images;
    private ResourceLocation image;
    private int offset;
    private int delay;
    private boolean firstUpdate;

    public boolean isGif() {
        return isGif;
    }

    private boolean isGif;
    private long lastUpdate;
    private long timeLeft;
    private File imageFile;



    public void deleteImages() {
        try {
            if(isGif) {
                images.forEach(imageFrameRes ->  mc.getTextureManager().deleteTexture(imageFrameRes.getImage()));
            } else {
                mc.getTextureManager().deleteTexture(getImage());
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }


    }


    public Image(String path) {
        try {
            if(Objects.equals(path, "NONE")
                    || Objects.equals(path, "")
                    ||Objects.equals(path, "none"))  {
                isGif = false;
                image = new ResourceLocation("textures/noimage.png");
                imageFile = null;
                return;
            }
            if (path.endsWith(".gif")) {
                imageFile = new File(path);
                if(!imageFile.exists()) {
                    isGif = false;
                    image = new ResourceLocation("textures/noimage.png");
                    imageFile = new File("NONE");
                    return;
                }
                images = ImageConvector.readGif(getImageFile());
                if(images.isEmpty()) {
                    isGif = false;
                    image = new ResourceLocation("textures/noimage.png");
                    return;
                }
                delay = 0;
                for (ImageFrameRes frame : images) {
                    delay += frame.getDelay() * 10;
                }
                delay /= images.size();
                isGif = true;
            } else if (path.endsWith(".jpg") || path.endsWith(".png") || path.endsWith(".jpeg")) {
                isGif = false;
                imageFile = new File(path);
                if(!imageFile.exists()) {
                    isGif = false;
                    image = new ResourceLocation("textures/noimage.png");
                    imageFile = new File("NONE");
                    return;
                }
                try {
                    DynamicTexture texture;
                    texture = new DynamicTexture(ImageIO.read(getImageFile()));
                    image = mc.getTextureManager().getDynamicTextureLocation(path, texture);
                } catch (IOException ignored) {
                }

            }
        } catch (Throwable t) {
            isGif = false;
           image = new ResourceLocation("textures/noimage.png");
            imageFile = new File("NONE");
        }

    }


    private boolean updateOffset() {
        if (images.size() == 0) return true;
        long now = getTime();
        long delta = now - lastUpdate;
        if (firstUpdate) {
            delta = 0;
            firstUpdate = false;
        }
        lastUpdate = now;
        timeLeft -= delta;
        if (timeLeft <= 0) {
            offset++;
            timeLeft = delay;
        }
        if (offset >= images.size()) offset = 0;
        return false;
    }


    public ResourceLocation getImage() {
        if (isGif) {
            if (updateOffset()) {
                return null;
            }
            return images.get(offset).getImage();
        } else {
            return image;
        }

    }

    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }


    public File getImageFile() {
        return imageFile;
    }

}

