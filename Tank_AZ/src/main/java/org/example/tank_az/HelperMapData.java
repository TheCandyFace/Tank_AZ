package org.example.tank_az;

import java.io.*;
import java.util.ArrayList;

public class HelperMapData {
    public static void saveMaps(File file, ArrayList<MapLayout> maps) {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(file))) {
            dos.writeInt(maps.size());

            for (MapLayout map : maps) {
                boolean[][] vPresent = map.getvWallPresent();
                boolean[][] hPresent = map.gethWallPresent();

                // Safety check: ensure arrays aren't null
                if (vPresent == null || hPresent == null) {
                    throw new IOException("Map data is not initialized for one of the maps!");
                }

                // Write Vertical (using actual array lengths)
                dos.writeInt(vPresent.length);
                dos.writeInt(vPresent[0].length);
                for (boolean[] row : vPresent) {
                    for (boolean wall : row) {
                        dos.writeBoolean(wall);
                    }
                }

                // Write Horizontal
                dos.writeInt(hPresent.length);
                dos.writeInt(hPresent[0].length);
                for (boolean[] row : hPresent) {
                    for (boolean wall : row) {
                        dos.writeBoolean(wall);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static ArrayList<MapLayout> loadMaps(File file, int rows, int cols) {
        ArrayList<MapLayout> maps = new ArrayList<>();
        try (DataInputStream dis = new DataInputStream(new FileInputStream(file))) {
            int mapCount = dis.readInt();
            for (int m = 0; m < mapCount; m++) {
                MapLayout map = new MapLayout(rows, cols);
                // Load Vertical
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j <= cols; j++) {
                        boolean present = dis.readBoolean();
                        map.getvWallPresent()[i][j] = present;
                        map.getVerticalWalls()[i][j].setVisible(present);
                    }
                }
                // Load Horizontal
                for (int i = 0; i <= rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        boolean present = dis.readBoolean();
                        map.gethWallPresent()[i][j] = present;
                        map.getHorizontalWalls()[i][j].setVisible(present);
                    }
                }
                maps.add(map);
            }
        } catch (IOException e) { e.printStackTrace(); }
        return maps;
    }
}
