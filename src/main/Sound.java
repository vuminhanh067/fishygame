package main;

import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class Sound {
    
    Clip[] clips;
    URL[] soundURL = new URL[30];
    FloatControl fc;

    public Sound() {
        soundURL[0] = getClass().getResource("/res/sound/background.wav");
        soundURL[1] = getClass().getResource("/res/sound/bigger.wav");
        soundURL[2] = getClass().getResource("/res/sound/bonus.wav");
        soundURL[3] = getClass().getResource("/res/sound/eat.wav");
        soundURL[4] = getClass().getResource("/res/sound/ending.wav");
        
        clips = new Clip[soundURL.length];
        loadAllSounds();
    }

    private void loadAllSounds() {
        for (int i = 0; i < soundURL.length; i++) {
            if (soundURL[i] != null) {
                try {
                    AudioInputStream ais = AudioSystem.getAudioInputStream(soundURL[i]);
                    clips[i] = AudioSystem.getClip();
                    clips[i].open(ais);
                } catch (Exception e) {
                    System.out.println("Error loading sound index: " + i);
                    e.printStackTrace();
                }
            }
        }
    }

    // Play Sound Effect (play once)
    public void play(int i) {
        if (clips[i] == null) return;
        
        // Stop if running to prevent glitching, then rewind
        if (clips[i].isRunning()) {
            clips[i].stop();
        }
        clips[i].setFramePosition(0);
        clips[i].start();
    }

    // Play Music (Loop continuously)
    public void loop(int i) {
        if (clips[i] == null) return;
        
        // Safety check: stop before looping to avoid overlap on the same clip
        if (clips[i].isRunning()) {
            clips[i].stop();
        }
        clips[i].setFramePosition(0);
        clips[i].loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stop(int i) {
        if (clips[i] == null) return;
        if (clips[i].isRunning()) {
            clips[i].stop();
        }
    }
    
    public void stopAll() {
        for (Clip c : clips) {
            if (c != null && c.isRunning()) {
                c.stop();
            }
        }
    }
}