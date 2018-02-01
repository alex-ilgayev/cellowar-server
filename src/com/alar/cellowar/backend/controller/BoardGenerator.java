package com.alar.cellowar.backend.controller;

import com.alar.cellowar.shared.datatypes.Antenna;
import com.alar.cellowar.shared.datatypes.CelloWarGameData;
import com.alar.cellowar.shared.datatypes.Obstacle;

import java.util.Random;

public class BoardGenerator {
    public static CelloWarGameData createNewBoard () {
        CelloWarGameData m = new CelloWarGameData();
        m.setWH(1080.0f, 12004.0f);

        int numAnts = 3;
        int numAntis = 2;
        int numObs = 2;

        //TODO: random obstacles.

        m.obst.add(new Obstacle(700.0f, 250.0f, 900.0f, 450.0f));
        m.obst.add(new Obstacle(200.0f, 250.0f, 400.0f, 450.0f));
        m.obst.add(new Obstacle(200.0f, 850.0f, 400.0f, 1050.0f));
        m.obst.add(new Obstacle(700.0f, 850.0f, 900.0f, 1050.0f));



        int i=0;

        Antenna ant;
        do {
            ant = generateAntenna(m.getWidth(), m.getHeight(), Antenna.AntennaType.TRANSMISSION);
            boolean insideObs = false;
            for(Obstacle obs: m.obst) {
                if(isAntennaInsideObstacle(ant, obs)) {
                    insideObs = true;
                }
            }
        } while(



            m.ants.add());
        }
        for(int i=0; i<numAntis; i++) {
            m.ants.add(generateAntenna(m.getWidth(), m.getHeight(), Antenna.AntennaType.ELECTONIC_WARFARE));
        }

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
