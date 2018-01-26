package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by alexi on 1/26/2018.
 */

public class Antenna implements Serializable {
    private static final long serialVersionUID = 1L;

    static final int ANT_W = 110;
    static final int ANT_H = 150;
    static final int ANT_BASE_H = ANT_H / 4;

    public enum AntennaType {
        TRANSMISSION,
        ELECTONIC_WARFARE
    }

    public class AntennaRouting implements Serializable{
        private static final long serialVersionUID = 1L;

        public HashSet<Antenna> routed_antennas;
        public HashSet<Integer> routed_bases_top;
        public HashSet<Integer> routed_bases_bottom;

        AntennaRouting() {
            routed_antennas = new HashSet<Antenna>();
            routed_bases_bottom = new HashSet<Integer>();
            routed_bases_top = new HashSet<Integer>();
        }
    }

    public AntennaRouting routing;

    public final float _radius;
    public float _x,_y;
    public final AntennaType _type;

    public Antenna(float radius, float x,float y, AntennaType type) {
        _radius = radius;
        _x = x;
        _y = y;
        _type = type;
        routing = new AntennaRouting();
    }
}
