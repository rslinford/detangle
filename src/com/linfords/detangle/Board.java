package com.linfords.detangle;

import com.linfords.detangle.Space.State;
import java.util.ArrayList;
import java.util.Collection;

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
        this.adjacent = locateMarkedAdjacent(current);
        this.adjacent.matchNodeMarkers(current);
        this.adjacent.flipTile(tiles.pop());
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

    private Space locateMarkedAdjacent(final Space space) {
        return locateAdjacent(space.posX, space.posY, space.nodeMarker);
    }

    private Space locateAdjacent(final Space space, int node) {
        return locateAdjacent(space.posX, space.posY, node);
    }

    private Space locateAdjacent(final int x, final int y, final int node) {
        switch (node) {
            case 0:
            case 1:
                return board[x][y + 2];
            case 2:
            case 3:
                return board[x + 2][y + 1];
            case 4:
            case 5:
                return board[x + 2][y - 1];
            case 6:
            case 7:
                return board[x][y - 2];
            case 8:
            case 9:
                return board[x - 2][y - 1];
            case 10:
            case 11:
                return board[x - 2][y + 1];
            default:
                throw new IllegalArgumentException("node(" + node + ")");
        }
    }

    private Space initStartingSpace() {
        final Space space = board[Space.OFFSET][Space.OFFSET];
        space.state = State.Wall;
        space.nodeMarker = 0;
        return space;
    }

    private void advance() {
        adjacent.traverse();
        current = adjacent;
        adjacent = locateMarkedAdjacent(current);
        adjacent.matchNodeMarkers(current);
        switch (adjacent.state) {
            case Covered:
                adjacent.flipTile(tiles.pop());
                break;
            case Wall:
            case Played:
                // expected cases, but nothing to do
                break;
            default:
                assert false : adjacent.state;
        }
        if (adjacent.state == State.Playable) {
        }
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

    static class WallNode {

        final Space space;
        final int node;

        public WallNode(final Space space, final int node) {
            this.space = space;
            this.node = node;
        }
    }
    Collection<WallNode> wallNodes = new ArrayList();

    void spinForWalls(Space s) {
        for (int i = 0; i < Tile.NODE_QTY; i++) {
            Space s2 = locateAdjacent(s, i);
            if (s2.state == State.Wall) {
                wallNodes.add(new WallNode(s2, Tile.adjacentNode(i)));
            }
        }
    }

    void findWallNodes() {
        Space s1 = board[Space.OFFSET][Space.OFFSET];

        // spin around center
        for (int i = 0; i < Tile.NODE_QTY; i += 2) {
            Space s2 = locateAdjacent(s1, i);
            wallNodes.add(new WallNode(s2, Tile.adjacentNode(i)));
            wallNodes.add(new WallNode(s2, Tile.adjacentNode(i + 1)));
        }

        do {
            s1 = locateAdjacent(s1, 0);
        } while (s1.state != State.Wall);
        
        
    }
}
