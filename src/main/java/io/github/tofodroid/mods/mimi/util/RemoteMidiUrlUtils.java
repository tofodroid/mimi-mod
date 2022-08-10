package io.github.tofodroid.mods.mimi.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RemoteMidiUrlUtils {
    public static Boolean validateMidiUrl(String url) {
        try {
            Pattern regex = Pattern.compile("^[hH][tT][tT][pP][sS]?:\\/\\/.*\\/\\S\\S*.[mM][iI][dD][iI]?$");
            Matcher matcher = regex.matcher(url);
            if(!matcher.find()) {
                throw new MalformedURLException(null);
            }
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
