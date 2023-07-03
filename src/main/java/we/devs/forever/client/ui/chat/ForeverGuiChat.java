package we.devs.forever.client.ui.chat;


import net.minecraft.client.gui.GuiChat;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import we.devs.forever.client.Client;
import we.devs.forever.client.command.api.Command;
import we.devs.forever.client.command.api.SyntaxChunk;
import we.devs.forever.mixin.mixins.accessor.IGuiTextField;

import java.util.ArrayList;

public class ForeverGuiChat extends GuiChat {

    private boolean drawOutline = true;

    public ForeverGuiChat(String defaultText) {
        super(defaultText);
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
        } else if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
            switch (keyCode) {
                case Keyboard.KEY_UP: {
                    this.getSentHistory(-1);
                    break;
                }
                case Keyboard.KEY_DOWN: {
                    this.getSentHistory(1);
                    break;
                }
                case Keyboard.KEY_PRIOR: {
                    this.mc.ingameGUI.getChatGUI().scroll(this.mc.ingameGUI.getChatGUI().getLineCount() - 1);
                    break;
                }
                case Keyboard.KEY_NEXT: {
                    this.mc.ingameGUI.getChatGUI().scroll(-this.mc.ingameGUI.getChatGUI().getLineCount() + 1);
                    break;
                }
                case Keyboard.KEY_TAB: {
                    if (this.inputField.getText().length() > 1) {
                        String[] args = this.inputField.getText().replaceAll("([\\s])\\1+", "$1").split(" ");
                        System.out.println(args.length);
                        if (args.length > 1) {
                            Command cmd = Client.commandManager.getCommandByName(args[0].toLowerCase().replace(Client.commandManager.getPrefix(), ""));
                            System.out.println(cmd);
                            if (cmd != null && args.length - 2 <= cmd.getSyntaxChunks().size() - 1) {
                                SyntaxChunk chunk = cmd.getSyntaxChunks().get(args.length - 2);
                                if (chunk != null) {
                                    System.out.println(chunk.getName());
                                    String latestArg = args[args.length - 1];
                                    String completedArg = chunk.predict(latestArg);
                                    String text = inputField.getText();
                                    text = text.substring(0, text.length() - latestArg.length());
                                    text = text.concat(completedArg);
                                    this.inputField.setText(text);
                                }
                            }
                        } else if (args.length == 1) {
                            for (Command cmd : Client.commandManager.getCommands()) {
                                String completion = cmd.complete(args[0].substring(1));
                                if (completion != null) {
                                    this.inputField.setText(Client.commandManager.getPrefix() + completion);
                                    break;
                                }
                            }
                        }
                    }
                    break;
                }
                default: {
                    this.inputField.textboxKeyTyped(typedChar, keyCode);
                }
            }
        } else {
            String s = this.inputField.getText().trim();

            if (!s.isEmpty()) {
                this.sendChatMessage(s);
                mc.ingameGUI.getChatGUI().addToSentMessages(s);
            }
            this.mc.displayGuiScreen(null);
        }

        drawOutline = inputField.getText().replaceAll(" ", "").startsWith(Client.commandManager.getPrefix());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        drawRect(2, this.height - 14, this.width - 2, this.height - 2, Integer.MIN_VALUE);

        int tx = ((IGuiTextField) this.inputField).getFontRenderer().getStringWidth(this.inputField.getText() + "") + 4;
        int ty = this.inputField.getEnableBackgroundDrawing() ? this.inputField.y + (this.inputField.height - 8) / 2 : this.inputField.y;
        ((IGuiTextField) this.inputField).getFontRenderer().drawStringWithShadow(calculateTooltip(this.inputField.getText()), tx, ty, 0x606060);

        this.inputField.drawTextBox();

        if (!drawOutline) return;

        boolean blendEnabled = GL11.glIsEnabled(GL11.GL_BLEND);
        boolean texture2DEnabled = GL11.glIsEnabled(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glColor3f(41 / 255f, 53 / 255f, 215 / 255f);
        GL11.glLineWidth(2F);
        GL11.glBegin(GL11.GL_LINES);
        {
            int x = this.inputField.x - 2;
            int y = this.inputField.y - 2;
            int width = this.inputField.width;
            int height = this.inputField.height;

            // Upper Left Corner
            GL11.glVertex2d(x, y);
            // Upper Right Corner
            GL11.glVertex2d(x + width, y);
            // Upper Right Corner
            GL11.glVertex2d(x + width, y);
            // Lower Right Corner
            GL11.glVertex2d(x + width, y + height);
            // Lower Right Corner
            GL11.glVertex2d(x + width, y + height);
            // Lower Left Corner
            GL11.glVertex2d(x, y + height);
            // Lower Left Corner
            GL11.glVertex2d(x, y + height);
            // Upper Left Corner
            GL11.glVertex2d(x, y);

        }

        GL11.glEnd();

        if (blendEnabled) {
            GL11.glEnable(GL11.GL_BLEND);
        }

        if (texture2DEnabled) {
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }

    }

    private String calculateTooltip(String currentText) {
        if (this.inputField.getText().length() < 1) return "";
        if (!currentText.startsWith(Client.commandManager.getPrefix())) return "";
        String[] args = this.inputField.getText().split(" ");
        if (args.length > 1 || (this.inputField.getText().length() > 2 && this.inputField.getText().endsWith(" "))) {
            Command cmd = Client.commandManager.getCommandByName(args[0].toLowerCase().replace(Client.commandManager.getPrefix(), ""));
            if (cmd != null) {
                ArrayList<SyntaxChunk> chunks = cmd.getSyntaxChunks();
                StringBuilder str = new StringBuilder();
                int i = 0;
                for (SyntaxChunk chunk : chunks) {
                    if (i == args.length - 2) {
                        String text = chunk.predict(args[i + 1]);
                        chunk.setValue(text);
                        int len = args[i + 1].length();
                        try {
                            text = text.substring(len);
                        } catch (StringIndexOutOfBoundsException exception) {
                            exception.printStackTrace();
                        }
                        str.append(text);
                    } else if (i >= args.length - 1) {
                        str.append(" ").append(chunk.getName());
                    }
                    i++;
                }
                return str.toString();
            }
        } else if (args.length == 1) {
            for (Command cmd : Client.commandManager.getCommands()) {
                if (cmd.getName().toLowerCase().startsWith(args[0].substring(1).toLowerCase())) {
                    String text = cmd.getName();
                    text = text.substring(args[0].substring(1).length());
                    return text;
                }
            }
        }
        return "";
    }
}
