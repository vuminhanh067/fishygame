package main;

import java.awt.*;
import java.util.Random;
import java.awt.image.BufferedImage;
import entity.RainBubble;

public class EnvironmentManager {
    GamePanel gp;
    public int eventTimer = 0;
    public int eventDuration = 0;
    public String currentEvent = "normal"; // normal, warning, raining
    public BufferedImage bubbleImg; // Biến giữ ảnh bong bóng
    // Độ mờ của màn hình (cho hiệu ứng tối)
    float filterAlpha = 0f;

    public EnvironmentManager(GamePanel gp) {
        this.gp = gp;
        loadImages();
    }
    public void loadImages() {
        try {
            // Chèn đường dẫn đến file ảnh của bạn ở đây
            bubbleImg = javax.imageio.ImageIO.read(getClass().getResourceAsStream("/res/animation/bubble.png"));
        } catch (Exception e) {
            System.out.println("Lỗi load ảnh bong bóng: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public void update() {
        eventTimer++;

        // 1. CHU KỲ 30 GI Y (30s * 60fps = 1800 frames)
        if (currentEvent.equals("normal") && eventTimer >= 1500) {
            currentEvent = "warning";
            eventTimer = 0;
        }

        // 2. TRẠNG THÁI CẢNH BÁO (Giật lag + Tối màn hình)
        if (currentEvent.equals("warning")) {
            // Hiệu ứng tối dần
            if (filterAlpha < 0.6f) filterAlpha += 0.01f;
            
            // Hiệu ứng giật (Rung màn hình)
            gp.screenShake = true; 

            if (eventTimer > 120) { // Cảnh báo trong 2 giây
                currentEvent = "raining";
                eventTimer = 0;
                gp.screenShake = false;
            }
        }

        // 3. TRẠNG THÁI MƯA (Trời sáng lại + Bong bóng rơi)
        if (currentEvent.equals("raining")) {
            if (filterAlpha > 0f) filterAlpha -= 0.02f; // Sáng trở lại
            
            spawnRainBubbles(); // Gọi hàm tạo bong bóng

            if (eventTimer > 600) { // Mưa trong 10 giây
                currentEvent = "normal";
                eventTimer = 0;
            }
        }
    }

    private void spawnRainBubbles() {
        if (eventTimer % 3 == 0) {
            int randomX = new Random().nextInt(gp.screenWidth);
            // Tạo bóng mới với tọa độ X ngẫu nhiên và Y trên đỉnh màn hình (-30)
            RainBubble rb = new RainBubble(gp, randomX, -30);
            rb.speed = 2 + new Random().nextDouble() * 3; // Tạo tốc độ ngẫu nhiên từ 2.0 đến 5.0
            gp.rainBubbles.add(rb);
        }
       
    }

    public void draw(Graphics2D g2) {
        // Vẽ lớp phủ màu tối nếu đang ở trạng thái warning
        if (filterAlpha > 0) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, filterAlpha));
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }
}