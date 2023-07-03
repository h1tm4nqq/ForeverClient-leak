package we.devs.forever.client.modules.impl.combat.burrow.enums;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum Mode {
    All(true),
    Obsidian(true, Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ANVIL),
    EChest(true, Blocks.ENDER_CHEST, Blocks.OBSIDIAN, Blocks.ANVIL),
    Block(true, Blocks.OBSIDIAN, Blocks.ENDER_CHEST, Blocks.ANVIL),
    Web(false, Blocks.WEB),
    Skulls(false, Blocks.SKULL);
    private final boolean isBurrowBlock;
    private final net.minecraft.block.Block[] blocks;

    Mode(boolean isBurrowBlock, Block... blocks) {
        this.isBurrowBlock = isBurrowBlock;
        this.blocks = blocks;
    }

    public boolean isBurrowBlock() {
        return isBurrowBlock;
    }

    public Block[] getBlocks() {
        return blocks;
    }
}