package com.c2t2s.mc;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class ArrowSpawnLocation {

    private Location arrowLocation;
    private Vector arrowDirection;

    public ArrowSpawnLocation (Location arrowLocation, Vector arrowDirection) {
        this.arrowLocation = arrowLocation;
        this.arrowDirection = arrowDirection;
    }

    public Location getArrowLocation() {
        return arrowLocation;
    }

    public Vector getArrowDirection() {
        return arrowDirection;
    }
}
