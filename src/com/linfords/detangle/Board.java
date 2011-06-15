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
        wipeSpaces();
        this.current = initStartingSpace();
        this.adjacent = locateAdjacent(current);
        this.adjacent.matchNodeMarkers(current);
    }

    private void wipeSpaces() {
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                final Space space = new Space(x, y);
                board[x][y] = space;
                if (outOfBounds(x, y)) {
                    space.state = State.Wall;
                } else {
                    space.state = State.Covered;
                }
            }
        }
    }

    private Space locateAdjacent(final Space space) {
        switch (space.nodeMarker) {
            case 0:
            case 1:
                return prepareSpace(space.posX, space.posY + 2);
            case 2:
            case 3:
                return prepareSpace(space.posX + 2, space.posY + 1);
            case 4:
            case 5:
                return prepareSpace(space.posX + 2, space.posY - 1);
            case 6:
            case 7:
                return prepareSpace(space.posX, space.posY - 2);
            case 8:
            case 9:
                return prepareSpace(space.posX - 2, space.posY - 1);
            case 10:
            case 11:
                return prepareSpace(space.posX - 2, space.posY + 1);
            default:
                throw new IllegalArgumentException("Space token(" + space.nodeMarker + ")");
        }
    }

    private Space initStartingSpace() {
        final Space space = board[Space.OFFSET][Space.OFFSET];
        space.state = State.Wall;
        space.nodeMarker = 0;
        return space;
    }

    private Space prepareSpace(final int posX, final int posY) {
        final Space space = board[posX][posY];
        if (space.state == State.Covered) {
            space.tile = tiles.pop();
            space.state = State.Playable;
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
        final Space space = board[posX][posY];
        space.state = State.Covered;
        space.tile = null;
        tiles.unpop();
    }

    /** Undo play on adjacentPos and set its rotation for another try. */
    void undoPlay(final int currentPosX, final int currentPosY, final int currentMarker,
            final int adjacentPosX, final int adjacentPosY, final int rotation) {
        current = board[currentPosX][currentPosY];
        current.nodeMarker = currentMarker;
        adjacent = board[adjacentPosX][adjacentPosY];
        adjacent.nodeMarker = Tile.adjacentNode(currentMarker);
        adjacent.tile.setRotation(rotation);
        adjacent.state = Space.State.Playable;
    }
}
