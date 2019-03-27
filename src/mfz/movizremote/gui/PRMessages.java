package mfz.movizremote.gui;

import java.beans.Beans;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PRMessages {
    // //////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    // //////////////////////////////////////////////////////////////////////////
    private PRMessages() {
        // do not instantiate
    }

    // //////////////////////////////////////////////////////////////////////////
    //
    // Bundle access
    //
    // //////////////////////////////////////////////////////////////////////////
    private static final String BUNDLE_NAME = "mfz.movizremote.gui.messages"; //$NON-NLS-1$
    private static final ResourceBundle RESOURCE_BUNDLE = loadBundle();

    private static ResourceBundle loadBundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME);
    }

    // //////////////////////////////////////////////////////////////////////////
    //
    // Strings access
    //
    // //////////////////////////////////////////////////////////////////////////
    public static String getString(String key) {
        try {
            ResourceBundle bundle = Beans.isDesignTime() ? loadBundle()
                    : RESOURCE_BUNDLE;
            return bundle.getString(key);
        } catch (MissingResourceException e) {
            return "!" + key + "!";
        }
    }
}
