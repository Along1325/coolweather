package com.example.lifeassistant.random;

import java.util.Random;

/**
 * Created by Administrator on 2017/6/8 0008.
 */

public class JokePageRandom {

    private int page;

    Random random = new Random();

    public JokePageRandom(int page) {
        this.page = random.nextInt(20000);
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = random.nextInt(20000);
    }
}
