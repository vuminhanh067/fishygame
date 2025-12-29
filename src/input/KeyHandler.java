package input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import main.GamePanel;

public class KeyHandler extends KeyAdapter {
    
    public GamePanel gp;
    public boolean spacePressed;
    public KeyHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_SPACE) {
            spacePressed = true;
            System.out.println("Phím Space đã được nhấn!");
        }
        // Xử lý phím M (Menu/Pause)
        if (code == KeyEvent.VK_M) {
    // Prevent opening menu if level transition is happening
        if (!gp.startBannerShown && !gp.isVictoryBannerActive && !gp.isGameOverBannerActive) {
            if (gp.gameState == gp.playState) {
            gp.gameState = gp.pauseState;
        }   else if (gp.gameState == gp.pauseState) {
            gp.gameState = gp.playState;
        }
    }
}
    }
}