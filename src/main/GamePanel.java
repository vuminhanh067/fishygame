package main;

import java.awt.Dimension;
import java.awt.Color;
import javax.imageio.ImageIO;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import enity.Player;

public class GamePanel extends JPanel implements Runnable {
    // Screen settings
    final int originalTileSize = 16; 
    final int scale = 2;
    public final int tileSize = originalTileSize * scale; 
    public final int screenWidth = 960; 
    public final int screenHeight = 730; 
    public BufferedImage background;

    int FPS = 60;
    
    // KHỞI TẠO HANDLER
    KeyHandler keyH = new KeyHandler();
    public MouseHandler mouseH = new MouseHandler(); // Thêm MouseHandler
    
    // Truyền cả keyH và mouseH vào Player
    Player player = new Player(this, keyH, mouseH); 
    Thread gameThread; 

    public GamePanel() {
        try {
            background = ImageIO.read(getClass().getResourceAsStream("/res/background.png"));
        } catch (IOException e) {
            System.out.println("Background could not be loaded!");
            e.printStackTrace();
        }

        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.black);
        this.setDoubleBuffered(true);
        
        // ĐĂNG KÝ LISTENER
        this.addKeyListener(keyH);
        this.addMouseMotionListener(mouseH); // Lắng nghe chuyển động chuột
        this.setFocusable(true);
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000/FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();
            
            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = remainingTime/1000000;
                if (remainingTime > 0) {
                    Thread.sleep((long) remainingTime);
                }
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        player.update();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null)
            g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        
        Graphics2D g2 = (Graphics2D)g;
        player.draw(g2);
        g2.dispose();
    }
}