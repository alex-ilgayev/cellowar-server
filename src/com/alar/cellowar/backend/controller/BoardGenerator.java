package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Antenna;
import com.alar.cellowar.shared.datatypes.CelloWarGameData;
import com.alar.cellowar.shared.datatypes.Obstacle;

import java.util.Random;

public class BoardGenerator {
    public static CelloWarGameData createNewBoard () {
        CelloWarGameData m = new CelloWarGameData();
        m.setWH(1080.0f, 1120.0f);

        int numAnts = 3;
        int numAntis = 2;
        int numObs = 2;

        //TODO: random obstacles.

//        m.obst.add(new Obstacle(700.0f, 250.0f, 900.0f, 450.0f));
//        m.obst.add(new Obstacle(200.0f, 250.0f, 400.0f, 450.0f));
//        m.obst.add(new Obstacle(200.0f, 850.0f, 400.0f, 1050.0f));
//        m.obst.add(new Obstacle(700.0f, 850.0f, 900.0f, 1050.0f));

//        m.obst.add(new Obstacle(75, 25f, 90f, 45f));
//        m.obst.add(new Obstacle(20f, 25f, 40f, 45f));
//        m.obst.add(new Obstacle(20f, 85f, 40f, 90f));
//        m.obst.add(new Obstacle(70f, 85f, 90f, 90f));

        placingRandomInsideBoard(m, Antenna.AntennaType.TRANSMISSION, numAnts);
        placingRandomInsideBoard(m, Antenna.AntennaType.ELECTONIC_WARFARE, numAntis);


//        m.ants.add(new Antenna(200.0f, 50.0f, 50.0f, Antenna.AntennaType.TRANSMISSION ));
//        m.ants.add(new Antenna(80.0f, 650.0f, 650.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));
//        m.ants.add(new Antenna(150.0f, 650.0f, 650.0f, Antenna.AntennaType.ELECTONIC_WARFARE ));
//
//        m.ants.add(new Antenna(250.0f, 640.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));
//        m.ants.add(new Antenna(250.0f, 620.0f, 1050.0f, Antenna.AntennaType.TRANSMISSION ));

//        int numBlue = 5;
//        int numRed = 2;
//
//        Antenna ant;
//        for(int i=0; i<numBlue; i++) {
//            ant = new Antenna(generateRandomCoord(), generateRandomCoord(),
//                    generateRandomCoord(), Antenna.AntennaType.TRANSMISSION);
//
//        }
//        for(int i=0; i<numRed; i++) {
//            ant = new Antenna(generateRandomCoord(), generateRandomCoord(),
//                    generateRandomCoord(), Antenna.AntennaType.ELECTONIC_WARFARE);
//        }

        return m;
    }

    // place random antennas/antis in the board without touching obstacles
    private static CelloWarGameData placingRandomInsideBoard(CelloWarGameData m, Antenna.AntennaType type, int numToPlace) {
        Antenna ant;
        int numAntsInserted = 0;
        while(numAntsInserted < numToPlace) {
            ant = generateAntenna(m.getWidth(), m.getHeight(), type);
            boolean insideObs = false;
            for (Obstacle obs : m.obst) {
                if (isAntennaInsideObstacle(ant, obs)) {
                    insideObs = true;
                }
            }
            if (insideObs == false) {
                m.ants.add(ant);
                numAntsInserted++;
            } else
                insideObs = false;
        }

        return m;
    }

    private static Antenna generateAntenna(float width, float height, Antenna.AntennaType antennaType) {
        float x,y,radius;
        x = generateRandom(0, width);
        y = generateRandom(0, height);
        radius = generateRandom(0.1f*width, 0.3f*width);
        return new Antenna(radius, x, y, antennaType);
    }

    // generate float num between min to max (non inclusive).
    private static float generateRandom(float min, float max) {
        return new Random().nextInt((int)max-(int)min)+min;
    }

    private static boolean isAntennaInsideObstacle(Antenna ant, Obstacle obs) {
        if(ant._x > obs._left && ant._x < obs._right && ant._y > obs._top && ant._y < obs._bottom)
            return true;
        return false;
    }
}
