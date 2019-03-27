package mfz.movizremote.introspection;

import java.io.InputStream;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

public class MyClasspathResourceLoader extends ResourceLoader {

    private String pathrelative = "";
    private Class<?> loader = MyClasspathResourceLoader.class;

    @Override
    public void init(ExtendedProperties configuration) {
        String rel = configuration.getString("relative");
        if (rel != null) {
            while (rel.startsWith("/")) {
                rel = rel.substring(1);
            }
            pathrelative = rel;
        }
        rel = configuration.getString("loader");
        if (rel != null) {
            Class<?> load;
            try {
                load = Class.forName(rel);
                loader = load;
            } catch (ClassNotFoundException e) {
            }
        }

    }

    @Override
    public InputStream getResourceStream(String source)
            throws ResourceNotFoundException {
        InputStream is = loader.getClassLoader().getResourceAsStream(
                pathrelative + source);
        if (is == null) {
            is = loader.getClassLoader().getResourceAsStream(
                    "/" + pathrelative + source);
            if (is == null) {
                String msg = "ClasspathResourceLoader Error: cannot find resource "
                        + source;

                throw new ResourceNotFoundException(msg);
            }
        }
        return is;
    }

    @Override
    public boolean isSourceModified(Resource resource) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public long getLastModified(Resource resource) {
        // TODO Auto-generated method stub
        return 0;
    }

}
