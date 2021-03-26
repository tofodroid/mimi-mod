package io.github.tofodroid.mods.mimi.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.github.tofodroid.mods.mimi.common.MIMIMod;

import org.apache.commons.io.IOUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;

public abstract class PlayerNameUtils {
    public static String getPlayerNameFromUUID(UUID uuid, World worldIn) {
        if(uuid == null) {
            return null;
        }

        // First, check in current world
        if(worldIn != null) {
            PlayerEntity player = worldIn.getPlayerByUuid(uuid);

            if(player != null) {
                return player.getName().getString();
            }
        }

        return lookupPlayerNameByUUID(uuid);
    }

    @SuppressWarnings({"deprecation","unchecked"})
    private static String lookupPlayerNameByUUID(UUID uuid) {
        String url = "https://api.mojang.com/user/profiles/"+uuid.toString().replace("-", "")+"/names";
        try {
            String nameJson = IOUtils.toString(new URL(url));           
            ArrayList<Map<String,String>> nameList = new Gson().fromJson(nameJson, ArrayList.class);
            if(nameList != null && !nameList.isEmpty() && nameList.get(0).get("name") != null) return nameList.get(0).get("name");
        } catch (ClassCastException | JsonSyntaxException | IOException e) {
            MIMIMod.LOGGER.error("Failed to fetch username from UUID. Error: ", e);
        }
        return "Unknown";
    }
}
