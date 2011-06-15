package com.linfords.detangle;

/**
 *
 * @author Scott
 */
public class Space {

    final static int OFFSET = 8;
    Tile tile = null;
    final int posX;
    final int posY;
    State state = State.Wall;
    int nodeMarker = -1;

    Space(final int posX, final int posY) {
        this.posX = posX;
        this.posY = posY;
    }

    @Override
    public String toString() {
        String t = (tile == null) ? "x]" : tile.toString();
        return t + " {" + (posX - OFFSET) + ", " + (posY - OFFSET) + "} marker: " + nodeMarker;
    }

    void traverse() {
        nodeMarker = tile.connectingNode(nodeMarker);
    }

    void matchNodeMarkers(final Space otherSpace) {
        nodeMarker = Tile.adjacentNode(otherSpace.nodeMarker);
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
}
