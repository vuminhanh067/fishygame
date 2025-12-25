package main;

import entity.Aquarium;
import entity.Banner;
import entity.Player;
import input.KeyHandler;
import input.MouseHandler;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
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
    public int gameState ; // quyết định game đang ở màn hình nào
    public final int playState = 1;// đang chơi
    public final int gameOverState = 2;// thua
    public final int winState = 3;// vinner
    public final int pauseState = 4;// tạm dừng
    public final int respawnState = 5;
    
    // --- 4. DATA ---
    public Level currentLevel;
    public boolean startBannerShown = false; 
    public boolean isGameOverBannerActive = false; // Thêm biến này vào GamePanel
    public int score = 0;
    public int lives = 3;

    // --- 5. MENU ASSETS & LOGIC ---
    public BufferedImage menuBg, btnNewGame, btnNewGame2, btnExit2 , btnExit, playerIcon, npc1, npc2, npc3, hudBackground ;

    
    public int menuX, menuY;
    
    // Public để MouseHandler truy cập check click
    public Rectangle newGameRect, exitRect, gameOptionRect; 
    
    // Biến điều khiển Animation Menu
    public int commandNum = -1; // -1: None, 0: NewGame, 1: Exit, 2: GameOption
    private int menuTick = 0;   // Đếm thời gian để tạo sóng

    // --- 6. SYSTEM ---
    int FPS = 60;
    public BufferedImage background;
    public BufferedImage background2;
    public BufferedImage background3;
    public BufferedImage currentBackground;
    
    // Input Handlers
    public MouseHandler mouseH;
    public KeyHandler keyH;
    Sound sound = new Sound();
    
    public CollisionChecker cChecker = new CollisionChecker(this);
    public Banner banner; 
    Thread gameThread;
    
    // Cursor Management
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
        
        currentLevel = new Level(1); // bắt đầu ở màn 1
        gameState = playState;

        // Init Cursors
        defaultCursor = Cursor.getDefaultCursor();
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        this.setCursor(blankCursor); // Mặc định ẩn

        // Load & Setup
        loadResources();
        setupMenuPositions();
        
        // Init Entities
        player = new Player(this, mouseH);
        aquarium = new Aquarium(this);
        
        banner = new Banner(this);
        banner.show("LEVEL 1", 180);// hiển thị dòng chữ level 1 trong vòng 180 frame (3s)

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        
        // Rất quan trọng: Phải gọi hàm này để các Rect không bị null
        setupMenuPositions();
    }

    private void loadResources() {
        try {
            menuBg = ImageIO.read(getClass().getResourceAsStream("/res/screen/openAndPause.png"));
            btnNewGame = ImageIO.read(getClass().getResourceAsStream("/res/screen/newgame.png"));
            btnNewGame2 = ImageIO.read(getClass().getResourceAsStream("/res/screen/newgame2.png"));
            btnExit = ImageIO.read(getClass().getResourceAsStream("/res/screen/exit.png"));
            btnExit2 = ImageIO.read(getClass().getResourceAsStream("/res/screen/exit2.png"));
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
        int bgW = 558; int bgH = 448;
        menuX = (screenWidth - bgW) / 2;
        menuY = (screenHeight - bgH) / 2;
        
        int centerY = menuY + bgH / 2;
        int ngW = 132; int ngH = 132;
        int exW = 89;  int exH = 89;
        int gap = 60;
        
        int totalBtnWidth = ngW + gap + exW;
        int startX = menuX + (bgW - totalBtnWidth) / 2;
        
        newGameRect = new Rectangle(startX, centerY - ngH/2 + 100, ngW, ngH);
        exitRect = new Rectangle(startX + ngW + gap, centerY - exH/2 + 100, exW, exH);
        gameOptionRect = new Rectangle(startX + ngW + gap, centerY - exH/2 + 200, exW, exH);
        playMusic(0);
    }
    // khôi phục toàn bộ trò chơi về trạng thái ban 
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
        // --- LOGIC CURSOR & MENU ANIMATION ---
       banner.update();

        if (gameState == playState) {
            cChecker.checkPlayerVsEnemies(player, aquarium.entities);
            if (this.getCursor() != blankCursor) this.setCursor(blankCursor);
            player.update();
            updateCamera();
            aquarium.update();

            // Kiểm tra chuyển màn
            if(score >= currentLevel.winScore){
                if (currentLevel.levelNum < 3) { // Nếu chưa phải level cuối
                    banner.show("LEVEL COMPLETE", 180);
                    gameState = pauseState;
                    startBannerShown = true; 
                    // startBannerShown = true sẽ kích hoạt nextLevel() trong đoạn logic pauseState bên dưới
                } else {
                    // Đã thắng Level 3
                    gameState = winState;
                    stopMusic();
                }
            }
            
            if(lives <= 0){
                gameState = gameOverState;
                isGameOverBannerActive = true; 
                banner.show("YOU LOSE", 180);
                stopMusic();
            }
            
        }
         if (gameState == pauseState) {
            if(startBannerShown){
                
                if (!banner.isActive()) {
                    startBannerShown = false; // Tắt đánh dấu
                    nextLevel(); // Chuyển sang Level 2
                }
            } else{
                // ĐÂY LÀ PAUSE MENU BÌNH THƯỜNG (Nhấn ESC hoặc mất mạng)
                if (this.getCursor() != defaultCursor) this.setCursor(defaultCursor);
                menuTick++;

                // Thoát Pause nếu là banner thông báo mất mạng (Sorry/Respawn)
                if (!banner.isActive() && lives > 0) gameState = playState;

            }
           
        } // 4. LOGIC RESPAWN/GAMEOVER (Giữ nguyên của bạn)
        else if (gameState == respawnState) {
            if (!banner.isActive()) {
                gameState = playState;
                player.resetPosition();
                player.enableInvincibility();
            }
        }
         

       
        else if (gameState == gameOverState) {
          
            if (isGameOverBannerActive) {
                if (!banner.isActive()) {
                    isGameOverBannerActive = false; // Sau khi xong chữ YOU LOSE, flag này tắt để hiện Menu
                }
            }
            if (this.getCursor() != defaultCursor) {
                this.setCursor(defaultCursor);
            }
        }
    }
    // Hàm bổ trợ để code update nhìn sạch hơn
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
        // Tăng số Level hiện tại lên
        int nextLvl = currentLevel.levelNum + 1;
        if(nextLvl <= 3){
            currentLevel = new Level(nextLvl);
            // Tự động đổi background theo level
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
             System.out.println("Transition to Level" + currentLevel+ " successful. Score retained: " + score);
        } else {
            gameState = winState;
            stopMusic();
        }
        
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Draw Background
        if (currentBackground != null) {
            int sx1 = cameraX; int sy1 = cameraY;
            int sx2 = cameraX + screenWidth; int sy2 = cameraY + screenHeight;
            if (sx2 > worldWidth) sx2 = worldWidth;
            if (sy2 > worldHeight) sy2 = worldHeight;
            //g2.drawImage(currentBackground, 0, 0, screenWidth, screenHeight, sx1, sy1, sx2, sy2, null);
            g2.drawImage(currentBackground, -cameraX, -cameraY, worldWidth, worldHeight, null);
        } else {
            g2.setColor(new Color(0, 100, 200)); 
            g2.fillRect(0, 0, screenWidth, screenHeight);
        }

        // 2. Draw Entities
        aquarium.draw(g2);
        player.draw(g2);
        drawGameUI(g2);

        // 3. Draw Overlay (Win/Lose)
        // 4. Draw Pause Menu
        if (gameState == pauseState || gameState == winState || gameState == gameOverState)
        {
            //stopMusic();
            // Cố định độ trong suốt về 1.0 (Rõ nét 100%) trước khi vẽ Menu
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            //g2.setFont(new Font("Arial", Font.BOLD, 30));
            //g2.setColor(Color.white);
            if (gameState == pauseState) {
                // CHỈ vẽ Menu Option nếu KHÔNG PHẢI đang trong giai đoạn chuyển màn
                if (!startBannerShown) {
                    drawPauseScreen(g2);
                }
            }
            if (gameState == winState) {
                String text = "You Won the Game!";
                int length = (int)g2.getFontMetrics().getStringBounds(text, g2).getWidth();
                g2.drawString(text, screenWidth / 2 - length / 2, screenHeight / 2);
            }
            else if(gameState == gameOverState)
            {
                
                if (isGameOverBannerActive) {
                    // Đang chạy hiệu ứng chữ cái YOU LOSE
                    if (!banner.isActive()) {
                        // Khi banner ảnh chữ cái kết thúc, mới cho phép hiện Menu thật
                        isGameOverBannerActive = false; 
                    }
                } else {
                        // Khi banner ảnh đã biến mất, lúc này mới vẽ bảng Menu (New Game / Exit)
                    drawPauseScreen(g2);
                }
            }
           //gameState = pauseState;
        }

        // 5. Draw Banner
        banner.draw(g2);
        g2.dispose();
    }
    
    private void drawPauseScreen(Graphics2D g2) {
        // Lớp nền tối
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        if (menuBg != null) g2.drawImage(menuBg, menuX, menuY, null);
        // Lấy kích thước ban đầu (Giả định bạn có biến này hoặc lấy từ ảnh menuBg)
        int originalWidth = menuBg.getWidth(null); 
        int originalHeight = menuBg.getHeight(null);

        // Định nghĩa kích thước mới
        int newMenuWidth = originalWidth + 200; 
        int newMenuHeight = originalHeight + 50;  

        // Vẽ khung Menu với kích thước MỚI
        if (menuBg != null) {
            g2.drawImage(menuBg, 
                        menuX - 50 ,      // Vị trí X
                        menuY,            // Vị trí Y
                        newMenuWidth,     // Chiều rộng mới
                        newMenuHeight,    // Chiều cao mới
                        null);
        }
        // --- TÍNH TOÁN HIỆU ỨNG SÓNG (BOBBING EFFECT) ---
        // Biên độ 5px, tốc độ 0.1
        int waveOffset = (int)(Math.sin(menuTick * 0.1) * 5); 

    //     Vẽ nút New Game
        int y = newGameRect.y;
        
        // KIỂM TRA HOVER (commandNum == 0)
        if (commandNum == 0) {
            y += waveOffset;
            // VẼ NÚT SÁNG (newgame2) KHI HOVER
            g2.drawImage(btnNewGame2, newGameRect.x, y, newGameRect.width, newGameRect.height, null);
        } else {
            // VẼ NÚT THƯỜNG KHI KHÔNG HOVER
            g2.drawImage(btnNewGame, newGameRect.x, y, newGameRect.width, newGameRect.height, null);
        }
            
        // Vẽ nút Exit
        if (btnExit != null && btnExit2 != null) {
            int y1 = exitRect.y;
            
            if (commandNum == 1) {
                y1 += waveOffset;
                // VẼ NÚT SÁNG (newgame2) KHI HOVER
                g2.drawImage(btnExit2, exitRect.x, y1, exitRect.width, exitRect.height, null);
            
            } else {
                // VẼ NÚT THƯỜNG KHI KHÔNG HOVER
                g2.drawImage(btnExit, exitRect.x, y1, exitRect.width, exitRect.height, null);
    
            }
        }
    }
    
    // Phương thức hỗ trợ để vẽ chữ với viền
    private void drawTextWithOutline (Graphics2D g2, String text, int x, int y, Color outlineColor, Color mainColor) {
        
        int offset = 2; 

        // 1. Vẽ Viền (Màu Đen)
        g2.setColor(outlineColor);
        g2.drawString(text, x + offset, y + offset);
        g2.drawString(text, x - offset, y + offset);
        g2.drawString(text, x + offset, y - offset);
        g2.drawString(text, x - offset, y - offset);
        g2.drawString(text, x, y + offset);
        g2.drawString(text, x, y - offset);
        g2.drawString(text, x + offset, y);
        g2.drawString(text, x - offset, y);

        // 2. Vẽ Chữ Chính (Màu Vàng Chanh)
        g2.setColor(mainColor);
        g2.drawString(text, x, y);
    }

    public void drawGameUI(Graphics2D g2) {
        
        // --- 1. THIẾT LẬP THÔNG SỐ VÀ VẼ NỀN THANH HUD ---
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
            npc2Score = 300;
            npc3Score = 900;
            currentNpc1 = npc1;
            currentNpc2 = npc2; // Cá Surgeonfish (Level 1)
            currentNpc3 = npc3; // Cá Lionfish (Level 1)
        } else if (currentLevel.levelNum == 2) {
            // Mốc điểm cho Level 2 (Ví dụ: cần 3000 và 4500 để tiến hóa)
            npc2Score = 2400; 
            npc3Score = 3600;
            currentNpc2 = currentLevel.monsterTypes.get(1).swimFrames[0]; // Cá Angler
            currentNpc3 = currentLevel.monsterTypes.get(2).swimFrames[0];
            currentNpc1 = currentLevel.monsterTypes.get(0).swimFrames[0];
        } else if (currentLevel.levelNum == 3){
            // Mốc điểm cho Level 2 (Ví dụ: cần 3000 và 4500 để tiến hóa)
            npc2Score = 5500; 
            npc3Score = 7500;
            currentNpc2 = currentLevel.monsterTypes.get(1).swimFrames[0]; // Cá Angler
            currentNpc3 = currentLevel.monsterTypes.get(2).swimFrames[0];
            currentNpc1 = currentLevel.monsterTypes.get(0).swimFrames[0];
        }
        // VẼ NỀN THANH HUD (GIẢ ĐỊNH)
        g2.drawImage(hudBackground, 0, HUD_Y, screenWidth, HUD_HEIGHT, null);
        
        // Đường viền (tùy chọn)
        g2.setColor(new Color(153, 204, 255, 150));
        g2.fillRect(0, HUD_Y + HUD_HEIGHT - 3, screenWidth, 3);
        g2.setFont(new Font("Cooper Std Black", Font.BOLD, TEXT_SIZE));
        

        // --- 2. VẼ CÁC THÀNH PHẦN CHÍNH (TEXT VÀ VỊ TRÍ) ---
        int x = 20;

        // A. MENU TEXT (BÊN TRÁI)
        drawTextWithOutline(g2, "MENU", x, TEXT_Y_MAIN, FONT_OUTLINE, FONT_MAIN);
        x += 80; 
        
        // B. NPC ICONS (npc1, npc2, npc3)
        final int NPC_BASE_WIDTH = 50;  // Chiều rộng cơ sở (ngang)
        final int NPC_BASE_HEIGHT = 40; // Chiều cao cơ sở (dọc)
        final int NPC_GAP = 15; 
        int currentNpcX = x + 40;
        
        // 1. NPC1 (Cá nhỏ nhất)
        final int size1_W = NPC_BASE_WIDTH;
        final int size1_H = NPC_BASE_HEIGHT;
        final int npcY1 = TEXT_Y_MAIN - size1_H + 10;
         // npcY1 chỉ tọa độ y bên trái của cá npc1
        if (npc1 != null) g2.drawImage(currentNpc1, currentNpcX, npcY1, size1_W, size1_H, null);
        currentNpcX = 135 + (int)((int)(HUD_WIDTH * 0.5) * ((double)npc2Score / currentLevel.winScore)) + 20;
        
        // 2. NPC2 
        Composite originalComposite1 = g2.getComposite();
        final int size2_W = (int)(NPC_BASE_WIDTH * 1.4);
        final int size2_H = (int)(NPC_BASE_HEIGHT * 1.3);
        final int npcY2 = TEXT_Y_MAIN - size2_H + 15 ; 
        if (score < npc2Score) {
            // Nếu điểm chưa đạt mốc 300: VẼ MỜ (30% Opacity)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        } else {
            // Nếu điểm đã đạt: VẼ SÁNG (100% Opacity)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        // Vẽ npc2
        if (npc2 != null) g2.drawImage(currentNpc2, currentNpcX, npcY2, size2_W, size2_H, null);
        currentNpcX = 135 + (int)((int)(HUD_WIDTH * 0.5) * ((double)npc3Score / currentLevel.winScore))+ 20;
        g2.setComposite(originalComposite1);// reset composite
        
        // 3. NPC3 
        originalComposite1 = g2.getComposite();
        final int size3_W = (int)(NPC_BASE_WIDTH * 1.8);
        final int size3_H = (int)(NPC_BASE_HEIGHT * 1.6);
        final int npcY3 = TEXT_Y_MAIN - size3_H + 20; 
        if (score < npc3Score) {
        // Nếu điểm chưa đạt mốc 900: VẼ MỜ (30% Opacity)
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        } else {
            // Nếu điểm đã đạt: VẼ SÁNG (100% Opacity)
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }
        if (npc3 != null) g2.drawImage(currentNpc3, currentNpcX, npcY3, size3_W, size3_H, null);
        currentNpcX += size3_W + NPC_GAP;
        g2.setComposite(originalComposite1);// reset
        // C. SCORE
        final int SCORE_TEXT_X = (int)(HUD_WIDTH * 0.7); // Vị trí X cố định cho chữ SCORE
        drawTextWithOutline(g2, "SCORE", SCORE_TEXT_X, TEXT_Y_MAIN, FONT_OUTLINE, FONT_MAIN);
        // VẼ KHUNG ĐIỂM (Dịch sang phải 80px từ chữ SCORE)
        g2.setFont(new Font("Cooper Std Black", Font.BOLD, TEXT_SIZE + 10));
        int scoreBoxX = SCORE_TEXT_X + 120;
        drawTextWithOutline(g2, String.valueOf(score), scoreBoxX, TEXT_Y_MAIN, FONT_OUTLINE, Color.WHITE);
        g2.setFont(new Font("Cooper Std Black", Font.BOLD, TEXT_SIZE));
        // E. GROWTH BAR (Thanh tiến hóa)
        int growthBarStartX = 20;
        int growthBarWidth =(int)(HUD_WIDTH * 0.5); // Chiếm khoảng nửa màn hình
        int barHeight = 15;
        int barY = TEXT_Y_SUB - barHeight;
        int growthBarX = growthBarStartX + 115;
        g2.setColor(Color.WHITE);
        drawTextWithOutline(g2, "GROWTH", growthBarStartX, TEXT_Y_SUB, FONT_OUTLINE, FONT_MAIN);
        
        // 3. VẼ PHẦN ĐÃ ĐẦY
        // Tính toán tiến độ hiện tại so với điểm thắng (2000d)
        double winScore = currentLevel.winScore; 
        double growthProgress = (double)score / winScore; 
        
        // Giới hạn tiến độ không vượt quá 100%
        if (growthProgress > 1.0) growthProgress = 1.0; 
        g2.setColor(Color.BLACK);
        g2.fillRect(growthBarX, barY, growthBarWidth, barHeight);
        g2.setColor(Color.RED); 
        g2.fillRect(growthBarX, barY, (int)(growthBarWidth * growthProgress), barHeight);
        
        
        // 3.3. VẼ CÁC NPC MỐC TIẾN HÓA (Vị trí điểm: 300d và 900d)
        g2.setColor(Color.YELLOW);
        final int TRIANGLE_SIZE = 10;


        // MỐC NPC2 (300d)
        
        int npc2BarX = growthBarX + (int)(growthBarWidth * ((double)npc2Score / winScore));
        g2.fillPolygon(new int[]{npc2BarX, npc2BarX - TRIANGLE_SIZE, npc2BarX + TRIANGLE_SIZE}, 
                    new int[]{barY + barHeight - TRIANGLE_SIZE + 5, barY + barHeight  + 10, barY + barHeight  + 10}, 3);
        
        
        // MỐC NPC3 (900d)
        int npc3BarX = growthBarX + (int)(growthBarWidth * ((double)npc3Score / winScore));
        g2.fillPolygon(new int[]{npc3BarX, npc3BarX - TRIANGLE_SIZE, npc3BarX + TRIANGLE_SIZE}, 
                   new int[]{barY + barHeight - TRIANGLE_SIZE + 5, barY + barHeight + 10, barY + barHeight + 10}, 3); // Vẽ NPC Icon tại các mốc 
    
        // F. ABILITY BAR (Khả năng đặc biệt)
        int abilityStartX = SCORE_TEXT_X;
        g2.setColor(Color.WHITE);
        drawTextWithOutline(g2, "LIVE", abilityStartX, TEXT_Y_SUB, FONT_OUTLINE, FONT_MAIN);
        
        // G. LIVES ICONS (Vị trí mạng sống)
        int livesIconStartX = abilityStartX + 70;
        final int LIVES_ICON_SIZE = 35; // Icon nhỏ hơn để vừa với vị trí này
        final int LIVES_ICON_Y = barY + (barHeight - LIVES_ICON_SIZE) / 2;
        // Tăng khoảng cách giữa các icon
        final int LIVES_ICON_GAP = 15;  
        g2.setColor(Color.WHITE);
        // Vòng lặp tối đa 3 lần (mạng tối đa)
        if (playerIcon != null) {
            for (int i = 0; i < 3; i++) {
                int currentX = livesIconStartX + i * (LIVES_ICON_SIZE + LIVES_ICON_GAP); // 5px gap
                
                if (i < lives) {
                    g2.drawImage(playerIcon, currentX, LIVES_ICON_Y, LIVES_ICON_SIZE, LIVES_ICON_SIZE, null);
                } else {
                    // Mạng mất: VẼ icon mờ (đã mất)
                    Composite originalComposite = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2.drawImage(playerIcon, currentX, LIVES_ICON_Y, LIVES_ICON_SIZE, LIVES_ICON_SIZE, null);
                    g2.setComposite(originalComposite);
                }
            }
        } 
    }

    public void playMusic(int i) {
        sound.setFile(i);
        sound.play();
        sound.loop();
    }
    public void stopMusic() {
        sound.stop();
    }
    public void playSE(int i) {
        sound.setFile(i);
        sound.play();
    }
}