package mfz.movizremote.utils;

import java.awt.Rectangle;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.Properties;

import mfz.movizremote.utils.VelocityUtils;

public class Configuration {
    public final static String CONF_FILENAME_SWITCH = "conf.filename";
    public final static String DEFAULT_CONFIURATION_FILE = "conf.properties";
    protected Properties internal = null, defaults = null;
    protected String path = "", propsCl = "";
    static private Configuration instance = null;

    static private Properties getDefaults() {
        try {
            Properties def = new Properties();
            def.load(VelocityUtils
                    .getInputStream("mfz/movizremote/utils/defconfig.properties"));
            return def;
        } catch (Exception e) {
            e.printStackTrace();
            return new Properties();
        }
    }

    public Rectangle loadRectangle(String name) {
        String xs = internal.getProperty(name + ".x", null);
        String ys = internal.getProperty(name + ".y", null);
        String ws = internal.getProperty(name + ".w", null);
        String hs = internal.getProperty(name + ".h", null);
        if (xs == null || ys == null || ws == null || hs == null)
            return null;
        else {
            try {
                int x = Integer.parseInt(xs);
                int y = Integer.parseInt(ys);
                int w = Integer.parseInt(ws);
                int h = Integer.parseInt(hs);
                return new Rectangle(x, y, w, h);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public void saveRectangle(Rectangle r, String name) {
        if (r == null) {
            internal.remove(name + ".x");
            internal.remove(name + ".y");
            internal.remove(name + ".w");
            internal.remove(name + ".h");
        } else {
            int x = (int) r.getX();
            int y = (int) r.getY();
            int w = (int) r.getWidth();
            int h = (int) r.getHeight();

            internal.setProperty(name + ".x", "" + x);
            internal.setProperty(name + ".y", "" + y);
            internal.setProperty(name + ".w", "" + w);
            internal.setProperty(name + ".h", "" + h);
        }
    }

    public static Configuration newInstance(String[] v) {
        if (instance != null)
            return instance;
        else if (v == null)
            return new Configuration();
        else
            return new Configuration(v);
    }

    private Configuration(String[] args) {
        this();
        int ln, start = 0;
        if (args.length > 0
                && args[0].startsWith(CONF_FILENAME_SWITCH + "=")
                && args[0].length() > (ln = (CONF_FILENAME_SWITCH + "=")
                        .length())) {
            path = args[0].substring(ln);
            start = 1;
        }

        for (int i = start; i < args.length; i++) {
            propsCl += args[i].replace("\\", "\\\\") + "\n";
        }
    }

    private Configuration() {
        defaults = getDefaults();
        internal = new Properties(defaults);
        path = DEFAULT_CONFIURATION_FILE;
        instance = this;
    }

    private Configuration(String filepath) {
        this();
        path = filepath;
        instance = this;
    }

    public void load() throws Exception {
        internal.load(VelocityUtils.getInputStream(path));
        if (!propsCl.isEmpty()) {
            internal.load(new StringReader(propsCl));
        }
    }

    public String get(String prop) {
        return internal.getProperty(prop);
    }

    public Integer getInt(String prop, Integer low, Integer up) {
        try {
            int rv = Integer.parseInt(internal.getProperty(prop));
            if ((low != null && rv < low) || (up != null && rv > up))
                throw new Exception("Limits not honoured");
            else
                return rv;
        } catch (Exception e) {
            return getIntDefault(prop);
        }
    }

    public String getDefault(String prop) {
        return internal.getProperty(prop);
    }

    public Integer getIntDefault(String prop) {
        try {
            return Integer.parseInt(defaults.getProperty(prop));
        } catch (Exception e) {
            return null;
        }
    }

    public void set(String prop, String val) {
        internal.setProperty(prop, val);
    }

    public void store() throws Exception {
        FileWriter fw = new FileWriter(path);
        internal.store(fw, "configuration settings");
    }

    public String getPath() {
        // TODO Auto-generated method stub
        return path;
    }
}
