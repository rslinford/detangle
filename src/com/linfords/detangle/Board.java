package com.linfords.detangle;

import com.linfords.detangle.Space.State;

/**
 *
 * @author Scott
 */
class Board {

    private final Space[][] board = new Space[18][18];
    private final TileStack tiles = new TileStack();
    private Tile swapTile;
    Space current;
    Space adjacent;

    Board() {
        this.swapTile = tiles.pop();
        this.current = createStartingSpace();
        this.adjacent = locateAdjacent(current);
        this.adjacent.matchNodeMarkers(current);
    }

    private Space locateAdjacent(final Space space) {
        switch (space.nodeMarker) {
            case 0:
            case 1:
                return locateSpace(space.posX, space.posY + 2);
            case 2:
            case 3:
                return locateSpace(space.posX + 2, space.posY + 1);
            case 4:
            case 5:
                return locateSpace(space.posX + 2, space.posY - 1);
            case 6:
            case 7:
                return locateSpace(space.posX, space.posY - 2);
            case 8:
            case 9:
                return locateSpace(space.posX - 2, space.posY - 1);
            case 10:
            case 11:
                return locateSpace(space.posX - 2, space.posY + 1);
            default:
                throw new IllegalArgumentException("Space token(" + space.nodeMarker + ")");
        }
    }

    private Space createStartingSpace() {
        Space space = new Space(Space.OFFSET, Space.OFFSET);
        board[Space.OFFSET][Space.OFFSET] = space;
        space.state = State.Wall;
        space.nodeMarker = 0;
        return space;
    }

    private Space locateSpace(final int posX, final int posY) {
        Space space = board[posX][posY];
        if (space == null) {
            space = new Space(posX, posY);
            board[posX][posY] = space;
            if (outOfBounds(posX, posY)) {
                space.state = State.Wall;
            } else {
                space.state = State.Playable;
                space.tile = tiles.pop();
            }
        }

        return space;
    }

    private void advance() {
        adjacent.traverse();
        current = adjacent;
        adjacent = locateAdjacent(current);
        adjacent.matchNodeMarkers(current);
    }

    void play() {
        if (adjacent.state != State.Playable) {
            throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
        }

        adjacent.state = State.Played;
        advance();
    }

    void flow() {
        if (adjacent.state != State.Played) {
            throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
        }

        advance();
    }

    private boolean outOfBounds(final int posX, final int posY) {
        final int x = Math.abs(posX - Space.OFFSET);
        if (x > 6) {
            return true;
        }

        final int y = Math.abs(posY - Space.OFFSET);

        switch (x) {
            case 0:
                return y > 6;
            case 2:
                return y > 5;
            case 4:
                return y > 4;
            case 6:
                return y > 3;
        }

        return false;
    }

    void putTileBack(final int posX, final int posY) {
        board[posX][posY] = null;
        tiles.unpop();
    }

    /** Undo play on adjacentPos and set its rotation for another try. */
    void undoPlay(final int currentPosX, final int currentPosY, final int currentMarker,
            final int adjacentPosX, final int adjacentPosY, final int rotation) {
        current = locateSpace(currentPosX, currentPosY);
        current.nodeMarker = currentMarker;
        adjacent = locateSpace(adjacentPosX, adjacentPosY);
        adjacent.nodeMarker = Tile.adjacentNode(currentMarker);
        adjacent.tile.setRotation(rotation);
        adjacent.state = Space.State.Playable;
    }
}
