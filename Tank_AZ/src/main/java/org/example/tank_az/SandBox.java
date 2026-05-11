package org.example.tank_az;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;
import org.example.tank_az.*;

import java.util.HashSet;
import java.util.Set;

public class SandBox extends Application {
    double anchorX;
    double anchorY;

    public static final int MAP_WIDTH = 848;
    public static final int MAP_HEIGHT = 636;
    public static final int CELL_SIZE = 53;
    public static final int GRID_ROWS = 12;
    public static final int GRID_COLS = 16;

    Set<KeyCode> keyPressed = new HashSet<>();
    Rectangle[][] wallsGridV = new Rectangle[GRID_ROWS][GRID_COLS];
    Rectangle[][] wallsGridH = new Rectangle[GRID_ROWS][GRID_COLS];
    TankPlayer player1 = new TankPlayer(Color.RED);
    TankPlayer player2 = new TankPlayer(Color.GREEN);
    Group walls = new Group();
    Group projectiles = new Group();
    Group gameGroup = new Group();
    Pane field = new Pane();

    @Override
    public void start(Stage stage) {
        // ---------- MAP CREATION ----------
        // Horizontal borders
        for (int c = 0; c < GRID_COLS; c++) {
            Rectangle top = new Rectangle(50, 3);
            top.setX(3 + c * CELL_SIZE);
            top.setY(0);

            Rectangle bottom = new Rectangle(50, 3);
            bottom.setX(3 + c * CELL_SIZE);
            bottom.setY(MAP_HEIGHT - 3);

            wallsGridH[0][c] = top;
            wallsGridH[GRID_ROWS - 1][c] = bottom;

            walls.getChildren().addAll(top, bottom);
        }

        // Vertical borders
        for (int r = 0; r < GRID_ROWS; r++) {
            Rectangle left = new Rectangle(3, 50);
            left.setX(0);
            left.setY(3 + r * CELL_SIZE);

            Rectangle right = new Rectangle(3, 50);
            right.setX(MAP_WIDTH - 3);
            right.setY(3 + r * CELL_SIZE);

            wallsGridV[r][0] = left;
            wallsGridV[r][GRID_COLS - 1] = right;

            walls.getChildren().addAll(left, right);
        }

        // Random vertical walls
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (Math.random() > 0.9) {
                    Rectangle w = new Rectangle(3, 50);
                    w.setX(c * CELL_SIZE);
                    w.setY(3 + r * CELL_SIZE);
                    wallsGridV[r][c] = w;
                    walls.getChildren().add(w);
                }
            }
        }

        // Random horizontal walls
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (Math.random() > 0.9) {
                    Rectangle w = new Rectangle(50, 3);
                    w.setX(3 + c * CELL_SIZE);
                    w.setY(r * CELL_SIZE);
                    wallsGridH[r][c] = w;
                    walls.getChildren().add(w);
                }
            }
        }

        // ---------- SCENE ----------
        gameGroup.getChildren().addAll(player1.getTank(),player2.getTank(),walls,projectiles);
        Group windowView = new Group(gameGroup);
        field.getChildren().addAll(windowView);

        player1.getTank().setLayoutX(25);
        player1.getTank().setLayoutY(25);
        player1.setAngle(180);

        player2.getTank().setLayoutX(75);
        player2.getTank().setLayoutY(75);

        Scene scene = new Scene(field, MAP_WIDTH, MAP_HEIGHT);
        stage.setScene(scene);
        stage.show();

        scene.setOnKeyPressed(e -> {
            keyPressed.add(e.getCode());
            if (e.getCode() == KeyCode.U) {
                gameGroup.setScaleX(2);
                gameGroup.setScaleY(2);
            } else if (e.getCode() == KeyCode.Y) {
                gameGroup.setScaleX(0.5);
                gameGroup.setScaleY(0.5);
            }
        });

        windowView.setOnMousePressed(event -> {
            // Record where the mouse is relative to the current position of the viewport
            anchorX = event.getSceneX() - windowView.getTranslateX();
            anchorY = event.getSceneY() - windowView.getTranslateY();
        });

        windowView.setOnMouseDragged(e -> {
            windowView.setTranslateX(e.getSceneX() - anchorX);
            windowView.setTranslateY(e.getSceneY() - anchorY);
        });
        scene.setOnKeyReleased(e -> keyPressed.remove(e.getCode()));

        // ---------- GAME LOOP ----------
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                handlePlayer1();
                handlePlayer2();
                bulletMovements();
            }
        }.start();
    }
    // ---------- PLAYER HANDLING ----------
    private void handlePlayer1() {
        double oldX = player1.getX();
        double oldY = player1.getY();
        double oldAngle = player1.getAngle();

        boolean moved = false;
        boolean rotated = false;

        // ---- ROTATION ----
        if (keyPressed.contains(KeyCode.LEFT)) {
            player1.rotateLeft();
            rotated = true;
        }
        if (keyPressed.contains(KeyCode.RIGHT)) {
            player1.rotateRight();
            rotated = true;
        }

        // If rotation causes overlap → undo rotation only
        if (rotated && checkCollisionTank(player1)) {
            player1.setAngle(oldAngle);
        }

        // ---- MOVEMENT ----
        if (keyPressed.contains(KeyCode.UP)) {
            player1.moveFoward();
            moved = true;
        }
        if (keyPressed.contains(KeyCode.DOWN)) {
            player1.moveBackward();
            moved = true;
        }
        if (keyPressed.contains(KeyCode.M)) {
            if (player1.getNumberOfBullets() < 5) {
                projectiles.getChildren().add(player1.shoot());
                keyPressed.remove(KeyCode.M);
            }
        }

        // If movement causes overlap → undo movement only
        if (moved && checkCollisionTank(player1)) {
            player1.setX(oldX);
            player1.setY(oldY);
        }
    }
    private void handlePlayer2() {
        double oldX = player2.getX();
        double oldY = player2.getY();
        double oldAngle = player2.getAngle();

        boolean moved = false;
        boolean rotated = false;

        // ---- ROTATION ----
        if (keyPressed.contains(KeyCode.A)) {
            player2.rotateLeft();
            rotated = true;
        }
        if (keyPressed.contains(KeyCode.D)) {
            player2.rotateRight();
            rotated = true;
        }

        // If rotation causes overlap → undo rotation only
        if (rotated && checkCollisionTank(player2)) {
            player2.setAngle(oldAngle);
        }

        // ---- MOVEMENT ----
        if (keyPressed.contains(KeyCode.W)) {
            player2.moveFoward();
            moved = true;
        }
        if (keyPressed.contains(KeyCode.S)) {
            player2.moveBackward();
            moved = true;
        }
        if (keyPressed.contains(KeyCode.Q)) {
            if (player2.getNumberOfBullets() < 5) {
                projectiles.getChildren().add(player2.shoot());
                keyPressed.remove(KeyCode.Q);
            }
        }

        // If movement causes overlap → undo movement only
        if (moved && checkCollisionTank(player2)) {
            player2.setX(oldX);
            player2.setY(oldY);
        }
    }
    // ---------- COLLISION USING Shape.intersect ----------
    private boolean checkCollisionTank(TankPlayer player) {

        Bounds tankBounds = player.getTank().getBoundsInParent();
        Rectangle body = (Rectangle)player.getTank().getChildren().get(0);

        int minCol = clamp((int)(tankBounds.getMinX() / CELL_SIZE), 0, GRID_COLS - 1);
        int maxCol = clamp((int)(tankBounds.getMaxX() / CELL_SIZE), 0, GRID_COLS - 1);
        int minRow = clamp((int)(tankBounds.getMinY() / CELL_SIZE), 0, GRID_ROWS - 1);
        int maxRow = clamp((int)(tankBounds.getMaxY() / CELL_SIZE), 0, GRID_ROWS - 1);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {

                Rectangle h = wallsGridH[r][c];
                if (h != null && Shape.intersect(body, h).getBoundsInLocal().getWidth() != -1)
                    return true;

                Rectangle v = wallsGridV[r][c];
                if (v != null && Shape.intersect(body, v).getBoundsInLocal().getWidth() != -1)
                    return true;
            }
        }
        return false;
    }
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
    public void bulletMovements() {
        for (Node bullet : projectiles.getChildren()) {
            double oldX = bullet.getLayoutX();
            TankPlayer.bulletMovementX((Circle) bullet);
            //Collision Checking
            Bounds projectileBounds = bullet.getBoundsInParent();
            int minCol = clamp((int) (projectileBounds.getMinX() / CELL_SIZE), 0, GRID_COLS - 1);
            int maxCol = clamp((int) (projectileBounds.getMaxX() / CELL_SIZE), 0, GRID_COLS - 1);
            int minRow = clamp((int) (projectileBounds.getMinY() / CELL_SIZE), 0, GRID_ROWS - 1);
            int maxRow = clamp((int) (projectileBounds.getMaxY() / CELL_SIZE), 0, GRID_ROWS - 1);
            for (int r = minRow; r <= maxRow; r++) {
                for (int c = minCol; c <= maxCol; c++) {
                    Rectangle h = wallsGridH[r][c];
                    if (h != null && Shape.intersect((Circle) bullet, h).getBoundsInLocal().getWidth() != -1) {
                        bullet.setLayoutX(oldX);
                        bullet.setRotate(Math.toDegrees(Math.PI - Math.toRadians(bullet.getRotate())));
                    }
                    Rectangle v = wallsGridV[r][c];
                    if (v != null && Shape.intersect((Circle) bullet, v).getBoundsInLocal().getWidth() != -1) {
                        bullet.setLayoutX(oldX);
                        bullet.setRotate(Math.toDegrees(Math.PI - Math.toRadians(bullet.getRotate())));
                    }
                }
            }

            double oldY = bullet.getLayoutY();
            TankPlayer.bulletMovementY((Circle) bullet);
            for (int r = minRow; r <= maxRow; r++) {
                for (int c = minCol; c <= maxCol; c++) {
                    Rectangle h = wallsGridH[r][c];
                    if (h != null && Shape.intersect((Circle) bullet, h).getBoundsInLocal().getWidth() != -1) {
                        bullet.setLayoutY(oldY);
                        bullet.setRotate(Math.toDegrees(-1 * Math.toRadians(bullet.getRotate())));
                    }
                    Rectangle v = wallsGridV[r][c];
                    if (v != null && Shape.intersect((Circle) bullet, v).getBoundsInLocal().getWidth() != -1) {
                        bullet.setLayoutY(oldY);
                        bullet.setRotate(Math.toDegrees(-1 * Math.toRadians(bullet.getRotate())));
                    }
                }
            }

            //Collision with a player
            if (Shape.intersect((Circle) bullet, (Rectangle) (player2.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                field.getChildren().remove(player2.getTank());
                player2.getTank().setLayoutX(1000);
                player2.getTank().setLayoutY(1000);
                projectiles.getChildren().remove(bullet);
            }
            ;
            if (Shape.intersect((Circle) bullet, (Rectangle) (player1.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                field.getChildren().remove(player1.getTank());
                player1.getTank().setLayoutX(1000);
                player1.getTank().setLayoutY(1000);
                projectiles.getChildren().remove(bullet);
            }
            ;
        }
    }
}