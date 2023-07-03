package we.devs.forever.client.ui.alts.ias.tools;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SkinRender {
    private final File file;
    private final TextureManager textureManager;
    private DynamicTexture previewTexture;
    private ResourceLocation resourceLocation;

    public SkinRender(TextureManager textureManager, File file) {
        this.textureManager = textureManager;
        this.file = file;
    }

    private boolean loadPreview() {
        try {
            BufferedImage image = ImageIO.read(this.file);
            this.previewTexture = new DynamicTexture(image);
            this.resourceLocation = this.textureManager.getDynamicTextureLocation("ias", this.previewTexture);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void drawImage(int xPos, int yPos, int width, int height) {
        boolean successful;
        if (this.previewTexture == null && !(successful = this.loadPreview())) {
            System.out.println("Failure to load preview.");
            return;
        }
        this.previewTexture.updateDynamicTexture();
        this.textureManager.bindTexture(this.resourceLocation);
        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0.0f, 0.0f, width, height, 64.0f, 128.0f);
    }
}

