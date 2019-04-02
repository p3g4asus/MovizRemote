package mfz.movizremote.introspection;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

public class MyClasspathResourceLoader extends ResourceLoader {

    private String pathrelative = "";
    private Class<?> loader = MyClasspathResourceLoader.class;

    @Override
    public void init(ExtProperties configuration) {
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
    public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException {
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
        try {
            return new InputStreamReader(is,encoding);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new ResourceNotFoundException("Charset not supported");
        }
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
