package main;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class BubbleParticle {
    double x, y, speedX, speedY;
    int size;
    int life; // Thời gian tồn tại của hạt (frame)
    Color color;

    public BubbleParticle(int x, int y) {
        Random rand = new Random();
        this.x = x;
        this.y = y;
        this.size = rand.nextInt(8) + 5; // Kích thước ngẫu nhiên
        this.life = rand.nextInt(50) + 50; // Sống từ 30-60 frame
        
        // Bắn ra theo hướng ngẫu nhiên (Pháo hoa tỏa tròn)
        double angle = rand.nextDouble() * 2 * Math.PI;
        double speed = rand.nextDouble() * 5 + 2;
        this.speedX = Math.cos(angle) * speed;
        this.speedY = Math.sin(angle) * speed;
        
        // Màu sắc ngẫu nhiên (Xanh, Hồng, Vàng...)
        // Để màu sáng hơn, ta giữ các giá trị RGB luôn lớn hơn 150
        int r = rand.nextInt(106) + 150; // 150 - 255
        int g = rand.nextInt(106) + 150; // 150 - 255
        int b = rand.nextInt(106) + 150; // 150 - 255
        this.color = new Color(r, b, g, 255);
    }

    public void update() {
        x += speedX;
        y += speedY;
        speedY += 0.1; // Trọng lực nhẹ làm bong bóng rơi xuống
        life--;
    }

    public void draw(Graphics2D g2) {
        g2.setColor(color);
        g2.fillOval((int)x, (int)y, size, size); // Vẽ vòng tròn bong bóng
    }
}