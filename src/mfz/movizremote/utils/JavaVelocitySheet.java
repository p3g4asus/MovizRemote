package mfz.movizremote.utils;

import java.io.InputStream;
import java.util.Locale;

import org.apache.velocity.runtime.RuntimeInstance;

import com.moviz.lib.velocity.VelocitySheet;

public abstract class JavaVelocitySheet extends VelocitySheet {
    
    public static String TEMPLATE_DIR = "mfz/movizremote/gui/templates/";

    @Override
    public String getResourceAsString(String file) {
        return readResourceIntoString(file);
    }
    
    public static String readResourceIntoString(String file) {
        String out = "", cc = Locale.getDefault().getCountry().toLowerCase();
        String[] tris = new String[] { TEMPLATE_DIR + file + "." + cc,
                "/" + TEMPLATE_DIR + file + "." + cc, TEMPLATE_DIR + file,
                "/" + TEMPLATE_DIR + file, };
        for (String t : tris) {
            InputStream is = JavaVelocitySheet.class.getResourceAsStream(t);
            if (is != null) {
                out = readAllFile(is);
                if (!out.isEmpty())
                    return out;
            }
        }
        return "";
    }

    public JavaVelocitySheet() {
    }

    @Override
    protected String getTemplateFilePath(String templateName) {
        Configuration conf = Configuration.newInstance(null);
        return conf.get("template." + templateName + ".file");
    }

    @Override
    protected void initInternalResourceLoader(RuntimeInstance ri) {
        ri.setProperty("resource.loaders", "class");
        ri.setProperty("resource.loader.class.class",
        "mfz.movizremote.introspection.MyClasspathResourceLoader");
        ri.setProperty("class.resource.loader.relative", TEMPLATE_DIR);
        
    }


    @Override
    protected void runtimeInstanceCommonInit(RuntimeInstance ri) {
        
    }
}
