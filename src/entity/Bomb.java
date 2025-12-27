package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;

public class Bomb extends Entity {
    GamePanel gp;
    public BufferedImage mineImg;
    public BufferedImage[] explosionFrames = new BufferedImage[5]; // Theo ảnh bạn gửi có 5 frames nổ
    public boolean exploded = false;
    public int explosionCounter = 0;
    public int explosionIndex = 0;

    public Bomb(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.width = 40; // Kích thước quả bom
        this.height = 40;
        this.solidArea = new Rectangle(0, 0, width, height);
        loadImages();
    }

    private void loadImages() {
        try {
            mineImg = ImageIO.read(getClass().getResourceAsStream("/res/bomb/mine.png"));
            for (int i = 0; i < 5; i++) {
                explosionFrames[i] = ImageIO.read(getClass().getResourceAsStream("/res/bomb/explosion" + (i + 1) + ".png"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void update() {
        if (exploded) {
            explosionCounter++;
            if (explosionCounter > 6) { // Tốc độ nổ
                explosionIndex++;
                explosionCounter = 0;
            }
        }
    }

    public void explode() {
        if (!exploded) {
            exploded = true;
            gp.playSE(4); // Âm thanh nổ
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = x - gp.cameraX;
        int screenY = y - gp.cameraY;

        if (!exploded) {
            g2.drawImage(mineImg, screenX, screenY, width, height, null);
        } else if (explosionIndex < 5) {
            // Vẽ hiệu ứng nổ to hơn quả bom một chút
            g2.drawImage(explosionFrames[explosionIndex], screenX - 20, screenY - 20, width + 40, height + 40, null);
        }
    }
}