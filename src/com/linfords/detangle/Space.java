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
        return "{" + posX + ", " + posY + "} " + tile + " marker(" + nodeMarker + ")";
    }

    void traverse() {
        nodeMarker = tile.connectingNode(nodeMarker);
    }

    void matchNodeMarkers(final Space otherSpace) {
        nodeMarker = Tile.adjacentNode(otherSpace.nodeMarker);
    }

    void flipTile(Tile tile) {
        assert state == State.Covered : state;
        this.tile = tile;
        state = State.Playable;
    }

    enum State {

        Playable, Played, Wall, Covered
    }
}