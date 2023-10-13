package universe;

import procotol.DoorWay;

public final class NoMatchedPlanetException extends Exception {

    public final String path;
    public final DoorWay doorway;

    public NoMatchedPlanetException(final DoorWay doorway, final String path) {
        super("No matched planet path %s%s".formatted(doorway, path));
        this.doorway = doorway;
        this.path = path;
    }
    
}
