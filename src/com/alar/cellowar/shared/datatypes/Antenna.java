package com.alar.cellowar.shared.datatypes;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Created by alexi on 1/26/2018.
 */

public class Antenna implements Serializable {
    private static final long serialVersionUID = 1L;

    static float ANT_W = 7.0f;
    static float ANT_H = 10.0f;
    static float ANT_BASE_H = ANT_H / 4;

    static float ANT_EW_W = 8.0f;
    static float ANT_EW_H = 8.0f;
    static float ANT_EW_BASE_H = ANT_EW_H / 4;

    public enum AntennaType {
        TRANSMISSION,
        ELECTONIC_WARFARE
    }

    /**
     * Contains structures used for winner calculations
     */
    public class AntennaRouting implements Serializable{
        private static final long serialVersionUID = 1L;

        public HashSet<Antenna> routed_antennas;
        public HashSet<Integer> routed_bases_top;
        public HashSet<Integer> routed_bases_bottom;
        public boolean routed_goal;
        public boolean isSpoofed;

        AntennaRouting() {
            routed_antennas = new HashSet<Antenna>();
            routed_bases_bottom = new HashSet<Integer>();
            routed_bases_top = new HashSet<Integer>();
            routed_goal = false;
            isSpoofed = false;
        }
    }

    public AntennaRouting routing;

    public float _radius;
    public float _x,_y;
    public final AntennaType _type;

    public Antenna(float radius, float x,float y, AntennaType type) {
        _radius = radius;
        _x = x;
        _y = y;
        _type = type;
        routing = new AntennaRouting();
    }

    public boolean isInsideHalo(float x, float y) {
        return Math.sqrt(Math.pow(x - this._x, 2.0) + Math.pow(y - this._y, 2.0)) < this._radius;
    }

    private float get_W() {
        if(_type == AntennaType.TRANSMISSION) {
            return ANT_W;
        } else if (_type == AntennaType.ELECTONIC_WARFARE) {
            return ANT_EW_W;
        } else {
            assert (false);
            return 0.0f;
        }
    }

    private float get_H() {
        if(_type == AntennaType.TRANSMISSION) {
            return ANT_H;
        } else if (_type == AntennaType.ELECTONIC_WARFARE) {
            return ANT_EW_H;
        } else {
            assert (false);
            return 0.0f;
        }
    }

    private float get_Base_H() {
        if(_type == AntennaType.TRANSMISSION) {
            return ANT_BASE_H;
        } else if (_type == AntennaType.ELECTONIC_WARFARE) {
            return ANT_EW_BASE_H;
        } else {
            assert (false);
            return 0.0f;
        }
    }

    public float getLeft() {
        return this._x - get_W() / 2;
    }

    public float getTop() {
        return this._y - get_H() / 2;
    }

    public float getRight() {
        return this._x + get_W() / 2;
    }

    public float getBottom() {
        return this._y + get_H() / 2;
    }
}
