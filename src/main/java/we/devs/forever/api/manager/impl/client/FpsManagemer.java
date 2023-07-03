package we.devs.forever.api.manager.impl.client;


import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import we.devs.forever.api.manager.api.AbstractManager;

import java.util.LinkedList;

public final class FpsManagemer extends AbstractManager {
    private int fps;
    private final LinkedList<Long> frames = new LinkedList<>( );

    public FpsManagemer() {
        super("Name");
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        long time = System.nanoTime( );

        frames.add( time );

        while ( true ) {
            long f = frames.getFirst( );
            final long ONE_SECOND = 1000000L * 1000L;
            if ( time - f > ONE_SECOND ) frames.remove( );
            else break;
        }

        fps = frames.size( );
    }

    public int getFPS( ) {
        return fps;
    }

    public float getFrametime( ) {
        return 1.0f / fps;
    }

    @Override
    protected void onLoad() {

    }

    @Override
    protected void onUnload() {

    }
}