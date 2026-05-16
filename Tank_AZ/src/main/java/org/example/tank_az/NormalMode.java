package org.example.tank_az;

import com.adonax.audiocue.*;
import javafx.animation.*;
import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.*;
import javafx.scene.text.Font;
import javafx.stage.*;
import com.adonax.audiocue.AudioCue;
import javafx.util.Duration;
import org.w3c.dom.css.*;

import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.time.*;
import java.util.*;

public class NormalMode extends Application {
    Stage stage;
    Rectangle[][] wallsGridV;
    Rectangle[][] wallsGridH;
    Rectangle[][] boxesGrid;
    ArrayList<MapLayout> layouts = new ArrayList<>();
    File fileForLayouts = new File("C:\\Users\\peter\\IdeaProjects\\Tank_AZ\\src\\main\\resources\\MapData\\10x12.bin");

    //Group map;
    double MAP_WIDTH;
    double MAP_HEIGHT;
    double CELL_SIZE;
    int GRID_ROWS;
    int GRID_COLS;
    double WALL_DENSITY;
    double WALL_WIDTH;

    TankPlayer player1 = new TankPlayer(Color.RED);
    TankPlayer player2 = new TankPlayer(Color.GREEN);
    Group projectiles = new Group();
    Group nukeShrapnels = new Group();
    ArrayList<Double> shrapnelsSpeed = new ArrayList<>();
    private Map<Node, Set<Rectangle>> activeCollisionsShrapnels = new HashMap<>();
    ArrayList<Group> mines = new ArrayList<>();
    Set<KeyCode> keyPressed = new HashSet<>();

    BorderPane gUILayout = new BorderPane();
    Group gameZone;

    Pane gameContainer;
    Group pointZone;
    HBox containerPoints;
    Label labelNumberPointPlayer1;
    Label labelNumberPointPlayer2;

    AudioCue deathSound;
    AudioCue bulletBounceSound;
    AudioCue pickUpSound;
    AudioCue lazerShotSound;
    AudioCue flashBangSound;
    AudioCue reverseDirectionSound;
    AudioCue setMineSound;
    AudioCue explodeMineSound;
    AudioCue fanSound;
    AudioCue shieldActivation;
    AudioCue shieldBreak;

    private int frameCountBox = 0;
    private int frameCountEnd = 0;
    private ArrayList<Circle> nukes = new ArrayList();
    private ArrayList<Long> nukesTime = new ArrayList<>();
    double SPAWN_BOX_RATE;
    double TIME_AFTER_DEATH_RESTART;
    public NormalMode() {
        GRID_COLS = 12;
        GRID_ROWS = 10;
        CELL_SIZE = 60;
        WALL_DENSITY = 0.3;
        WALL_WIDTH = 4;
        SPAWN_BOX_RATE = 300;
        TIME_AFTER_DEATH_RESTART = 300;
    }

    @Override
    public void start(Stage stage) {
        gameContainer = new Pane();
        createGameZone();
        createPointZone();
        gameContainer.getChildren().add(gameZone);
        gameContainer.setPrefSize(MAP_WIDTH, MAP_HEIGHT); // Set your map's width and height
        gameContainer.setMinSize(MAP_WIDTH, MAP_HEIGHT);
        gameContainer.setMaxSize(MAP_WIDTH, MAP_HEIGHT);
        Rectangle clip = new Rectangle(MAP_WIDTH, MAP_HEIGHT);
        gameContainer.setClip(clip);
        gUILayout.setCenter(gameContainer);
        gUILayout.setTop(pointZone);
        Scene scene = new Scene(gUILayout);
        stage.setScene(scene);
        stage.setTitle("Normal Mode");
        stage.show();
        this.stage = stage;

        //Create boxe array
        boxesGrid = new Rectangle[GRID_ROWS][GRID_COLS];

        //Load Sounds
        try {
            URL url = getClass().getResource("/lego-yoda-death-sound-effect.wav");
            URL url2 = getClass().getResource("/PingPongBall.wav");
            URL url3 = getClass().getResource("/prowler-sound-effect_6bXErot.wav");
            URL url4 = getClass().getResource("/lazer-sfx.wav");
            URL url5 = getClass().getResource("/flashbang-gah-dayum.wav");
            URL url6 = getClass().getResource("/huh-meme.wav");
            URL url7 = getClass().getResource("/smoke-alarm-beep.wav");
            URL url8 = getClass().getResource("/boom-boom.wav");
            URL url9 = getClass().getResource("/raaar_FerSY7o.wav");
            URL url10 = getClass().getResource("/mag_forcefield.wav");
            URL url11 = getClass().getResource("/fortnite-shield-break-sound.wav");
            if (url == null || url2 == null || url3 == null || url4 == null || url5 == null || url6 == null) {
                System.out.println("file not found");
            }
            // 2. Create the cue (max polyphony of 4 simultaneous plays)
            deathSound = AudioCue.makeStereoCue(url, 4);
            bulletBounceSound = AudioCue.makeStereoCue(url2, 10);
            pickUpSound = AudioCue.makeStereoCue(url3,4);
            lazerShotSound = AudioCue.makeStereoCue(url4,2);
            flashBangSound = AudioCue.makeStereoCue(url5, 4);
            reverseDirectionSound = AudioCue.makeStereoCue(url6, 2);
            setMineSound = AudioCue.makeStereoCue(url7,2);
            explodeMineSound = AudioCue.makeStereoCue(url8,2);
            fanSound = AudioCue.makeStereoCue(url9,2);
            shieldActivation = AudioCue.makeStereoCue(url10,2);
            shieldBreak = AudioCue.makeStereoCue(url11, 2);

            // 3. Open the cue to prepare for playback
            deathSound.open();
            bulletBounceSound.open();
            pickUpSound.open();
            lazerShotSound.open();
            flashBangSound.open();
            reverseDirectionSound.open();
            setMineSound.open();
            explodeMineSound.open();
            fanSound.open();
            shieldBreak.open();
            shieldActivation.open();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        //Managing Input
        scene.setOnKeyPressed(e -> {
            keyPressed.add(e.getCode());
            //ResetMap
            if (keyPressed.contains(KeyCode.R)) {
                newRound();
            }

            if (keyPressed.contains(KeyCode.T)) {
                MapLayout map = new MapLayout(wallsGridV, wallsGridH);
                layouts.add(map);
                keyPressed.remove(KeyCode.T);
                System.out.println("Map Added To ArrayList");
            }
        });
        scene.setOnKeyReleased(e -> keyPressed.remove(e.getCode()));

        //Animation and Game Changes
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                handlePlayer1();
                handlePlayer2();
                handleBullets();
                handleNukes();
                handleNukeShrapnels();

                ++frameCountBox;
                if (frameCountBox == SPAWN_BOX_RATE) {
                    randomSpawnPowerUp();
                    frameCountBox = 0;
                }

                ++frameCountEnd;
                if (!player1.getAlive() || !player2.getAlive()) {
                    if (frameCountEnd == TIME_AFTER_DEATH_RESTART) {
                        newRound();
                    }
                } else {
                    frameCountEnd = 0;
                }
            }
        }.start();
    }
    public void createRandomMap(int rows, int cols, double cellSize, double wallWidth, double wallDensity) {
        GRID_ROWS = rows;
        GRID_COLS = cols;
        CELL_SIZE = cellSize;
        WALL_DENSITY = wallDensity;

        MAP_WIDTH = GRID_COLS * CELL_SIZE;
        MAP_HEIGHT = GRID_ROWS * CELL_SIZE;

        gameZone = new Group();

        wallsGridV = new Rectangle[GRID_ROWS][GRID_COLS];
        wallsGridH = new Rectangle[GRID_ROWS][GRID_COLS];

        double wallLength = CELL_SIZE - wallWidth;

        // ---------- MAP CREATION ----------

        // Horizontal borders
        for (int c = 0; c < GRID_COLS; c++) {
            Rectangle top = new Rectangle(wallLength, wallWidth);
            top.setX(wallWidth + c * CELL_SIZE);
            top.setY(0);

            Rectangle bottom = new Rectangle(wallLength, wallWidth);
            bottom.setX(wallWidth + c * CELL_SIZE);
            bottom.setY(MAP_HEIGHT - wallWidth);

            wallsGridH[0][c] = top;
            wallsGridH[GRID_ROWS - 1][c] = bottom;

            gameZone.getChildren().addAll(top, bottom);
        }

        // Vertical borders
        for (int r = 0; r < GRID_ROWS; r++) {
            Rectangle left = new Rectangle(wallWidth, wallLength);
            left.setX(0);
            left.setY(wallWidth + r * CELL_SIZE);

            Rectangle right = new Rectangle(wallWidth, wallLength);
            right.setX(MAP_WIDTH - wallWidth);
            right.setY(wallWidth + r * CELL_SIZE);

            wallsGridV[r][0] = left;
            wallsGridV[r][GRID_COLS - 1] = right;

            gameZone.getChildren().addAll(left, right);
        }

        // Random vertical walls
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (Math.random() < WALL_DENSITY && wallsGridV[r][c] == null) {
                    Rectangle w = new Rectangle(wallWidth, wallLength);
                    w.setX(c * CELL_SIZE);
                    w.setY(wallWidth + r * CELL_SIZE);

                    wallsGridV[r][c] = w;
                    gameZone.getChildren().add(w);
                }
            }
        }

        // Random horizontal walls
        for (int r = 0; r < GRID_ROWS; r++) {
            for (int c = 0; c < GRID_COLS; c++) {
                if (Math.random() < WALL_DENSITY && wallsGridH[r][c] == null) {
                    Rectangle w = new Rectangle(wallLength, wallWidth);
                    w.setX(wallWidth + c * CELL_SIZE);
                    w.setY(r * CELL_SIZE);
                    wallsGridH[r][c] = w;
                    gameZone.getChildren().add(w);
                }
            }
        }
    }
    public void createGameZone() {
        createRandomMap(GRID_ROWS, GRID_COLS, CELL_SIZE, WALL_WIDTH, WALL_DENSITY);
        projectiles = new Group();
        gameZone.getChildren().addAll(player1.getTank(),player2.getTank(),projectiles,nukeShrapnels);
        player1.getTank().setLayoutX(CELL_SIZE - 0.5*CELL_SIZE);
        player1.getTank().setLayoutY(CELL_SIZE - 0.5*CELL_SIZE);
        player1.setAngle(0);
        player2.getTank().setLayoutX((GRID_COLS - 1) * CELL_SIZE - 0.5*CELL_SIZE);
        player2.getTank().setLayoutY((GRID_ROWS - 1) * CELL_SIZE - 0.5*CELL_SIZE);
        player2.setAngle(180);
        player1.setAlive(true);
        player2.setAlive(true);
    }
    public void loadGameZoneRandom() {
        gameZone = new Group();
        MapLayout map = layouts.get((int)(Math.random() * (layouts.size())));
        wallsGridH = map.getHorizontalWalls();
        wallsGridV = map.getVerticalWalls();
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                gameZone.getChildren().add(wallsGridH[i][j]);
            }
        }
        for (int i = 0; i < GRID_ROWS; i++) {
            for (int j = 0; j < GRID_COLS; j++) {
                gameZone.getChildren().add(wallsGridV[i][j]);
            }
        }
        projectiles = new Group();
        gameZone.getChildren().addAll(player1.getTank(),player2.getTank(),projectiles,nukeShrapnels);
        player1.getTank().setLayoutX(CELL_SIZE - 0.5*CELL_SIZE);
        player1.getTank().setLayoutY(CELL_SIZE - 0.5*CELL_SIZE);
        player1.setAngle(0);
        player2.getTank().setLayoutX((GRID_COLS - 1) * CELL_SIZE - 0.5*CELL_SIZE);
        player2.getTank().setLayoutY((GRID_ROWS - 1) * CELL_SIZE - 0.5*CELL_SIZE);
        player2.setAngle(180);
        player1.setAlive(true);
        player2.setAlive(true);
    }

    public void createPointZone() {
        pointZone = new Group();
        containerPoints = new HBox(20);
        containerPoints.setAlignment(Pos.CENTER);

        VBox statsPlayer1 = new VBox();
        TankPlayer player1Image = new TankPlayer(Color.RED);
        labelNumberPointPlayer1 = new Label("Number of Points: 0");
        statsPlayer1.getChildren().addAll(player1Image.getTank(),labelNumberPointPlayer1);

        VBox statsPlayer2 = new VBox();
        TankPlayer player2Image = new TankPlayer(Color.GREEN);
        labelNumberPointPlayer2 = new Label("Number of Points: 0");
        statsPlayer2.getChildren().addAll(player2Image.getTank(),labelNumberPointPlayer2);

        VBox options = new VBox();
        Button exitB = new Button("Exit");
        exitB.setFocusTraversable(false);
        exitB.setFont(Font.font("Segoe UI", FontWeight.BOLD,10));
        exitB.setOnAction(actionEvent -> {
            pickUpSound.close();
            deathSound.close();
            bulletBounceSound.close();
            //saveAllMaps();
            System.exit(0);
        });
        Button mainMenuB = new Button("MainMenu");
        mainMenuB.setFocusTraversable(false);
        mainMenuB.setFont(Font.font("Segoe UI", FontWeight.BOLD,10));
        mainMenuB.setOnAction(actionEvent -> {
            try {
                GameStart gameStart = new GameStart();
                gameStart.start(stage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        options.getChildren().addAll(exitB,mainMenuB);

        containerPoints.getChildren().addAll(statsPlayer1,statsPlayer2,options);
        pointZone.getChildren().addAll(containerPoints);
    }
    public void saveAllMaps() {
        HelperMapData.saveMaps(fileForLayouts,layouts);
    }
    public void loadAllMaps() {
        layouts = HelperMapData.loadMaps(fileForLayouts,10,12);
    }

    //Controllers
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

        // ---- MOVEMENT ----
        if (keyPressed.contains(KeyCode.UP)) {
            player1.moveFoward();
            moved = true;
        }
        if (keyPressed.contains(KeyCode.DOWN)) {
            player1.moveBackward();
            moved = true;
        }

        // ---- FIRE WEAPON ----
        if (keyPressed.contains(KeyCode.M)) {
            if (!checkCollisionCannon(player1)) {
                if (player1.getNumberOfBullets() < 5) {
                    Node node = player1.shoot();
                    if (node instanceof Group) {
                        if (player1.getWeaponNumber() == 3) {
                            mines.add((Group)node);
                            gameContainer.getChildren().add(node);
                            setMineSound.play();
                            FadeTransition fadeTransition = new FadeTransition();
                            fadeTransition.setNode(node);
                            fadeTransition.setToValue(0);
                            fadeTransition.setRate(1);
                            fadeTransition.play();
                            changePlayerWeapon(player1, 0);
                        } else {
                            int i = 0;
                            while (i < ((Group) node).getChildren().size())
                                projectiles.getChildren().add(((Group) node).getChildren().get(i));
                            changePlayerWeapon(player1, 0);
                        }
                    } else if (node instanceof Line) {
                        gameContainer.getChildren().add(node);
                        FadeTransition fadeTransition = new FadeTransition();
                        fadeTransition.setNode(node);
                        fadeTransition.setToValue(0);
                        fadeTransition.setRate(-0.8);
                        fadeTransition.play();
                        lazerShotSound.play();
                        fadeTransition.setOnFinished(actionEvent -> {
                            if (Shape.intersect((Line)node,(Rectangle)player2.getTank().getChildren().get(0)).getBoundsInParent().getWidth() != -1 )
                                explode(player2);
                            gameContainer.getChildren().remove(node);
                            changePlayerWeapon(player1,0);
                        });
                    } else if (player1.getWeaponNumber() == 4) {
                        nukes.add((Circle)node);
                        nukesTime.add(nukes.indexOf(node), System.currentTimeMillis());
                        projectiles.getChildren().add(node);
                        changePlayerWeapon(player1, 0);
                    } else if (player1.getWeaponNumber() == 5) {
                        fanSound.play();
                        turnNodesAwayFromPoint(projectiles, player1.getX(), player1.getY());
                        turnNodesAwayFromPoint(nukeShrapnels, player1.getX(), player1.getY());
                        changePlayerWeapon(player1, 0);
                    } else {
                        projectiles.getChildren().add(node);
                    }
                    keyPressed.remove(KeyCode.M);
                    System.out.println("Bullets player 2: " + player1.getNumberOfBullets());
                }
            }
        }

        // ---- CHECK COLLISION ----
        // If movement causes overlap → undo movement only
        if (moved && checkCollisionTank(player1)) {
            player1.setX(oldX);
            player1.setY(oldY);
        }
        // If rotation causes overlap → undo rotation only
        if (rotated && checkCollisionTank(player1)) {
            player1.setAngle(oldAngle);
        }

        // If player is picking_up power up
        handleBoxesPowerUpApplication(player1,handleBoxesPickUp(player1));
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

        // ---- MOVEMENT ----
        if (keyPressed.contains(KeyCode.W)) {
            player2.moveFoward();
            moved = true;
        }
        if (keyPressed.contains(KeyCode.S)) {
            player2.moveBackward();
            moved = true;
        }

        // ---- FIRE WEAPON ----
        if (keyPressed.contains(KeyCode.Q)) {
            if (player2.getNumberOfBullets() < 5) {
                Node node = player2.shoot();
                if (node instanceof Group) {
                    if (player2.getWeaponNumber() == 3) {
                        mines.add((Group)node);
                        gameContainer.getChildren().add(node);
                        setMineSound.play();
                        FadeTransition fadeTransition = new FadeTransition();
                        fadeTransition.setNode(node);
                        fadeTransition.setToValue(0);
                        fadeTransition.setRate(0.5);
                        fadeTransition.play();
                        changePlayerWeapon(player2, 0);
                    } else {
                        int i = 0;
                        while (i < ((Group) node).getChildren().size())
                            projectiles.getChildren().add(((Group) node).getChildren().get(i));
                        changePlayerWeapon(player2, 0);
                    }
                } else if (node instanceof Line) {
                    gameContainer.getChildren().add(node);
                    FadeTransition fadeTransition = new FadeTransition();
                    fadeTransition.setNode(node);
                    fadeTransition.setToValue(0);
                    fadeTransition.setRate(-0.8);
                    fadeTransition.play();
                    lazerShotSound.play();
                    fadeTransition.setOnFinished(actionEvent -> {
                        if (Shape.intersect((Line)node,(Rectangle)player1.getTank().getChildren().get(0)).getBoundsInParent().getWidth() != -1 )
                            explode(player1);
                        gameContainer.getChildren().remove(node);
                        changePlayerWeapon(player2,0);
                    });
                }  else if (player2.getWeaponNumber() == 4) {
                    nukes.add((Circle)node);
                    nukesTime.add(nukes.indexOf(node), System.currentTimeMillis());
                    projectiles.getChildren().add(node);
                    changePlayerWeapon(player2, 0);
                } else if (player2.getWeaponNumber() == 5) {
                    fanSound.play();
                    turnNodesAwayFromPoint(projectiles, player2.getX(), player2.getY());
                    turnNodesAwayFromPoint(nukeShrapnels, player2.getX(), player2.getY());
                    changePlayerWeapon(player2, 0);
                }  else {
                    projectiles.getChildren().add(node);
                }
                keyPressed.remove(KeyCode.Q);
                System.out.println("Bullets player 2: " + player2.getNumberOfBullets());
            }
        }

        // ---- CHECK COLLISION ----
        // If movement causes overlap → undo movement only
        if (moved && checkCollisionTank(player2)) {
            player2.setX(oldX);
            player2.setY(oldY);
        }
        // If rotation causes overlap → undo rotation only
        if (rotated && checkCollisionTank(player2)) {
            player2.setAngle(oldAngle);
        }

        // If player is picking_up power up
        handleBoxesPowerUpApplication(player2,handleBoxesPickUp(player2));
    }

    //Collision methods
    private boolean checkCollisionTank(TankPlayer player) {
        Bounds tankBounds = player.getTank().getBoundsInParent();
        Rectangle body = (Rectangle)player.getTank().getChildren().get(0);

        int minCol = clamp((int)(tankBounds.getMinX() / CELL_SIZE), 0, GRID_COLS - 1);
        int maxCol = clamp((int)((tankBounds.getMaxX() + 1) / CELL_SIZE), 0, GRID_COLS - 1);
        int minRow = clamp((int)(tankBounds.getMinY() / CELL_SIZE), 0, GRID_ROWS - 1);
        int maxRow = clamp((int)((tankBounds.getMaxY() + 1) / CELL_SIZE), 0, GRID_ROWS - 1);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {

                Rectangle h = wallsGridH[r][c];
                if (h != null) {
                    if (Shape.intersect(body, h).getBoundsInParent().getWidth() != -1) {
                        return true;
                    }
                }

                Rectangle v = wallsGridV[r][c];
                if (v != null) {
                    if (Shape.intersect(body, v).getBoundsInParent().getWidth() != -1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private boolean checkCollisionCannon(TankPlayer player) {
        Shape cannon = player.getCannon();
        Bounds cannonBounds = cannon.getBoundsInParent();
        if (cannon == null) return false;

        int minCol = clamp((int)(cannonBounds.getMinX() / CELL_SIZE), 0, GRID_COLS - 1);
        int maxCol = clamp((int)((cannonBounds.getMaxX() + 1) / CELL_SIZE), 0, GRID_COLS - 1);
        int minRow = clamp((int)(cannonBounds.getMinY() / CELL_SIZE), 0, GRID_ROWS - 1);
        int maxRow = clamp((int)((cannonBounds.getMaxY() + 1) / CELL_SIZE), 0, GRID_ROWS - 1);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {

                Rectangle h = wallsGridH[r][c];
                if (h != null) {
                    if (Shape.intersect(cannon,h).getBoundsInParent().getWidth() != -1) {
                        return true;
                    }
                }

                Rectangle v = wallsGridV[r][c];
                if (v != null) {
                    if (Shape.intersect(cannon,v).getBoundsInParent().getWidth() != -1) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public void handleBullets() {
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
                        bulletBounceSound.play();
                        bullet.setLayoutX(oldX);
                        bullet.setRotate(Math.toDegrees(Math.PI - Math.toRadians(bullet.getRotate())));
                    }
                    Rectangle v = wallsGridV[r][c];
                    if (v != null && Shape.intersect((Circle) bullet, v).getBoundsInLocal().getWidth() != -1) {
                        bulletBounceSound.play();
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
                        bulletBounceSound.play();
                        bullet.setLayoutY(oldY);
                        bullet.setRotate(Math.toDegrees(-1 * Math.toRadians(bullet.getRotate())));
                    }
                    Rectangle v = wallsGridV[r][c];
                    if (v != null && Shape.intersect((Circle) bullet, v).getBoundsInLocal().getWidth() != -1) {
                        bulletBounceSound.play();
                        bullet.setLayoutY(oldY);
                        bullet.setRotate(Math.toDegrees(-1 * Math.toRadians(bullet.getRotate())));
                    }
                }
            }

            //Collision with a player
            if (Shape.intersect((Circle) bullet, (Rectangle) (player2.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                explode(player2);
                projectiles.getChildren().remove(bullet);
                if (nukes.contains(bullet)) {
                    nukeExplosion(nukes.indexOf(bullet));
                }
            }

            if (Shape.intersect((Circle) bullet, (Rectangle) (player1.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                explode(player1);
                projectiles.getChildren().remove(bullet);
                if (nukes.contains(bullet)) {
                    nukeExplosion(nukes.indexOf(bullet));
                }
            }

            //If outOfBounds remove Bullet
            if (bullet.getLayoutX() < 0 || bullet.getLayoutX() > MAP_WIDTH) {
                projectiles.getChildren().remove(bullet);
            }
            if (bullet.getLayoutY() < 0 || bullet.getLayoutY() > MAP_HEIGHT) {
                projectiles.getChildren().remove(bullet);
            }
        }

        for (Group mine : mines) {
            //Check is mine intersects with player
            if (Shape.intersect((Circle)(mine.getChildren().get(0)), (Rectangle) (player2.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                explode(player2);
                mines.remove(mine);
                explodeMineSound.play();
                gameContainer.getChildren().remove(mine);
            }

            if (Shape.intersect((Circle)(mine.getChildren().get(0)), (Rectangle) (player1.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                explode(player1);
                mines.remove(mine);
                explodeMineSound.play();
                gameContainer.getChildren().remove(mine);
            }
        }
    }
    public void handleNukeShrapnels() {
        for (int i = 0; i < nukeShrapnels.getChildren().size(); i++) {
            Node bullet = nukeShrapnels.getChildren().get(i);
            double currentSpeed = shrapnelsSpeed.get(i);

            // Move the bullet (allowing it to enter/pass through the wall)
            TankPlayer.nukeShrapnelMovementX((Circle) bullet, currentSpeed);
            TankPlayer.nukeShrapnelMovementY((Circle) bullet, currentSpeed);

            // Get bounds and initialize tracking set for this bullet if it doesn't exist
            Bounds bounds = bullet.getBoundsInParent();
            activeCollisionsShrapnels.putIfAbsent(bullet, new HashSet<>());
            Set<Rectangle> wallsCurrentlyTouching = new HashSet<>();
            Set<Rectangle> previouslyTouching = activeCollisionsShrapnels.get(bullet);

            int minCol = clamp((int) (bounds.getMinX() / CELL_SIZE), 0, GRID_COLS - 1);
            int maxCol = clamp((int) (bounds.getMaxX() / CELL_SIZE), 0, GRID_COLS - 1);
            int minRow = clamp((int) (bounds.getMinY() / CELL_SIZE), 0, GRID_ROWS - 1);
            int maxRow = clamp((int) (bounds.getMaxY() / CELL_SIZE), 0, GRID_ROWS - 1);

            boolean hitNewWall = false;

            // Check walls in the grid
            for (int r = minRow; r <= maxRow; r++) {
                for (int c = minCol; c <= maxCol; c++) {
                    checkWallOverlap(bullet, wallsGridH[r][c], wallsCurrentlyTouching);
                    checkWallOverlap(bullet, wallsGridV[r][c], wallsCurrentlyTouching);
                }
            }

            // Logic: If we are touching a wall now that we weren't touching last frame, it's a NEW hit.
            for (Rectangle wall : wallsCurrentlyTouching) {
                if (!previouslyTouching.contains(wall)) {
                    hitNewWall = true;
                    break; // We only reduce speed once per frame even if hitting 2 walls at once
                }
            }

            if (hitNewWall && currentSpeed > 1) {
                shrapnelsSpeed.set(i, Math.max(1.0, currentSpeed - 1.0));
            }

            // Update the "previously touching" list for the next frame
            activeCollisionsShrapnels.put(bullet, wallsCurrentlyTouching);

            if (Shape.intersect((Circle) bullet, (Rectangle) (player1.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                explode(player1);
                nukeShrapnels.getChildren().remove(bullet);
            }

            if (Shape.intersect((Circle) bullet, (Rectangle) (player2.getTank().getChildren().get(0))).getBoundsInLocal().getWidth() != -1) {
                explode(player2);
                nukeShrapnels.getChildren().remove(bullet);
            }
        }
    }
    private void checkWallOverlap(Node bullet, Rectangle wall, Set<Rectangle> touchingSet) {
        if (wall != null && Shape.intersect((Circle) bullet, wall).getBoundsInLocal().getWidth() != -1) {
            touchingSet.add(wall);
        }
    }
    public String handleBoxesPickUp(TankPlayer player) {

        Bounds tankBounds = player.getTank().getBoundsInParent();

        int minCol = clamp((int)(tankBounds.getMinX() / CELL_SIZE), 0, GRID_COLS - 1);
        int maxCol = clamp((int)(tankBounds.getMaxX() / CELL_SIZE), 0, GRID_COLS - 1);
        int minRow = clamp((int)(tankBounds.getMinY() / CELL_SIZE), 0, GRID_ROWS - 1);
        int maxRow = clamp((int)(tankBounds.getMaxY() / CELL_SIZE), 0, GRID_ROWS - 1);

        for (int r = minRow; r <= maxRow; r++) {
            for (int c = minCol; c <= maxCol; c++) {
                Rectangle b = boxesGrid[r][c];
                if (b != null) {
                    if (Shape.intersect((Rectangle)player.getTank().getChildren().get(0),b).getBoundsInParent().getWidth() != -1) {
                        boxesGrid[r][c] = null;
                        gameZone.getChildren().remove(b);
                        return b.getUserData().toString();
                    }
                }
            }
        }

        return null;
    }
    private int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    //In game events
    public void explode(TankPlayer player) {
        if (player.isHasShield()) {
            shieldBreak.play();
            player.setHasShield(false);
            return;
        }

        //Removing real player and creating dummy
        deathSound.setVolume(deathSound.play(),0.6);
        TankPlayer dummy = new TankPlayer(player.getColor(),player.getWeaponNumber());
        dummy.setX(player.getX());
        dummy.setY(player.getY());
        dummy.setAngle(player.getAngle());
        gameZone.getChildren().add(dummy.getTank());
        gameZone.getChildren().remove(player.getTank());
        player.getTank().setLayoutX(10000);
        player.getTank().setLayoutY(10000);
        player.setAlive(false);

        //Starting animation
        FadeTransition fadeTransition = new FadeTransition();
        fadeTransition.setNode(dummy.getTank());
        fadeTransition.setToValue(0);
        fadeTransition.play();
        fadeTransition.setOnFinished( e -> {
            gameZone.getChildren().remove(dummy.getTank());
            fadeTransition.stop();
        });
    }
    public void newRound() {
        //Checking who won
        if (!player1.getAlive() && player2.getAlive()) {
            player2.setScore(player2.getScore() + 1);
            labelNumberPointPlayer2.setText("Number of Points: " + player2.getScore());
        }
        if (!player2.getAlive() && player1.getAlive()) {
            player1.setScore(player1.getScore() + 1);
            labelNumberPointPlayer1.setText("Number of Points: " + player1.getScore());
        }

        gUILayout.setCenter(null);
        gameZone.getChildren().clear();
        mines.clear();
        nukes.clear();
        nukesTime.clear();
        nukeShrapnels.getChildren().clear();
        shrapnelsSpeed.clear();
        createGameZone();
        gameContainer.getChildren().clear();
        gameContainer.getChildren().add(gameZone);
        gUILayout.setCenter(gameContainer);
        System.out.println("New Round");
        player1.setNumberOfBullets(0);
        player1.getTank().setOpacity(100);
        player1.setHasShield(false);
        player2.setNumberOfBullets(0);
        player2.getTank().setOpacity(100);
        player2.setHasShield(false);
        changePlayerWeapon(player1,0);
        changePlayerWeapon(player2,0);
        frameCountBox = 0;
    }
    public void newRoundLoadedMaps() {
        //Checking who won
        if (!player1.getAlive() && player2.getAlive()) {
            player2.setScore(player2.getScore() + 1);
            labelNumberPointPlayer2.setText("Number of Points: " + player2.getScore());
        }
        if (!player2.getAlive() && player1.getAlive()) {
            player1.setScore(player1.getScore() + 1);
            labelNumberPointPlayer1.setText("Number of Points: " + player1.getScore());
        }

        gUILayout.setCenter(null);
        gameZone.getChildren().clear();
        mines.clear();
        nukes.clear();
        nukesTime.clear();
        nukeShrapnels.getChildren().clear();
        shrapnelsSpeed.clear();
        createGameZone();
        gameContainer.getChildren().clear();
        gameContainer.getChildren().add(gameZone);
        gUILayout.setCenter(gameContainer);
        System.out.println("New Round");
        player1.setNumberOfBullets(0);
        player1.getTank().setOpacity(100);
        player2.setNumberOfBullets(0);
        player2.getTank().setOpacity(100);
        changePlayerWeapon(player1,0);
        changePlayerWeapon(player2,0);
        frameCountBox = 0;
    }
    public void randomSpawnPowerUp() {
        int col = (int)(Math.random() * GRID_COLS);
        int row = (int)(Math.random() * GRID_ROWS);

        // Don't spawn if a box already exists in this cell
        if (boxesGrid[row][col] != null) {
            return;
        }

        double size = 15; // Slightly larger for better visibility/collision
        double x = col * CELL_SIZE + (CELL_SIZE - size) / 2;
        double y = row * CELL_SIZE + (CELL_SIZE - size) / 2;

        Rectangle box = new Rectangle(x, y, size, size);
        box.setStroke(Color.GREY);

        int random = (int)(Math.random() * 8);

        // Assign Type using UserData and set the visual color
        if (random == 0) {
            box.setUserData("FLASHBANG");
            box.setFill(Color.WHITE);
        } else if (random == 1) {
            box.setUserData("SHOTGUN");
            box.setFill(Color.TURQUOISE);
        } else if (random == 2) {
            box.setUserData("LAZER");
            box.setFill(Color.ORANGE);
        } else if (random == 3) {
            box.setUserData("REVERSE");
            box.setFill(Color.PINK);
        } else if (random == 4) {
            box.setUserData("MINE");
            box.setFill(Color.DARKGRAY);
        } else if (random == 5) {
            box.setUserData("NUKE");
            box.setFill(Color.YELLOW);
        } else if (random == 6) {
            box.setUserData("SHIELD");
            box.setFill(Color.LIMEGREEN);
        } else {
            box.setUserData("FAN");
            box.setFill(Color.PURPLE);
        }

        boxesGrid[row][col] = box;
        gameZone.getChildren().add(box);
    }
    private void changePlayerWeapon(TankPlayer player, int newWeapon) {
        // Save position + rotation
        double x = player.getX();
        double y = player.getY();
        double angle = player.getAngle();

        // Remove current tank safely
        gameZone.getChildren().remove(player.getTank());

        // Change weapon
        player.setWeaponNumber(newWeapon);
        player.setTank();

        // Restore transform
        player.setX(x);
        player.setY(y);
        player.setAngle(angle);

        // Add back to scene
        gameZone.getChildren().add(player.getTank());
    }
    public void handleBoxesPowerUpApplication(TankPlayer player, String data) {
        if (data != null) {
            if (data.equals("SHOTGUN")) {
                player.setNumberOfBullets(player.getNumberOfBullets() - 1);
                changePlayerWeapon(player,1);
            } else if (data.equals("LAZER")){
                player.setNumberOfBullets(player.getNumberOfBullets() - 1);
                changePlayerWeapon(player,2);
            } else if (data.equals("FLASHBANG")) {
                flashBangSound.play();
                // 1. Prepare the node state
                gameZone.setVisible(true);
                gameZone.setOpacity(100);
                // 2. Use the specialized constructor for reliability
                FadeTransition fadeIn = new FadeTransition(Duration.seconds(10), gameZone); // Duration/2 because rate is 2
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.setRate(1);
                fadeIn.play();   // Play the animation
            } else if (data.equals("REVERSE")) {
                player1.setAngle(player1.getAngle() + 180);
                player2.setAngle(player2.getAngle() + 180);
                reverseDirectionSound.play();
            } else if (data.equals("MINE")) {
                player.setNumberOfBullets(player.getNumberOfBullets() - 1);
                changePlayerWeapon(player,3);
            } else if (data.equals("NUKE")) {
                pickUpSound.play();
                player.setNumberOfBullets(player.getNumberOfBullets() - 1);
                changePlayerWeapon(player,4);
            } else if (data.equals("SHIELD")) {
                shieldActivation.play();
                player.setHasShield(true);
            } else if (data.equals("FAN")) {
                changePlayerWeapon(player,5);
            }
        }
    }
    public void handleNukes() {
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < nukes.size(); i++) {
            if (currentTime - nukesTime.get(i) > 10000) {
                nukeExplosion(i);
            }
        }
    }
    public void nukeExplosion(int i) {
        projectiles.getChildren().remove(nukes.get(i));
        double explosionPositionX = nukes.get(i).getLayoutX();
        double explosionPositionY = nukes.get(i).getLayoutY();
        nukesTime.remove(i);
        nukes.remove(i);
        explodeMineSound.play();
        for (int j = 0; j < 120; j++) {
            Circle shrapnel = new Circle(2);
            double randomAngle = Math.random() * 360;
            shrapnel.setRotate(Math.random() * 360);
            shrapnel.setLayoutX(explosionPositionX + Math.cos(Math.toRadians(randomAngle)) * 3);
            shrapnel.setLayoutY(explosionPositionY + Math.sin(Math.toRadians(randomAngle)) * 3);
            shrapnelsSpeed.add(8.0);
            nukeShrapnels.getChildren().add(shrapnel);
        }
    }
    public void rotateNodes180(Group group) {
        for (Node node : group.getChildren()) {
            node.setRotate(node.getRotate() + 180);
        }
    }
    public void turnNodesAwayFromPoint(Group group, double targetX, double targetY) {
        for (Node node : group.getChildren()) {
            // 1. Get the center position of the node in the group's coordinate space
            Bounds bounds = node.getBoundsInParent();
            double nodeCenterX = bounds.getMinX() + (bounds.getWidth() / 2.0);
            double nodeCenterY = bounds.getMinY() + (bounds.getHeight() / 2.0);

            // 2. Calculate the angle from the target point to the node center
            double deltaX = nodeCenterX - targetX;
            double deltaY = nodeCenterY - targetY;
            double angleInRadians = Math.atan2(deltaY, deltaX);
            double angleInDegrees = Math.toDegrees(angleInRadians);

            // 3. Apply rotation (JavaFX 0 degrees points Right/East)
            node.setRotate(angleInDegrees);
        }
    }



    public void getGridPosition(double x, double y) {
        // Basic integer division to find the cell
        int col = (int) (x / CELL_SIZE);
        int row = (int) (y / CELL_SIZE);

        // Bounds checking to ensure the click is inside the maze area
        if (col >= 0 && col < GRID_COLS && row >= 0 && row < GRID_ROWS) {
            System.out.println("Column " + col + " Row " + row);
        }
    }
}