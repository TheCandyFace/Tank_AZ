package org.example.tank_az;

import javafx.application.*;
import javafx.concurrent.*;

public class Timer implements Runnable {
    private int seconds;

    public Timer(int seconds) {
        this.seconds = seconds;
    }

    @Override
    public void run() {
        for (int i = seconds; i >= 0; i--) {
            System.out.println(i + " seconds remaining");
            try {
                // Pause the thread for 1000 milliseconds (1 second)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle the exception if the thread is interrupted
                e.printStackTrace();
            }
        }
    }
}
