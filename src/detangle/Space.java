package detangle;

/**
 *
 * @author Scott
 */
public class Space {

    SpaceState state = SpaceState.Wall;
    int marker = -1;
    Tile tile = null;
    final Coordinates pos;

    Space(final Coordinates pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        return tile + " " + pos + " marker: " + marker;
    }

    void traverse() {
        System.out.print("  traverse: " + marker + " -> ");
        marker = tile.connectingNode(marker);
        System.out.println(marker + " direction: " + direction(marker));
    }

    void matchMarker(final Space otherSpace) {
        marker = Tile.adjacentNode(otherSpace.marker);
    }

    private String direction(int marker) {
        switch (marker) {
            case 0:
            case 1:
                return "N";
            case 2:
            case 3:
                return "NE";
            case 4:
            case 5:
                return "SE";
            case 6:
            case 7:
                return "S";
            case 8:
            case 9:
                return "SW";
            case 10:
            case 11:
                return "NW";
            default:
                throw new IllegalArgumentException("marker " + marker);
        }
    }

    enum SpaceState {

        Playable, Played, Wall
    }

    static class Coordinates {

        final float x;
        final float y;

        Coordinates(final float x, final float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object candidate) {
            if ((candidate instanceof Coordinates) != true) {
                return false;
            }

            Coordinates otherPos = (Coordinates) candidate;
            if ((x != otherPos.x) || (y != otherPos.y)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            return Float.valueOf(x).hashCode() + Float.valueOf(y).hashCode();
        }

        @Override
        public String toString() {
            return "{" + x + ", " + y + "}";
        }
    }
}
