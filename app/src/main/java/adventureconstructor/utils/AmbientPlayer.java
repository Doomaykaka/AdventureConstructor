package adventureconstructor.utils;

import java.io.File;
import java.io.IOException;
import javax.sound.sampled.*;

public class AmbientPlayer {
    private Clip clip = null;
    private String currentPath = null;

    public void play(String path, boolean loop) {
        stop();

        if (path == null || path.isEmpty()) return;

        File f = new File(path);

        if (!f.exists()) return;

        launchPlayer(path, loop, f);
    }

    private void launchPlayer(String path, boolean loop, File f) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(f);
            clip = AudioSystem.getClip();
            clip.open(ais);

            if (loop) clip.loop(Clip.LOOP_CONTINUOUSLY);
            else clip.start();

            currentPath = path;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("AmbientPlayer: " + e.getMessage());
        }
    }

    public void stop() {
        if (clip != null) {
            if (clip.isRunning()) clip.stop();
            clip.close();
            clip = null;
        }
        currentPath = null;
    }

    public boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    public String getCurrentPath() {
        return currentPath;
    }
}
