package entity;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import main.GamePanel;

public class Item extends Entity {
    GamePanel gp;
    public BufferedImage image;
    public boolean collected = false;
    private int speed = 2; // Tốc độ rơi của vật phẩm

    public Item(GamePanel gp, int x, int y) {
        this.gp = gp;
        this.x = x;
        this.y = y;
        this.width = 32;
        this.height = 32;
        this.solidArea = new Rectangle(0, 0, width, height);
        loadImage();
    }

    private void loadImage() {
        try {
            // Bạn thay bằng đường dẫn ảnh ngôi sao của bạn nhé
            image = ImageIO.read(getClass().getResourceAsStream("/res/item/star.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void update() {
        // Vật phẩm từ từ chìm xuống đáy biển
        y += speed;

        // Nếu rơi quá giới hạn thế giới thì biến mất
        if (y > gp.worldHeight) {
            collected = true; 
        }
    }

    public void draw(Graphics2D g2) {
        int screenX = x - gp.cameraX;
        int screenY = y - gp.cameraY;

        // Tối ưu: Chỉ vẽ khi nằm trong khung hình camera
        if (x + width > gp.cameraX && x < gp.cameraX + gp.screenWidth &&
            y + height > gp.cameraY && y < gp.cameraY + gp.screenHeight) {
            g2.drawImage(image, screenX, screenY, width, height, null);
        }
    }
}