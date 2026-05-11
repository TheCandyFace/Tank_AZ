module org.example.tank_az {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jdk.xml.dom;
    requires javafx.base;
    requires java.desktop;
    requires audiocue;


    opens org.example.tank_az to javafx.fxml;
    exports org.example.tank_az;
}