package main;

import entity.AttackBubble;
import entity.Bomb;
import entity.Boss;
import entity.Enemy;
import entity.HeartItem;
import entity.Player;
import entity.RainBubble;
import entity.StunBubble;
import java.awt.Rectangle;
import java.util.ArrayList;

public class CollisionChecker {
    
    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkPlayerVsEnemies(Player player, ArrayList<Enemy> enemies) {
        if (player.invincible) return;
        int pPad = 10;
        player.solidArea.setBounds(player.x + pPad, player.y + pPad, player.width - 2*pPad, player.height - 2*pPad);

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            if (enemy == null) continue;


            int paddingX = (int)(enemy.width * 0.2); 
            int paddingY = (int)(enemy.height * 0.2); 

            enemy.solidArea.setBounds(
                enemy.x + paddingX, 
                enemy.y + paddingY, 
                enemy.width - 2 * paddingX, 
                enemy.height - 2 * paddingY
            );

            if (player.solidArea.intersects(enemy.solidArea)) {
                processCollision(player, enemy, i);
                gp.playSE(3);
            }
        }
    }

    public void processCollision(Player player, Enemy enemy, int index) {
        int playerSize = player.width * player.height;
        int enemySize = enemy.width * enemy.height;

        if (playerSize*1.5 >= enemySize/1.5) {
            // ĂN
            gp.aquarium.entities.remove(index);
            gp.score += enemy.scoreValue;
            player.eating();
        } else {
            // BỊ ĂN
            gp.lives--;
            gp.aquarium.entities.remove(index); 
            
            System.out.println("Ouch! Lives left: " + gp.lives);
            
            if (gp.lives > 0) {
                // >> TRƯỜNG HỢP 1: CÒN MẠNG -> PAUSE GAME + HIỆN SORRY
                gp.gameState = gp.respawnState; // Dừng game
                gp.banner.show("SORRY", 180); // Hiện chữ SORRY trong 2 giây (120 frames)
                
            } else {
                // >> TRƯỜNG HỢP 2: HẾT MẠNG -> GAME OVER
                gp.gameState = gp.gameOverState;
                gp.banner.show("YOU LOSE", -1); 
                System.out.println("GAME OVER");
            }
        }
    }
    public void checkPlayerVsBoss(Player player, Boss boss) {
    if (boss != null && player.solidArea.intersects(boss.solidArea)) {
        // Nếu Player đủ lớn để gây sát thương (tùy game của bạn)
        // Ở đây giả định va chạm là Player mất mạng
        
        if (!player.invincible) {
            player.invincible = true;
            player.startInvincibility();
            gp.lives -= 1 ;
           
            // 2. Hiệu ứng dừng hình (Hit-stop) 100ms để cảm nhận va chạm
            try { Thread.sleep(1000); } catch (Exception e) {}
            if (gp.lives > 0) {
                // >> TRƯỜNG HỢP 1: CÒN MẠNG -> PAUSE GAME + HIỆN SORRY
                gp.gameState = gp.respawnState; // Dừng game
                gp.banner.show("SORRY", 180); // Hiện chữ SORRY trong 2 giây (120 frames)
                
            }
            int safetyDistance = 400; // Khoảng cách văng ra

            if (player.x + (player.width/2) < boss.x + (boss.width/2)) {
                // Player đang ở nửa bên trái Boss -> Đẩy hẳn sang trái mép Boss
                player.x = boss.x - safetyDistance;
            } else {
                // Player đang ở nửa bên phải Boss -> Đẩy hẳn sang phải mép Boss
                player.x = boss.x + boss.width + safetyDistance;
            }

            // Tương tự cho trục Y để không bị dính khi chạm trên/dưới
            if (player.y < boss.y + (boss.height/2)) {
                player.y = boss.y - 350;
            } else {
                player.y = boss.y + boss.height + 350;
            }

            // 3. ĐẢM BẢO KHÔNG VĂNG KHỎI MÀN HÌNH (Boundary Check)
            if (player.x < 50) player.x = gp.screenWidth - 50;
            if (player.x > gp.screenWidth - 100) player.x = 50;
            if (player.y < 50) player.y = gp.screenHeight - 100;
            if (player.y > gp.screenHeight - 100) player.y = 100;
            
           
                // Đảm bảo sau khi văng không bị ra ngoài màn hình
            if (player.x < 50) player.x = 50;
            if (player.x > gp.screenWidth - 100) player.x = gp.screenWidth - 100;

            player.startInvincibility(); // Hàm giúp player nhấp nháy/bất tử tạm thời
            gp.playSE(3); // Âm thanh va chạm
            
            if (gp.lives > 0) {
                gp.gameState = gp.respawnState;
                //gp.banner.show("BE CAREFUL!", 120);
            }
        }
    }
    }
    public void checkBubbles(Player player, Boss boss, ArrayList<RainBubble> bubbles) {
        for (int i = 0; i < bubbles.size(); i++) {
            RainBubble rb = bubbles.get(i);
            
            // Chạm Boss -> Hồi máu
            if (boss != null && rb.solidArea.intersects(boss.solidArea)) {
                if (boss.currentHP < boss.maxHP) boss.currentHP += 0.1;
                rb.alive = false;
            }
            if (player != null && player.solidArea.intersects(rb.solidArea)) {
                if (player.playerBubble < player.maxPlayerBubble) {
                    player.collectBubble(); // Cộng vào kho
                    rb.alive = false; // Bong bóng biến mất
                    gp.playSE(3); // Phát âm thanh "pop" nếu có
                }
            }
            
        }
        // Xóa bóng biến mất
        bubbles.removeIf(b -> !b.alive || b.y > gp.screenHeight);
    }
    // check va cham boss ban bong ra
    public void checkAllBubbleCollisions(ArrayList<AttackBubble> pBubbles, ArrayList<StunBubble> bBubbles, ArrayList<RainBubble> rBubbles, Player player) {
    
    for (int i = 0; i < bBubbles.size(); i++) {
            StunBubble sb = bBubbles.get(i);
            if (!sb.alive) continue;

            // 1. Chạm Player
            if (sb.solidArea.intersects(player.solidArea)) {
                gp.lives -= 0.1;
                if (gp.lives < 0) gp.lives = 0;
                sb.alive = false;
                continue;
            }

            // 2. Triệt tiêu với bóng Player bắn ra
            for (AttackBubble pb : pBubbles) {
                if (pb.alive && sb.solidArea.intersects(pb.solidArea)) {
                    pb.alive = false;
                    sb.alive = false;
                    break;
                }
            }
            if (!sb.alive) continue;

            // 3. Triệt tiêu với bóng mưa hồi máu
            for (RainBubble rb : rBubbles) {
                if (rb.alive && sb.solidArea.intersects(rb.solidArea)) {
                    rb.alive = false;
                    sb.alive = false;
                    break;
                }
            }
        }
    }
    public void checkBubblesInteraction(Player player, Boss boss, ArrayList<AttackBubble> pBubbles, ArrayList<RainBubble> rBubbles) {
    
        // DUYỆT BÓNG MƯA TRƯỚC
        for (int i = 0; i < rBubbles.size(); i++) {
            RainBubble rb = rBubbles.get(i);
            if (!rb.alive) continue;

            // A. PLAYER NHẬN BÓNG MƯA (TĂNG THANH BÓNG)
            if (player.solidArea.intersects(rb.solidArea)) {
                if (player.playerBubble < player.maxPlayerBubble) {
                    player.playerBubble++;
                    rb.alive = false; 
                    continue; // Bóng này đã mất, bỏ qua các check dưới
                }
            }

            // B. BÓNG PLAYER TRIỆT TIÊU BÓNG MƯA
            for (int j = 0; j < pBubbles.size(); j++) {
                AttackBubble pb = pBubbles.get(j);
                if (!pb.alive) continue;

                if (pb.solidArea.intersects(rb.solidArea)) {
                    pb.alive = false; // Mất bóng tấn công
                    rb.alive = false; // Mất bóng mưa
                    // Tạo hiệu ứng nổ nhỏ (Particle) tại đây nếu muốn
                    break; 
                }
            }
        
            // C. BOSS NHẬN BÓNG MƯA (HỒI MÁU)
            if (rb.alive && boss.solidArea.intersects(rb.solidArea)) {
                rb.alive = false;
                boss.currentHP += 0.1;
            }
        }

        // DUYỆT BÓNG TẤN CÔNG CÒN LẠI ĐỂ GAY DAMAGE CHO BOSS
        for (AttackBubble pb : pBubbles) {
            if (pb.alive && boss.solidArea.intersects(pb.solidArea)) {
                pb.alive = false;
                boss.currentHP -= 0.25;
            }
        }
         pBubbles.removeIf(b -> !b.alive);
    }
    public boolean checkBombCollision(Bomb bomb, Player player, Boss boss, ArrayList<AttackBubble> playerBubbles) {
        
        
        // 1. Check với Player
        if (bomb.solidArea.intersects(player.solidArea)) return true;
        
        // 2. Check với Boss
        if (boss != null && boss.alive) {
           
            if (bomb.solidArea.intersects(boss.solidArea)){ 
                //System.out.println("bomm");
                return true;
            }
        }

        // 3. Check với Bong bóng của Player
        for (int i = 0; i < playerBubbles.size(); i++) {
            AttackBubble b = playerBubbles.get(i);
            if (bomb.solidArea.intersects(b.solidArea)) {
                playerBubbles.remove(i);
                return true;
            }
        }

        return false;  
    }
    public void checkItemCollision(Player player, Boss boss, ArrayList<HeartItem> items) {
        for (int i = 0; i < items.size(); i++) {
            HeartItem item = items.get(i);

                // Player ăn cá đỏ
            if (player.solidArea.intersects(item.solidArea)) {
                    gp.lives += 0.5; // Hồi 0.5 mạng
                    if (gp.lives > gp.maxHP) gp.lives= gp.maxHP;
                    
                    item.active = false;
                    //gp.playSE(X); // Âm thanh hồi máu
            }

                // Boss ăn cá đỏ (Boss cũng tranh với Player)
            if (boss != null && boss.alive && boss.solidArea.intersects(item.solidArea)) {
                    boss.currentHP = Math.min(boss.maxHP, boss.currentHP + 5);
                    item.active = false;
                    System.out.println("Boss đã cướp được cá đỏ! +5HP");
            }
                
                // Xóa item đã ăn hoặc hết hạn
                if (!item.active) {
                    items.remove(i);
                    i--;
                }
            }
        }
        
    }