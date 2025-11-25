package main;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class MouseHandler implements MouseMotionListener {
    
    public int mouseX, mouseY;

    @Override
    public void mouseDragged(MouseEvent e) {
        // Có thể cập nhật tọa độ khi kéo chuột nếu cần
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // Cập nhật tọa độ khi di chuyển chuột
        mouseX = e.getX();
        mouseY = e.getY();
    }
}