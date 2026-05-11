package org.example.tank_az;

import javafx.scene.*;
import javafx.scene.shape.Rectangle;
import java.io.Serializable;

public class MapLayout {
    private Rectangle[][] verticalWalls;
    private Rectangle[][] horizontalWalls;

    // New: Logical arrays to store if a wall exists
    private boolean[][] vWallPresent;
    private boolean[][] hWallPresent;

    public MapLayout(int rows, int cols) {
        this.verticalWalls = new Rectangle[rows][cols + 1];
        this.horizontalWalls = new Rectangle[rows + 1][cols];
        this.vWallPresent = new boolean[rows][cols + 1];
        this.hWallPresent = new boolean[rows + 1][cols];
        initializeRects();
    }

    public MapLayout(Rectangle[][] verticalWalls, Rectangle[][] horizontalWalls) {
        this.verticalWalls = verticalWalls;
        this.horizontalWalls = horizontalWalls;

        // Initialize boolean arrays to match the rectangle array sizes
        this.vWallPresent = new boolean[verticalWalls.length][verticalWalls[0].length];
        this.hWallPresent = new boolean[horizontalWalls.length][horizontalWalls[0].length];

        // Sync booleans with the current state of the Rectangles
        syncBooleansWithRects();
    }

    private void syncBooleansWithRects() {
        // Check vertical rectangles
        for (int i = 0; i < verticalWalls.length; i++) {
            for (int j = 0; j < verticalWalls[i].length; j++) {
                // If the slot is NOT null, a wall exists there
                vWallPresent[i][j] = (verticalWalls[i][j] != null);
            }
        }

        // Check horizontal rectangles
        for (int i = 0; i < horizontalWalls.length; i++) {
            for (int j = 0; j < horizontalWalls[i].length; j++) {
                // If the slot is NOT null, a wall exists there
                hWallPresent[i][j] = (horizontalWalls[i][j] != null);
            }
        }
    }

    private void initializeRects() {
        // Initialize the arrays with Rectangle objects
        for (int i = 0; i < verticalWalls.length; i++) {
            for (int j = 0; j < verticalWalls[i].length; j++) {
                verticalWalls[i][j] = new Rectangle(3,53);
                verticalWalls[i][j].setX(j * 53);
                verticalWalls[i][j].setY(3 + i * 53);
            }
        }
        for (int i = 0; i < horizontalWalls.length; i++) {
            for (int j = 0; j < horizontalWalls[i].length; j++) {
                horizontalWalls[i][j] = new Rectangle(53,3);
                horizontalWalls[i][j].setX(53 + j * 53);
                horizontalWalls[i][j].setY(i * 53);
            }
        }
    }

    // Getters and Setters
    public Rectangle[][] getVerticalWalls() { return verticalWalls; }
    public void setVerticalWalls(Rectangle[][] walls) { this.verticalWalls = walls; }

    public Rectangle[][] getHorizontalWalls() { return horizontalWalls; }
    public void setHorizontalWalls(Rectangle[][] walls) { this.horizontalWalls = walls; }

    public boolean[][] getvWallPresent() {
        return vWallPresent;
    }

    public boolean[][] gethWallPresent() {
        return hWallPresent;
    }
}
