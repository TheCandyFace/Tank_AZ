package org.example.tank_az;

import javafx.scene.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;

public class TankPlayer {
    //Data Field
    private Group tank = new Group();
    private Group weapon = new Group();
    private int weaponNumber = 4;
    private Rectangle body = new Rectangle();
    private Circle turret = new Circle();
    private Circle shield = new Circle();
    private Color color;
    private double movingSpeed = 2;
    private double rotatingSpeed = 3;
    private double angle = 0;
    private int numberOfBullets = 0;
    private boolean isAlive = true;
    private int score = 0;
    private static double bulletSpeedDefaultWeapon = 2.5;
    private boolean hasShield = false;

    //Constructor
    public TankPlayer(Color playerColor, int weaponNumber) {
        body.setWidth(25);
        body.setHeight(15);
        body.setX(-12.5);
        body.setY(-7.5);
        body.setFill(playerColor);
        body.setStroke(Color.BLACK);

        turret.setRadius(5);
        turret.setCenterX(0);
        turret.setCenterY(0);
        turret.setStroke(Color.BLACK);
        turret.setFill(playerColor);


        this.weaponNumber = weaponNumber;
        weapon = getWeapon(weaponNumber);
        color = playerColor;

        tank.getChildren().addAll(body,turret,weapon);
    }

    public TankPlayer(Color playerColor) {
        body.setWidth(25);
        body.setHeight(15);
        body.setX(-12.5);
        body.setY(-7.5);
        body.setFill(playerColor);
        body.setStroke(Color.BLACK);

        turret.setRadius(5);
        turret.setCenterX(0);
        turret.setCenterY(0);
        turret.setStroke(Color.BLACK);
        turret.setFill(playerColor);

        weapon = getWeapon(weaponNumber);
        color = playerColor;

        tank.getChildren().addAll(body,turret,weapon);
    }

    public Color getColor() {
        return color;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Group getWeapon(int i) {
        Group weapon = new Group();
        switch (i){
            case 0:
                Rectangle cannon = new Rectangle();
                cannon.setStroke(Color.BLACK);
                cannon.setFill(body.getFill());
                cannon.setWidth(15);
                cannon.setHeight(3);
                cannon.setY(-1.5);
                cannon.setX(0);
                weapon.getChildren().add(cannon);
                weaponNumber = 0;
                return weapon;
            case 1:
                Polygon shotgun = new Polygon(5,0,15,-5,15,5);
                shotgun.setStroke(Color.BLACK);
                shotgun.setFill(body.getFill());
                weapon.getChildren().add(shotgun);
                weaponNumber = 1;
                return weapon;
            case 2:
                Arc beamCoupola = new Arc(0,0,7,5,90,180);
                Rectangle branch = new Rectangle(0,-0.5,8,1);
                Circle bulb = new Circle(10,0,2);
                weapon.getChildren().addAll(branch,beamCoupola,bulb);
                weaponNumber = 2;
                return weapon;
            case 3:
                Rectangle mineBox = new Rectangle(-5,-5,10,10);
                mineBox.setFill(Color.BLACK);
                mineBox.setStroke(Color.BLACK);
                weapon.getChildren().add(mineBox);
                weaponNumber = 3;
                return weapon;
            case 4:
                Rectangle hugeCannon = new Rectangle();
                hugeCannon.setStroke(Color.BLACK);
                hugeCannon.setFill(body.getFill());
                hugeCannon.setWidth(15);
                hugeCannon.setHeight(7);
                hugeCannon.setY(-3.5);
                hugeCannon.setX(0);
                weapon.getChildren().add(hugeCannon);
                weaponNumber = 4;
                return weapon;
        }
        return weapon;
    }

    public void setWeaponNumber(int weaponNumber) {
        this.weaponNumber = weaponNumber;
    }

    public int getWeaponNumber() {
        return weaponNumber;
    }

    public Group getTank() {
        return tank;
    }

    public void setTank() {
        tank = null;
        weapon = getWeapon(weaponNumber);
        tank = new Group(body,turret,weapon);
        tank.setRotate(angle);
    }

    public Shape getCannon() {
        if (weapon.getChildren().isEmpty()) return null;

        Node node = weapon.getChildren().get(0);

        if (node instanceof Shape) {
            return (Shape) node;
        }

        return null;
    }

    public void setNumberOfBullets(int numberOfBullets) {
        this.numberOfBullets = numberOfBullets;
    }

    public int getNumberOfBullets() { return numberOfBullets; }

    public double getX() {
        return tank.getLayoutX();
    }

    public double getY() {
        return tank.getLayoutY();
    }

    public void setX(double x) {
        tank.setLayoutX(x);
    }

    public void setY(double y) {
        tank.setLayoutY(y);
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle = angle;
        tank.setRotate(angle);
    }

    public void rotateLeft() {
        tank.setRotate(angle -= rotatingSpeed);
    }

    public void rotateRight() {
        tank.setRotate(angle += rotatingSpeed);
    }

    public void moveFoward() {
        tank.setLayoutX(tank.getLayoutX() + movingSpeed * Math.cos(Math.toRadians(angle)));
        tank.setLayoutY(tank.getLayoutY() + movingSpeed * Math.sin(Math.toRadians(angle)));
    }

    public void moveBackward() {
        tank.setLayoutX(tank.getLayoutX() - movingSpeed * Math.cos(Math.toRadians(angle)));
        tank.setLayoutY(tank.getLayoutY() - movingSpeed * Math.sin(Math.toRadians(angle)));
    }

    public Node shoot() {
        switch (weaponNumber) {
            case 0:
                Circle bullet = new Circle(2);
                bullet.setRotate(angle);
                bullet.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 14);
                bullet.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 14);
                numberOfBullets++;
                return bullet;
            case 1:
                Circle bullet1 = new Circle(1);
                bullet1.setRotate(angle);
                bullet1.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 12);
                bullet1.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 12);
                Circle bullet2 = new Circle(1);
                bullet2.setRotate(angle + 3);
                bullet2.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 12);
                bullet2.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 12);
                Circle bullet3 = new Circle(1);
                bullet3.setRotate(angle - 3);
                bullet3.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 12);
                bullet3.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 12);
                Circle bullet4 = new Circle(1);
                bullet4.setRotate(angle + 6);
                bullet4.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 12);
                bullet4.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 12);
                Circle bullet5 = new Circle(1);
                bullet5.setRotate(angle - 6);
                bullet5.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 12);
                bullet5.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 12);
                numberOfBullets++;
                return new Group(bullet1,bullet2,bullet3,bullet4,bullet5);
            case 2:
                double length = 300;
                double radians = Math.toRadians(angle);

                // The tank's center is now exactly its LayoutX/Y
                double startX = getTank().getLayoutX();
                double startY = getTank().getLayoutY();

                // If you want the beam to start at the tip of a 15px barrel:
                double barrelLength = 5;
                double tipX = startX + Math.cos(radians) * barrelLength;
                double tipY = startY + Math.sin(radians) * barrelLength;

                double endX = startX + Math.cos(radians) * (barrelLength + length);
                double endY = startY + Math.sin(radians) * (barrelLength + length);

                Line beam = new Line(tipX, tipY, endX, endY);
                beam.setStroke(Color.RED);
                beam.setStrokeWidth(2);

                numberOfBullets++;
                beam.setManaged(false);
                beam.setVisible(true);
                return beam;
            case 3:
                Circle base = new Circle(5);
                base.setFill(Color.BLACK);
                Circle led = new Circle(2);
                led.setFill(Color.RED);
                Group mine = new Group(base,led);
                mine.setLayoutX(getTank().getLayoutX() - Math.cos(Math.toRadians(angle)) * 22);
                mine.setLayoutY(getTank().getLayoutY() - Math.sin(Math.toRadians(angle)) * 22);
                numberOfBullets++;
                return mine;
            case 4:
                Circle bomb = new Circle(7);
                bomb.setFill(Color.BLACK);
                bomb.setRotate(angle);
                bomb.setLayoutX(getTank().getLayoutX() + Math.cos(Math.toRadians(angle)) * 30);
                bomb.setLayoutY(getTank().getLayoutY() + Math.sin(Math.toRadians(angle)) * 30);
                numberOfBullets++;
                return bomb;
        }
        return new Circle();
    }

    public static void bulletMovementX(Circle bullet) {
        bullet.setLayoutX(bullet.getLayoutX() + Math.cos(Math.toRadians(bullet.getRotate())) * bulletSpeedDefaultWeapon);
    }

    public static void bulletMovementY(Circle bullet) {
        bullet.setLayoutY(bullet.getLayoutY() + Math.sin(Math.toRadians(bullet.getRotate())) * bulletSpeedDefaultWeapon);
    }

    public static void nukeShrapnelMovementX(Circle bullet, double speed) {
        bullet.setLayoutX(bullet.getLayoutX() + Math.cos(Math.toRadians(bullet.getRotate())) * speed);
    }

    public static void nukeShrapnelMovementY(Circle bullet, double speed) {
        bullet.setLayoutY(bullet.getLayoutY() + Math.sin(Math.toRadians(bullet.getRotate())) * speed);
    }

    public void setAlive(boolean isAlive) {
        this.isAlive = isAlive;
    }

    public boolean getAlive() {
        return isAlive;
    }

    public boolean isHasShield() {
        return hasShield;
    }

    public void setHasShield(boolean hasShield) {
        this.hasShield = hasShield;
        if (hasShield) {
            body.setStroke(Color.LIMEGREEN);
            body.setStrokeWidth(4);
        } else {
            body.setStroke(Color.BLACK);
            body.setStrokeWidth(1);
        }
    }
}
