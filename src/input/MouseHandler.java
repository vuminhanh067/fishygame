package input;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import main.GamePanel;

public class MouseHandler extends MouseAdapter {
    
    private GamePanel gp;
    public int mouseX, mouseY; 

    public MouseHandler(GamePanel gp) {
        this.gp = gp;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        
        if (gp.gameState == gp.pauseState || gp.gameState == gp.winState || gp.gameState == gp.gameOverState) {
            
            // Prevent hover effects during transitions
            if (gp.startBannerShown) return;

            // >> LOGIC 1: IF SHOWING MAIN MENU
            if (!gp.showOptions) {
                if (gp.newGameRect != null && gp.newGameRect.contains(mouseX, mouseY)) {
                    gp.commandNum = 0; 
                } 
                else if (gp.gameOptionRect != null && gp.gameOptionRect.contains(mouseX, mouseY)) {
                    gp.commandNum = 1; 
                }
                else if (gp.exitRect != null && gp.exitRect.contains(mouseX, mouseY)) {
                    gp.commandNum = 2; 
                } 
                else {
                    gp.commandNum = -1;
                }
            } 
            // >> LOGIC 2: IF SHOWING OPTIONS
            else {
                if (gp.musicRect != null && gp.musicRect.contains(mouseX, mouseY)) {
                    gp.commandNum = 3; // Hover Music
                }
                else if (gp.soundRect != null && gp.soundRect.contains(mouseX, mouseY)) {
                    gp.commandNum = 4; // Hover Sound
                }
                else {
                    gp.commandNum = -1;
                }
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    // Changed from mouseClicked to mousePressed for better responsiveness
    @Override
    public void mousePressed(MouseEvent e) {
        if (gp.gameState == gp.pauseState || gp.gameState == gp.winState || gp.gameState == gp.gameOverState) {
            
            // IMPORTANT: Do not allow interaction if level transition is happening
            if (gp.startBannerShown) return;

            int mx = e.getX();
            int my = e.getY();

            // >> CASE 1: MAIN MENU CLICKS
            if (!gp.showOptions) {
                if (gp.newGameRect != null && gp.newGameRect.contains(mx, my)) {
                    gp.resetGame();
                }
                else if (gp.gameOptionRect != null && gp.gameOptionRect.contains(mx, my)){
                    System.out.println("DEBUG: Open Options");
                    gp.showOptions = true; // Switch to Option View
                }
                else if (gp.exitRect != null && gp.exitRect.contains(mx, my)) {
                    System.exit(0);
                }
                // >> NEW FEATURE: CLICK OUTSIDE TO BACK (Only in Pause State)
                // If user clicks outside the menu area, resume the game
                else if (gp.gameState == gp.pauseState && gp.menuArea != null && !gp.menuArea.contains(mx, my)) {
                    gp.gameState = gp.playState;
                }
            }
            // >> CASE 2: OPTION MENU CLICKS
            else {
                // Click Music Toggle
                if (gp.musicRect != null && gp.musicRect.contains(mx, my)) {
                    gp.toggleMusic();
                }
                // Click Sound Toggle
                else if (gp.soundRect != null && gp.soundRect.contains(mx, my)) {
                    gp.toggleSound();
                }
                // Click outside buttons -> Go back to Main Menu
                else {
                    gp.showOptions = false; // Back to Main Menu
                }
            }
        }
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
    }
}