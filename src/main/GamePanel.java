package main;

import entity.Aquarium;
import entity.Banner;
import entity.Player;
import input.KeyHandler;
import input.MouseHandler;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GamePanel extends JPanel implements Runnable {
    // --- 1. SCREEN & WORLD SETTINGS ---
    public final int screenWidth = 780;
    public final int screenHeight = 640;
    public int worldWidth = 1280;
    public int worldHeight = 960;
    public final int originalTileSize = 16;
    public final int scale = 2;
    public final int tileSize = originalTileSize * scale; 

    // --- 2. CAMERA ---
    public int cameraX = 0;
    public int cameraY = 0;

    // --- 3. GAME STATE ---
    public int gameState; 
    public final int playState = 1;
    public final int gameOverState = 2;
    public final int winState = 3;
    public final int pauseState = 4;
    public final int respawnState = 5;
    
    // --- 4. DATA ---
    public Level currentLevel;
    public boolean startBannerShown = false; 
    public boolean isGameOverBannerActive = false; 
    public boolean isVictoryBannerActive = false;
    public int score = 0;
    public int lives = 3;
    
    // >> OPTION SETTINGS
    public boolean showOptions = false; 
    public boolean isMusicOn = true;
    public boolean isSoundOn = true;
    private int currentMusicId = 0;

    // --- 5. MENU ASSETS ---
    public BufferedImage menuBg, btnNewGame, btnNewGame2, btnGO, btnGO2, btnExit2,
                         btnExit, playerIcon, npc1, npc2, npc3, hudBackground,
                         btnMusicOn, btnMusicOff, btnSoundOn, btnSoundOff;

    public int menuX, menuY;
    
    // Rectangles for Mouse Interaction
    public Rectangle newGameRect, exitRect, gameOptionRect, musicRect, soundRect; 
    
    // Animation Control
    public int commandNum = -1; // 0:NewGame, 1:Options, 2:Exit, 3:Music, 4:Sound
    private int menuTick = 0;   

    // --- 6. SYSTEM ---
    int FPS = 60;
    public BufferedImage background, background2, background3, currentBackground;
    
    public MouseHandler mouseH;
    public KeyHandler keyH;
    Sound music = new Sound();
    Sound se = new Sound();
    
    public CollisionChecker cChecker = new CollisionChecker(this);
    public Banner banner; 
    public Banner guideBanner;
    Thread gameThread;
    
    private Cursor blankCursor;   
    private Cursor defaultCursor; 
    
    // --- 7. ENTITIES ---
    public Player player;
    public Aquarium aquarium;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        this.setFocusable(true);
        
        // Init Input Handlers
        mouseH = new MouseHandler(this);
        keyH = new KeyHandler(this);
        this.addMouseListener(mouseH);
        this.addMouseMotionListener(mouseH);
        this.addKeyListener(keyH);
        
        currentLevel = new Level(1); 
        gameState = playState;

        // Init Cursors
        defaultCursor = Cursor.getDefaultCursor();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        this.setCursor(blankCursor); 

        // Load & Setup
        loadResources();
        setupMenuPositions();
        
        // Init Entities
        player = new Player(this, mouseH);
        aquarium = new Aquarium(this);
        banner = new Banner(this);
        banner.show("LEVEL 1", 180);

        guideBanner = new Banner(this);
        guideBanner.prepareStatic("CLICK OUTSIDE TO BACK");
        // Start Music (if enabled)
        playMusic(0);
    }

    private void loadResources() {
        try {
            menuBg = ImageIO.read(getClass().getResourceAsStream("/res/screen/openAndPause.png"));
            btnNewGame = ImageIO.read(getClass().getResourceAsStream("/res/screen/newgame.png"));
            btnNewGame2 = ImageIO.read(getClass().getResourceAsStream("/res/screen/newgame2.png"));
            btnGO = ImageIO.read(getClass().getResourceAsStream("/res/screen/gameoption.png"));
            btnGO2 = ImageIO.read(getClass().getResourceAsStream("/res/screen/gameoption2.png"));
            btnExit = ImageIO.read(getClass().getResourceAsStream("/res/screen/exit.png"));
            btnExit2 = ImageIO.read(getClass().getResourceAsStream("/res/screen/exit2.png"));
            
            // >> OPTION IMAGES
            btnMusicOn = ImageIO.read(getClass().getResourceAsStream("/res/screen/musicOn.png"));
            btnMusicOff = ImageIO.read(getClass().getResourceAsStream("/res/screen/musicOff.png"));
            btnSoundOn = ImageIO.read(getClass().getResourceAsStream("/res/screen/soundOn.png"));
            btnSoundOff = ImageIO.read(getClass().getResourceAsStream("/res/screen/soundOff.png"));

            playerIcon = ImageIO.read(getClass().getResourceAsStream("/res/angelfish/angelfishidle1.png"));
            npc1 = ImageIO.read(getClass().getResourceAsStream("/res/minnow/minnowswim1.png"));
            npc2 = ImageIO.read(getClass().getResourceAsStream("/res/surgeonfish/surgeonfishswim6.png"));
            npc3 = ImageIO.read(getClass().getResourceAsStream("/res/lionfish/lionfishidle1.png"));
            hudBackground = ImageIO.read(getClass().getResourceAsStream("/res/screen/menuOcean3.jpg"));
            background = ImageIO.read(getClass().getResourceAsStream("/res/background.png"));
            background2 = ImageIO.read(getClass().getResourceAsStream("/res/background2.png"));
            background3 = ImageIO.read(getClass().getResourceAsStream("/res/backgroung3.png"));
            currentBackground = background;
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void setupMenuPositions() {
        // --- Menu Background ---
        int bgW = 558; 
        int bgH = 448;
        menuX = (screenWidth - bgW) / 2;
        menuY = (screenHeight - bgH) / 2;

        int btnSize = 132; 
        int gap = 40;     

        // --- 1. MAIN MENU LAYOUT (Horizontal) ---
        int totalContentWidth = (btnSize * 3) + (gap * 2);
        int commonY = menuY + (bgH - btnSize) / 2;
        int startX = menuX + (bgW - totalContentWidth) / 2;
    
        newGameRect = new Rectangle(startX, commonY, btnSize, btnSize);
        gameOptionRect = new Rectangle(startX + btnSize + gap, commonY, btnSize, btnSize);
        exitRect = new Rectangle(startX + (btnSize + gap) * 2, commonY, btnSize, btnSize);

        // --- 2. OPTIONS MENU LAYOUT (Horizontal) ---
        // Center 2 buttons (Music & Sound)
        int totalOptionWidth = (btnSize * 2) + gap; 
        int optionStartX = menuX + (bgW - totalOptionWidth) / 2;

        musicRect = new Rectangle(optionStartX, commonY, btnSize, btnSize);
        soundRect = new Rectangle(optionStartX + btnSize + gap, commonY, btnSize, btnSize);
    }

    public void resetGame() {
        score = 0;
        lives = 3;
        currentLevel = new Level(1);
        aquarium.reset(); 
        player.setDefaultValues();
        banner.show("LEVEL 1", 180);
        startBannerShown = false;
        gameState = playState;
        currentBackground = background;
        music.stopAll();
        playMusic(0);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;
        while (gameThread != null) {
            update();
            repaint();
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime / 1000000;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) { e.printStackTrace(); }
        }
    }

    public void update() {
        banner.update();

        if (gameState == playState) {
            cChecker.checkPlayerVsEnemies(player, aquarium.entities);
            if (this.getCursor() != blankCursor) this.setCursor(blankCursor);
            player.update();
            updateCamera();
            aquarium.update();

            // Check Win Level
            if(score >= currentLevel.winScore){
                if (currentLevel.levelNum < 3) { 
                    banner.show("COMPLETE!", 180);
                    gameState = pauseState;
                    startBannerShown = true; 
                } else {
                    nextLevel(); // Gọi nextLevel ở đây để kích hoạt winState
                }
            }
            // Check Game Over
            if(lives <= 0){
                gameState = gameOverState;
                isGameOverBannerActive = true; 
                banner.show("YOU LOSE!", 180);
            }
            
        }
        else if (gameState == pauseState) {
            if(startBannerShown){
                if (!banner.isActive()) {
                    startBannerShown = false; 
                    nextLevel(); 
                }
            } else{
                if (this.getCursor() != defaultCursor) this.setCursor(defaultCursor);
                menuTick++;
            }
        } 
        else if (gameState == respawnState) {
            if (!banner.isActive()) {
                gameState = playState;
                player.resetPosition();
                player.enableInvincibility();
            }
        }
        else if (gameState == gameOverState) {
            if (isGameOverBannerActive) {
                if (!banner.isActive()) isGameOverBannerActive = false; 
            }
            if (this.getCursor() != defaultCursor) this.setCursor(defaultCursor);
        }
        // >> LOGIC WIN STATE ĐÃ SỬA <<
        else if (gameState == winState) {
            if (isVictoryBannerActive) {
                // Khi banner Victory đang chạy -> Ẩn chuột
                if (this.getCursor() != blankCursor) this.setCursor(blankCursor);
                
                // Nếu banner chạy xong -> Tắt cờ để chuyển sang menu
                if (!banner.isActive()) {
                    isVictoryBannerActive = false;
                }
            } 
            else {
                // Khi banner đã tắt -> Hiện chuột để bấm Menu
                if (this.getCursor() != defaultCursor) {
                    this.setCursor(defaultCursor); 
                }
                menuTick++; 
            }
        }
    }

    public void updateCamera() {
        int marginX = 150; int marginY = 100;
        int playerScreenX = player.x - cameraX;
        int playerScreenY = player.y - cameraY;

        if (playerScreenX < marginX) cameraX = player.x - marginX;
        else if (playerScreenX + player.width > screenWidth - marginX) cameraX = (player.x + player.width) - (screenWidth - marginX);

        if (playerScreenY < marginY) cameraY = player.y - marginY;
        else if (playerScreenY + player.height > screenHeight - marginY) cameraY = (player.y + player.height) - (screenHeight - marginY);

        if (cameraX < 0) cameraX = 0;
        if (cameraY < 0) cameraY = 0;
        if (cameraX > worldWidth - screenWidth) cameraX = worldWidth - screenWidth;
        if (cameraY > worldHeight - screenHeight) cameraY = worldHeight - screenHeight;
    }
    public void nextLevel() {
        int nextLvl = currentLevel.levelNum + 1;
        if(nextLvl <= 3){
            // --- LOGIC CHUYỂN LEVEL BÌNH THƯỜNG ---
            currentLevel = new Level(nextLvl);
            if (nextLvl == 2) currentBackground = background2;
            if (nextLvl == 3) currentBackground = background3;

            player.setDefaultValues(); 
            player.resetPosition();
            cameraX = 0; cameraY = 0;
            startBannerShown = false;
            aquarium.reset();
            
            banner.show("LEVEL " + nextLvl, 180);
            gameState = playState;
            startBannerShown = false;
            System.out.println("Transition to Level " + currentLevel + " successful.");
        } else {
            // --- LOGIC CHIẾN THẮNG TOÀN GAME ---
            gameState = winState;
            isVictoryBannerActive = true; // Bật cờ banner chiến thắng
            banner.show("VICTORY!", 300); // Hiện chữ VICTORY trong 5 giây (60*5)
            
            music.stopAll();
            playMusic(4); 
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Draw Background
        if (currentBackground != null) {
            g2.drawImage(currentBackground, -cameraX, -cameraY, worldWidth, worldHeight, null);
        } else {
            g2.setColor(new Color(0, 100, 200)); 
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }
        // 2. Draw Entities
        aquarium.draw(g2);
        player.draw(g2);
        drawGameUI(g2);

        // 3. Draw Menus (Pause / Win / Lose)
        if (gameState == pauseState || gameState == winState || gameState == gameOverState)
        {
            // Reset độ trong suốt để vẽ menu rõ ràng
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            if (gameState == pauseState) {
                if (!startBannerShown) {
                    drawPauseScreen(g2);
                }
            }
            else if (gameState == winState) {
                // Nếu banner đã chạy xong thì mới vẽ Menu
                if (!isVictoryBannerActive) {
                    drawPauseScreen(g2);
                }
            }
            else if(gameState == gameOverState)
            {
                if (!isGameOverBannerActive) {
                    drawPauseScreen(g2);
                }
            }
        }
        // 4. Draw Banner
        banner.draw(g2);
        g2.dispose();
    }
    
    private void drawPauseScreen(Graphics2D g2) {
        // Dark Overlay
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        // Menu Background
        int originalWidth = menuBg.getWidth(null); 
        int originalHeight = menuBg.getHeight(null);
        int newMenuWidth = originalWidth + 200; 
        int newMenuHeight = originalHeight + 50;
        int bgDrawX = menuX - 50;  

        if (menuBg != null) {
            g2.drawImage(menuBg, menuX - 50, menuY, newMenuWidth, newMenuHeight, null);
        }

        // Wave Effect
        int waveOffset = (int)(Math.sin(menuTick * 0.1) * 5); 

        // >> LOGIC: CHECK IF SHOWING OPTIONS
        if (!showOptions) {
            // --- MAIN MENU BUTTONS ---
            
            // 1. New Game
            int y0 = newGameRect.y;
            if (commandNum == 0) y0 += waveOffset; 
            // Select Image (Hover vs Normal)
            BufferedImage imgNG = (commandNum == 0) ? btnNewGame2 : btnNewGame;
            g2.drawImage(imgNG, newGameRect.x, y0, newGameRect.width, newGameRect.height, null);

            // 2. Options
            int y1 = gameOptionRect.y;
            if (commandNum == 1) y1 += waveOffset;
            BufferedImage imgGO = (commandNum == 1) ? btnGO2 : btnGO;
            g2.drawImage(imgGO, gameOptionRect.x, y1, gameOptionRect.width, gameOptionRect.height, null);

            // 3. Exit
            int y2 = exitRect.y;
            if (commandNum == 2) y2 += waveOffset;
            BufferedImage imgEx = (commandNum == 2) ? btnExit2 : btnExit;
            g2.drawImage(imgEx, exitRect.x, y2, exitRect.width, exitRect.height, null);
            
        } else {
            // --- OPTIONS MENU BUTTONS ---
            
            // 1. Music Toggle
            int mY = musicRect.y;
            if (commandNum == 3) mY += waveOffset;
            // Choose Image based on STATE (On/Off)
            BufferedImage imgMusic = isMusicOn ? btnMusicOn : btnMusicOff;
            g2.drawImage(imgMusic, musicRect.x, mY, musicRect.width, musicRect.height, null);
            
            // 2. Sound Toggle
            int sY = soundRect.y;
            if (commandNum == 4) sY += waveOffset;
            BufferedImage imgSound = isSoundOn ? btnSoundOn : btnSoundOff;
            g2.drawImage(imgSound, soundRect.x, sY, soundRect.width, soundRect.height, null);
            
            // Instructions
            int bgCenterX = bgDrawX + (newMenuWidth / 2);
            
            // 2. Tính tọa độ Y: Nằm sát đáy MenuBg (Lùi lên 35px so với đáy)
            int textY = menuY + newMenuHeight - 35;
            
            // 3. Vẽ chữ (Chiều cao chữ khoảng 22px là vừa đẹp)
            guideBanner.drawStatic(g2, bgCenterX, textY, 25);
        }
    }
    
    // --- TOGGLE METHODS ---
    public void toggleMusic() {
        isMusicOn = !isMusicOn;
        if (isMusicOn) {
            // Bật lại bài nhạc đang chọn
            playMusic(currentMusicId); 
        } else {
            // Tắt toàn bộ nhạc
            music.stopAll(); 
        }
    }

    public void toggleSound() {
        isSoundOn = !isSoundOn;
        if (isSoundOn) playSE(3); 
    }

    private void drawTextWithOutline (Graphics2D g2, String text, int x, int y, Color outlineColor, Color mainColor) {
        int offset = 2; 
        g2.setColor(outlineColor);
        g2.drawString(text, x + offset, y + offset);
        g2.drawString(text, x - offset, y + offset);
        g2.drawString(text, x + offset, y - offset);
        g2.drawString(text, x - offset, y - offset);
        g2.drawString(text, x, y + offset);
        g2.drawString(text, x, y - offset);
        g2.drawString(text, x + offset, y);
        g2.drawString(text, x - offset, y);
        g2.setColor(mainColor);
        g2.drawString(text, x, y);
    }

    public void drawGameUI(Graphics2D g2) {
        final int HUD_HEIGHT = 120;
        final int HUD_WIDTH = screenWidth;
        final int HUD_Y = 0;
        final int TEXT_SIZE = 18;
        final int TEXT_Y_MAIN = HUD_Y + 55;
        final int TEXT_Y_SUB = HUD_Y + 95;
        final Color FONT_OUTLINE = new Color(0, 0, 0, 180);
        final Color FONT_MAIN = new Color(230, 255, 150, 255);;
        int npc2Score = 0;
        int npc3Score = 0;
        BufferedImage currentNpc2 = null, currentNpc3 = null, currentNpc1=null;

        if (currentLevel.levelNum == 1) {
            npc2Score = 300; npc3Score = 900;
            currentNpc1 = npc1;
            currentNpc2 = npc2; 
            currentNpc3 = npc3; 
        } else if (currentLevel.levelNum == 2) {
            npc2Score = 2400; npc3Score = 3600;
            currentNpc2 = currentLevel.monsterTypes.get(1).swimFrames[0]; 
            currentNpc3 = currentLevel.monsterTypes.get(2).swimFrames[0];
            currentNpc1 = currentLevel.monsterTypes.get(0).swimFrames[0];
        } else if (currentLevel.levelNum == 3){
            npc2Score = 5500; npc3Score = 7500;
            currentNpc2 = currentLevel.monsterTypes.get(1).swimFrames[0];
            currentNpc3 = currentLevel.monsterTypes.get(2).swimFrames[0];
            currentNpc1 = currentLevel.monsterTypes.get(0).swimFrames[0];
        }
        
        g2.drawImage(hudBackground, 0, HUD_Y, screenWidth, HUD_HEIGHT, null);
        g2.setColor(new Color(153, 204, 255, 150));
        g2.fillRect(0, HUD_Y + HUD_HEIGHT - 3, screenWidth, 3);
        g2.setFont(new Font("Cooper Std Black", Font.BOLD, TEXT_SIZE));
        
        int x = 20;
        drawTextWithOutline(g2, "MENU", x, TEXT_Y_MAIN, FONT_OUTLINE, FONT_MAIN);
        x += 80; 
        
        final int NPC_BASE_WIDTH = 50; 
        final int NPC_BASE_HEIGHT = 40;
        final int NPC_GAP = 15; 
        int currentNpcX = x + 40;
        
        final int size1_W = NPC_BASE_WIDTH;
        final int size1_H = NPC_BASE_HEIGHT;
        final int npcY1 = TEXT_Y_MAIN - size1_H + 10;
        if (npc1 != null) g2.drawImage(currentNpc1, currentNpcX, npcY1, size1_W, size1_H, null);
        currentNpcX = 135 + (int)((int)(HUD_WIDTH * 0.5) * ((double)npc2Score / currentLevel.winScore)) + 20;
        
        Composite originalComposite1 = g2.getComposite();
        final int size2_W = (int)(NPC_BASE_WIDTH * 1.4);
        final int size2_H = (int)(NPC_BASE_HEIGHT * 1.3);
        final int npcY2 = TEXT_Y_MAIN - size2_H + 15 ; 
        if (score < npc2Score) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (npc2 != null) g2.drawImage(currentNpc2, currentNpcX, npcY2, size2_W, size2_H, null);
        currentNpcX = 135 + (int)((int)(HUD_WIDTH * 0.5) * ((double)npc3Score / currentLevel.winScore))+ 20;
        g2.setComposite(originalComposite1);
        
        originalComposite1 = g2.getComposite();
        final int size3_W = (int)(NPC_BASE_WIDTH * 1.8);
        final int size3_H = (int)(NPC_BASE_HEIGHT * 1.6);
        final int npcY3 = TEXT_Y_MAIN - size3_H + 20; 
        if (score < npc3Score) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (npc3 != null) g2.drawImage(currentNpc3, currentNpcX, npcY3, size3_W, size3_H, null);
        currentNpcX += size3_W + NPC_GAP;
        g2.setComposite(originalComposite1);

        final int SCORE_TEXT_X = (int)(HUD_WIDTH * 0.7); 
        drawTextWithOutline(g2, "SCORE", SCORE_TEXT_X, TEXT_Y_MAIN, FONT_OUTLINE, FONT_MAIN);
        g2.setFont(new Font("Cooper Std Black", Font.BOLD, TEXT_SIZE + 10));
        int scoreBoxX = SCORE_TEXT_X + 120;
        drawTextWithOutline(g2, String.valueOf(score), scoreBoxX, TEXT_Y_MAIN, FONT_OUTLINE, Color.WHITE);
        g2.setFont(new Font("Cooper Std Black", Font.BOLD, TEXT_SIZE));
        int growthBarStartX = 20;
        int growthBarWidth =(int)(HUD_WIDTH * 0.5); 
        int barHeight = 15;
        int barY = TEXT_Y_SUB - barHeight;
        int growthBarX = growthBarStartX + 115;
        g2.setColor(Color.WHITE);
        drawTextWithOutline(g2, "GROWTH", growthBarStartX, TEXT_Y_SUB, FONT_OUTLINE, FONT_MAIN);
        
        double winScore = currentLevel.winScore; 
        double growthProgress = (double)score / winScore; 
        
        if (growthProgress > 1.0) growthProgress = 1.0; 
        g2.setColor(Color.BLACK);
        g2.fillRect(growthBarX, barY, growthBarWidth, barHeight);
        g2.setColor(Color.RED); 
        g2.fillRect(growthBarX, barY, (int)(growthBarWidth * growthProgress), barHeight);
        
        g2.setColor(Color.YELLOW);
        final int TRIANGLE_SIZE = 10;

        int npc2BarX = growthBarX + (int)(growthBarWidth * ((double)npc2Score / winScore));
        g2.fillPolygon(new int[]{npc2BarX, npc2BarX - TRIANGLE_SIZE, npc2BarX + TRIANGLE_SIZE}, 
                     new int[]{barY + barHeight - TRIANGLE_SIZE + 5, barY + barHeight  + 10, barY + barHeight  + 10}, 3);
        
        int npc3BarX = growthBarX + (int)(growthBarWidth * ((double)npc3Score / winScore));
        g2.fillPolygon(new int[]{npc3BarX, npc3BarX - TRIANGLE_SIZE, npc3BarX + TRIANGLE_SIZE}, 
                    new int[]{barY + barHeight - TRIANGLE_SIZE + 5, barY + barHeight + 10, barY + barHeight + 10}, 3); 
    
        int abilityStartX = SCORE_TEXT_X;
        g2.setColor(Color.WHITE);
        drawTextWithOutline(g2, "LIVE", abilityStartX, TEXT_Y_SUB, FONT_OUTLINE, FONT_MAIN);
        
        int livesIconStartX = abilityStartX + 70;
        final int LIVES_ICON_SIZE = 35; 
        final int LIVES_ICON_Y = barY + (barHeight - LIVES_ICON_SIZE) / 2;
        final int LIVES_ICON_GAP = 15;  
        g2.setColor(Color.WHITE);
        if (playerIcon != null) {
            for (int i = 0; i < 3; i++) {
                int currentX = livesIconStartX + i * (LIVES_ICON_SIZE + LIVES_ICON_GAP); 
                
                if (i < lives) {
                    g2.drawImage(playerIcon, currentX, LIVES_ICON_Y, LIVES_ICON_SIZE, LIVES_ICON_SIZE, null);
                } else {
                    Composite originalComposite = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2.drawImage(playerIcon, currentX, LIVES_ICON_Y, LIVES_ICON_SIZE, LIVES_ICON_SIZE, null);
                    g2.setComposite(originalComposite);
                }
            }
        } 
    }
    public void playMusic(int i) {
        if (isMusicOn) {
            music.stopAll(); // Ensure no other music track is playing
            music.loop(i);
            currentMusicId = i; // Store current track ID
        }
    }
    public void playSE(int i) {
        // Logic: Only play if Sound option is ON
        if (isSoundOn) {
            se.play(i);
        }
    }
}