package we.devs.forever.loader.custompath;

import net.minecraft.launchwrapper.Launch;
import sun.misc.Resource;
import sun.misc.URLClassPath;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

public class CustomClassPath extends URLClassPath
{

    private final URLClassPath parent;
    public final Map< String, byte[] > resources;

    public CustomClassPath(URLClassPath ucp, Map< String, byte[] > resources )
    {
        super( Launch.classLoader.getURLs() );
        parent = ucp;
        this.resources = resources;
    }

    public URL makeURLResource( String name )
    {
        final byte[] resource = resources.get( name );
        try
        {
            return resource == null ? null : new URL( null, "file:///!ForeverClient/" + name, new URLStreamHandler()
            {

                @Override
                protected URLConnection openConnection( URL url )
                {
                    return new URLConnection( url )
                    {

                        @Override
                        public void connect()
                        {
                        }

                        @Override
                        public InputStream getInputStream()
                        {
                            try
                            {
                                return new ByteArrayInputStream( resource );
                            } catch ( Throwable t )
                            {
                                t.printStackTrace( System.out );
                                return null;
                            }
                        }

                        @Override
                        public int getContentLength()
                        {
                            try
                            {
                                return resource.length;
                            } catch ( Throwable t )
                            {
                                t.printStackTrace();
                                return 0;
                            }
                        }
                    };
                }
            } );
        } catch ( MalformedURLException e )
        {
            return null;
        }
    }

    public Resource makeResource( final String key )
    {
        final URL resource = this.makeURLResource( key );
        return resource == null ? null : new Resource()
        {
            URLConnection connection = null;

            public String getName()
            {
                return key;
            }

            public URL getURL()
            {
                return resource;
            }

            public URL getCodeSourceURL()
            {
                return resource;
            }

            public InputStream getInputStream() throws IOException
            {
                if ( this.connection == null )
                {
                    this.connection = resource.openConnection();
                }
                return this.connection.getInputStream();
            }

            public int getContentLength() throws IOException
            {
                if ( this.connection == null )
                {
                    this.connection = resource.openConnection();
                }
                return this.connection.getContentLength();
            }
        };
    }

    public synchronized List< IOException > closeLoaders()
    {
        return this.parent.closeLoaders();
    }

    public synchronized void addURL( URL url )
    {
        this.parent.addURL( url );
    }

    public URL[] getURLs()
    {
        return this.parent.getURLs();
    }

    public URL findResource( String name, boolean check )
    {
        URL resource = this.parent.findResource( name, check );
        if ( resource != null )
        {
            return resource;
        }
        if ( resources.containsKey( name ) )
        {
            return this.makeURLResource( name );
        }
        return null;
    }

    public Resource getResource( String name, boolean check )
    {
        Resource resource = this.parent.getResource( name, check );
        if ( resource != null )
        {
            return resource;
        }
        if ( resources.containsKey( name ) )
        {
            return this.makeResource( name );
        }
        return null;
    }

    public Enumeration< URL > findResources( String name, boolean check )
    {
        return this.parent.findResources( name, check );
    }

    public Resource getResource( String name )
    {
        Resource resource = this.parent.getResource( name );
        if ( resource != null )
        {
            return resource;
        }
        if ( resources.containsKey( name ) )
        {
            return this.makeResource( name );
        }
        return null;
    }

    public Enumeration< Resource > getResources( String name, boolean check )
    {
        return this.parent.getResources( name, check );
    }

    public Enumeration< Resource > getResources( String name )
    {
        return this.parent.getResources( name );
    }

    public URL checkURL( URL url )
    {
        return this.parent.checkURL( url );
    }

}
