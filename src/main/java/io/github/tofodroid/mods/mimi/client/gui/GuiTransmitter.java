package io.github.tofodroid.mods.mimi.client.gui;

import java.nio.file.Path;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ClientMidiListPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket.CONTROL;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacket;
import net.minecraft.Util;
import com.mojang.blaze3d.vertex.PoseStack;

public class GuiTransmitter extends GuiTransmitterBlock {
    // Local File Controls
    protected static final Vector2Int SOURCE_FILTER_BUTTON = new Vector2Int(265,32);
    protected static final Vector2Int SOURCE_FILTER_SCREEN = new Vector2Int(283,33);
    protected static final Vector2Int OPEN_LOCAL_FOLDER_BUTTON = new Vector2Int(10,32);

    public GuiTransmitter(UUID musicPlayerId) {
        super(musicPlayerId);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SOURCE_FILTER_BUTTON))) {
            this.sendTransmitterCommand(CONTROL.SOURCE_M);
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(OPEN_LOCAL_FOLDER_BUTTON))) {
            Util.getPlatform().openUri(Path.of(MIMIMod.getProxy().clientMidiFiles().getCurrentFolderPath()).toUri());
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected  PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderGraphics(graphics, mouseX, mouseY, partialTicks);

        // Local Folder Button
        blit(graphics, START_X + 9, START_Y + 31, 173, 302, 17, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        // Source Filter Button & Screen Background
        blit(graphics, START_X + 264, START_Y + 31, 191, 302, 33, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        // Source Filter Screen
        blit(graphics, START_X + 283, START_Y + 33, 105 + (this.musicStatus.sourceMode.ordinal() * 13), 302, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        
        return graphics;
    }

    @Override
    protected Integer maxPlaylistSongTitleWidth() {
        return 318;
    }
    
    @Override
    public Boolean isSinglePlayerOrLANHost() {
        // Always return false because this GUI always shows the local folder button in the same spot
        return false;
    }
    
    @Override
    protected PoseStack renderPlaylistSongBadges(PoseStack graphics, BasicMidiInfo info, Integer songIndex, Integer minSong) {
        super.renderPlaylistSongBadges(graphics, info, songIndex, minSong);

        // Source Badge
        blit(graphics, START_X + 329, START_Y + getFirstSongY() - 1 + songIndex * 11, !info.serverMidi ? 163 : 154, 302, 9, 9, TEXTURE_SIZE, TEXTURE_SIZE);

        return graphics;
    }
    
    @Override
    protected void startRefreshSongList() {
        MIMIMod.getProxy().clientMidiFiles().refresh(true);
        NetworkProxy.sendToServer(new ClientMidiListPacket(MIMIMod.getProxy().clientMidiFiles().getSortedSongInfos()));
        NetworkProxy.sendToServer(new ServerMusicPlayerSongListPacket(musicPlayerId));
    }
}