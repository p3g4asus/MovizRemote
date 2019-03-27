package mfz.movizremote.utils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class VelocityUtils {

    public static InputStream getInputStream(String fileName)
            throws FileNotFoundException {
        // try to find the resource in local class resources
        InputStream localStream = VelocityUtils.class.getResourceAsStream("/"
                + fileName);
        if (localStream != null)
            return new BufferedInputStream(localStream);

        // find file in local data directory
        // if (new java.io.File(fileName).exists()) {
        return new FileInputStream(fileName);
        // }

        // load the resource from web
        // return new URL(DATA_DIRECTORY_CLIENT + fileName).openStream();
    }
}
