package main;

import entity.MonsterType;
import java.util.ArrayList;

public class Level {
    
    public int levelNum;
    public int winScore;
    public ArrayList<MonsterType> monsterTypes = new ArrayList<>();
    
    public Level(int levelNum) {
        this.levelNum = levelNum;
        setupLevel();
    }
    
    private void setupLevel() {
        if (levelNum == 1) {
            this.winScore = 2000; 
            
            // 1. MINNOW (40x30) - 30 điểm
            // Player Lv1 (50x40) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "minnow", "/res/minnow/", 2, 40, 30, 30, 
                15, 7, 0, 0 
            ));
            // 2. SURGEONFISH (85x80) - 60 điểm
            // Player Lv2 (150x126) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "surgeonfish", "/res/surgeonfish/", 3, 95, 75, 60, 
                14, 5, 5, 0 
            ));
            // 3. LIONFISH (110x104) - 90 điểm
            // Player Lv3 (175x140) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "lionfish", "/res/lionfish/", 4, 190, 160, 150, 
                14, 5, 6, 6 
            ));
        }
        else if(levelNum == 2) {
            monsterTypes.clear();
            this.winScore = 5000; 
            
            // 1. lionfish (95x90) - 120 điểm
            // Player Lv4 (200x160) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "lionfish", "/res/lionfish/", 4, 95, 90, 120, 
                13, 5, 7, 7 
            ));
            // 2. Anglerfish (200x180) - 200 điểm
            // Player Lv5 (250x200) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "Anglerfish", "/res/Anglerfish/", 5, 200, 180, 200, 
                12, 4, 8, 8 
            ));
            // 3. parrotfish (150x60) - 250 điểm
            // Player Lv5 (250x200) NHỎ HƠN -> BỊ ĂN
            monsterTypes.add(new MonsterType(
                "parrotfish", "/res/parrotfish/", 5, 150, 60, 250, 
                16, 6, 10, 10 
            ));
        }
        else if(levelNum == 3) {
            monsterTypes.clear();
            this.winScore = 8000; 
            
            // 1. parrotfish (220x200) - 300 điểm
            // Player Lv6 (300x240) LỚN HƠN -> Ăn được
            monsterTypes.add(new MonsterType(
                "parrotfish", "/res/parrotfish/", 6, 220, 200, 300, 
                10, 3, 12, 12 
            ));
            // 2. BARRACUDA (300x250) - 500 điểm
            // Player Lv6 (300x240) NHỎ HƠN -> BỊ ĂN
            monsterTypes.add(new MonsterType(
                "barracuda", "/res/barracuda/", 6, 300, 250, 500, 
                14, 4, 15, 15 
            ));
            // 3. SHARK (500x400) - 1000 điểm
            // Player Lv6 (300x240) NHỎ HƠN -> BỊ ĂN
            monsterTypes.add(new MonsterType(
                "shark", "/res/shark/", 6, 500, 400, 1000, 
                8, 2, 20, 20 
            ));
        }
    }
}