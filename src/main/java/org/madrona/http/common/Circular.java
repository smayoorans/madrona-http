package org.madrona.http.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Circular {

    private List<Integer> delayPatternIntList = new ArrayList<>(Arrays.asList(1, 4, 20));

    private int currentIndex = 0;

    private static Circular instance;

    public static synchronized Circular getInstance() {
        if (instance == null) {
            instance = new Circular();
        }
        return instance;
    }

    public synchronized int nextDelay() {
        if (currentIndex < delayPatternIntList.size() - 1) {
            currentIndex++;
        } else {
            currentIndex = 0;
        }
        return delayPatternIntList.get(currentIndex);
    }
} 