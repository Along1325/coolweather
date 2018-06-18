package com.example.lifeassistant.random;

import java.util.Random;

/**
 * Created by Administrator on 2017/6/8 0008.
 */

public class JokeColorRandom {
    private int[] textColor = {0xFFFF7F24,0xFFFF6EB4,0xFFFF4040	,0xFFFF1493,0xFFFF0000,0xFFEE5C42,0xFFEE0000,0xFFEE6363,0xFFDB7093,0xFFD15FEE,0xFFCD2990,0xFFCD0000,0xFFBCEE68,0xFFADFF2F,0xFF8EE5EE,0xFF7EC0EE,0xFF71C671,0xFF4169E1,0xFF1E90FF,0xFF00F5FF,0xFF008B45,0xFF0000FF,0xFF008B8B,0xFF228B22,0xFF8B008B,0xFF98FB98};
    private int selectColor;
    Random random = new Random();

    public JokeColorRandom(int selectColor) {
        int selectId = random.nextInt(textColor.length-1);
        this.selectColor = textColor[selectId];
    }

    public int getSelectColor() {
        return selectColor;
    }

    public void setSelectColor(int selectColor) {
        int selectId = random.nextInt(textColor.length-1);
        this.selectColor = textColor[selectId];
    }
}
