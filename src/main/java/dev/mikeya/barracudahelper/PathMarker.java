package dev.mikeya.barracudahelper;

import lombok.Setter;
import net.runelite.api.coords.WorldPoint;

public class PathMarker {
    public enum Type {
        PATH, CRATE, RUM
    }

    private final WorldPoint location;
    @Setter
    private Type type;

    public PathMarker(WorldPoint location, Type type) {
        this.location = location;
        this.type = type;
    }

    public WorldPoint getLocation() {
        return location;
    }

    public Type getType() {
        return type;
    }

}
