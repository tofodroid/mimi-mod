package io.github.tofodroid.mods.mimi.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.tofodroid.mods.mimi.common.config.ModConfigs;

public abstract class RemoteMidiUrlUtils {
    public static Boolean validHostString(String url) {
        Pattern regex = Pattern.compile("^[0-9a-zA-Z][0-9a-zA-Z.]*?\\.[a-zA-Z][a-zA-Z][a-zA-Z]*?$");
        Matcher matcher = regex.matcher(url);
        return !(url.toLowerCase().startsWith("www.") || !matcher.find());
    }

    public static Boolean validateMidiUrl(String url) {
        try {
            Pattern regex = Pattern.compile("^[hH][tT][tT][pP][sS]?:\\/\\/.*\\/[^\\/\\\\\\s]*[^\\/.\\\\\\s]$");
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

    public static Boolean validateFilename(String name) {
        Pattern regex = Pattern.compile("^[0-9a-zA-Z][0-9a-zA-Z-_]*?[0-9a-zA-Z]$");
        Matcher matcher = regex.matcher(name);
        return name.length() <= 64 && matcher.find();
    }

    public static Boolean validateFileUrl(String url) {
        Pattern regex = Pattern.compile("^server:\\/\\/[0-9a-zA-Z][0-9a-zA-Z-_]*?[0-9a-zA-Z]$?$");
        Matcher matcher = regex.matcher(url);
        if(!matcher.find()) {
            return false;
        }
        return true;
    }

    public static Boolean validateMidiHost(String url) {
        try {
            URL realUrl = new URL(url);

            if(ModConfigs.COMMON.getAllowedHostsList().isEmpty()) {
                return true;
            }

            for(String host : ModConfigs.COMMON.getAllowedHostsList()) {
                if(realUrl.getHost().toLowerCase().endsWith(host)) {
                    return true;
                }
            }
            return false;
        } catch (MalformedURLException e) {
            return false;
        }
    }
}
