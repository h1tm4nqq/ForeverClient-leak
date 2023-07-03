package we.devs.forever.api.util.math.path;

import net.minecraft.util.math.BlockPos;

public class BasePath {

    private final BlockPos pos;
    private final int count;
    private final BasePathGroup group;
    private boolean isPlaced = false;

    public BasePath(BlockPos pos, int count, BasePathGroup group) {
        this.pos = pos;
        this.count = count;
        this.group = group;
    }

    @Override
    public String toString() {
        return "BasePath{" +
                "pos=" + pos +
                ", count=" + count +
                ", group=" + group +
                '}';
    }

    public boolean isPlaced() {
        return isPlaced;
    }

    public void setPlaced(boolean placed) {
        isPlaced = placed;
    }

    public BlockPos getPos() {
        return pos;
    }

    public int getCount() {
        return count;
    }

    public BasePathGroup getGroup() {
        return group;
    }
}
