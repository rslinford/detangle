package com.linfords.detangle;

/**
 *
 * @author Scott
 */
public class Space {

    Tile tile = null;
    final Coordinates pos;
    State state = State.Wall;
    int marker = -1;

    Space(final Coordinates pos) {
        this.pos = pos;
    }

    @Override
    public String toString() {
        String t = (tile == null) ? "x]" : tile.toString();
        return t + " " + pos + " marker: " + marker;
    }

    void traverse() {
        marker = tile.connectingNode(marker);
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

    enum State {

        Playable, Played, Wall
    }

    static class Coordinates {

        final int x;
        final int y;

        Coordinates(final int x, final int y) {
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
            return Integer.valueOf(x).hashCode() + Integer.valueOf(y).hashCode();
        }

        @Override
        public String toString() {
            return "{" + x + ", " + y + "}";
        }
    }
}
