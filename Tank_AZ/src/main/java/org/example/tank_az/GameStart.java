package org.example.tank_az;

import javafx.application.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class GameStart extends Application {
    @Override
    public void start(Stage gameWindow) throws Exception {
        //Creating UI components for menu
        Text title = new Text("TANK AZ");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD,20));
        Button playB = new Button("Play");
        playB.setFont(Font.font("Segoe UI", FontWeight.BOLD,10));
        Button scoreboardB = new Button("ScoreBoard");
        scoreboardB.setFont(Font.font("Segoe UI", FontWeight.BOLD,10));
        Button exitB = new Button("Exit");
        exitB.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        //Layout of UI
        VBox menuPane = new VBox();
        menuPane.setSpacing(20);
        menuPane.setAlignment(Pos.CENTER);
        menuPane.getChildren().addAll(title,playB,scoreboardB,exitB);
        Scene sceneMenu = new Scene(menuPane, 900,600);
        gameWindow.setTitle("TANK AZ");
        gameWindow.setScene(sceneMenu);
        gameWindow.setResizable(false);
        gameWindow.show();

        //Button Handlers
        scoreboardB.setOnAction(actionEvent -> {

        });

        exitB.setOnAction(actionEvent -> {
            System.exit(0);
        });

        playB.setOnAction(actionEvent -> {
            NormalMode game = new NormalMode();
            game.start(gameWindow);
        });

        TankPlayer player1 = new TankPlayer(Color.RED,0);
        menuPane.getChildren().add(player1.getTank());
        player1.getTank().setRotate(90);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
