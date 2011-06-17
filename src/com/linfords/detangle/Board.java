package com.linfords.detangle;

import com.linfords.detangle.Space.State;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Scott
 */
final class Board {

    private final Space[][] board = new Space[18][18];
    private final TileStack tiles = new TileStack();
    private Tile swapTile;
    Space current;
    Space adjacent;
    private WallNode wallNodes[];

    Board() {
        this.swapTile = tiles.pop();
        wipeSpaces();
        this.current = initStartingSpace();
        this.adjacent = locateMarkedAdjacent(current);
        this.adjacent.matchNodeMarkers(current);
        this.adjacent.flipTile(tiles.pop());

        initWallNodes();
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

        @Override
        public String toString() {
            return "[WallNode " + space + " wallNode(" + node + ")]";

        }
    }

    void initWallNodes() {

        List<WallNode> temp = new ArrayList();
        
        // Nodes around the center tile. Clockwise.
        temp.add(new WallNode(board[8][10], 6));
        temp.add(new WallNode(board[8][10], 7));
        temp.add(new WallNode(board[10][9], 8));
        temp.add(new WallNode(board[10][9], 9));
        temp.add(new WallNode(board[10][7], 10));
        temp.add(new WallNode(board[10][7], 11));
        temp.add(new WallNode(board[8][6], 0));
        temp.add(new WallNode(board[8][6], 1));
        temp.add(new WallNode(board[6][7], 2));
        temp.add(new WallNode(board[6][7], 3));
        temp.add(new WallNode(board[6][9], 4));
        temp.add(new WallNode(board[6][9], 5));
        
        // Top Corner
        temp.add(new WallNode(board[8][14], 10));
        temp.add(new WallNode(board[8][14], 11));
        temp.add(new WallNode(board[8][14], 0));
        temp.add(new WallNode(board[8][14], 1));
        temp.add(new WallNode(board[8][14], 2));
        temp.add(new WallNode(board[8][14], 3));
        
        // North East Side
        temp.add(new WallNode(board[10][13], 0));
        temp.add(new WallNode(board[10][13], 1));
        temp.add(new WallNode(board[10][13], 2));
        temp.add(new WallNode(board[10][13], 3));
        
        temp.add(new WallNode(board[12][12], 0));
        temp.add(new WallNode(board[12][12], 1));
        temp.add(new WallNode(board[12][12], 2));
        temp.add(new WallNode(board[12][12], 3));
        
        // North East Corner
        temp.add(new WallNode(board[14][11], 0));
        temp.add(new WallNode(board[14][11], 1));
        temp.add(new WallNode(board[14][11], 2));
        temp.add(new WallNode(board[14][11], 3));
        temp.add(new WallNode(board[14][11], 4));
        temp.add(new WallNode(board[14][11], 5));
        
        // East Side
        temp.add(new WallNode(board[14][9], 2));
        temp.add(new WallNode(board[14][9], 3));
        temp.add(new WallNode(board[14][9], 4));
        temp.add(new WallNode(board[14][9], 5));
        
        temp.add(new WallNode(board[14][7], 2));
        temp.add(new WallNode(board[14][7], 3));
        temp.add(new WallNode(board[14][7], 4));
        temp.add(new WallNode(board[14][7], 5));
        
        // South East Corner
        temp.add(new WallNode(board[14][5], 2));
        temp.add(new WallNode(board[14][5], 3));
        temp.add(new WallNode(board[14][5], 4));
        temp.add(new WallNode(board[14][5], 5));
        temp.add(new WallNode(board[14][5], 6));
        temp.add(new WallNode(board[14][5], 7));
        
        // South East Side
        temp.add(new WallNode(board[12][4], 4));
        temp.add(new WallNode(board[12][4], 5));
        temp.add(new WallNode(board[12][4], 6));
        temp.add(new WallNode(board[12][4], 7));
        
        temp.add(new WallNode(board[10][3], 4));
        temp.add(new WallNode(board[10][3], 5));
        temp.add(new WallNode(board[10][3], 6));
        temp.add(new WallNode(board[10][3], 7));
        
        // South Corner
        temp.add(new WallNode(board[8][2], 4));
        temp.add(new WallNode(board[8][2], 5));
        temp.add(new WallNode(board[8][2], 6));
        temp.add(new WallNode(board[8][2], 7));
        temp.add(new WallNode(board[8][2], 8));
        temp.add(new WallNode(board[8][2], 9));
        
        // South West Side
        temp.add(new WallNode(board[6][3], 6));
        temp.add(new WallNode(board[6][3], 7));
        temp.add(new WallNode(board[6][3], 8));
        temp.add(new WallNode(board[6][3], 9));
        
        temp.add(new WallNode(board[4][4], 6));
        temp.add(new WallNode(board[4][4], 7));
        temp.add(new WallNode(board[4][4], 8));
        temp.add(new WallNode(board[4][4], 9));
        
        // South West Corner
        temp.add(new WallNode(board[2][5], 6));
        temp.add(new WallNode(board[2][5], 7));
        temp.add(new WallNode(board[2][5], 8));
        temp.add(new WallNode(board[2][5], 9));
        temp.add(new WallNode(board[2][5], 10));
        temp.add(new WallNode(board[2][5], 11));
        
        // West Side
        temp.add(new WallNode(board[2][7], 8));
        temp.add(new WallNode(board[2][7], 9));
        temp.add(new WallNode(board[2][7], 10));
        temp.add(new WallNode(board[2][7], 11));

        temp.add(new WallNode(board[2][9], 8));
        temp.add(new WallNode(board[2][9], 9));
        temp.add(new WallNode(board[2][9], 10));
        temp.add(new WallNode(board[2][9], 11));

        // North West Corner
        temp.add(new WallNode(board[2][11], 8));
        temp.add(new WallNode(board[2][11], 9));
        temp.add(new WallNode(board[2][11], 10));
        temp.add(new WallNode(board[2][11], 11));
        temp.add(new WallNode(board[2][11], 0));
        temp.add(new WallNode(board[2][11], 1));
        
        // North West Side
        temp.add(new WallNode(board[4][12], 10));
        temp.add(new WallNode(board[4][12], 11));
        temp.add(new WallNode(board[4][12], 0));
        temp.add(new WallNode(board[4][12], 1));

        temp.add(new WallNode(board[6][13], 10));
        temp.add(new WallNode(board[6][13], 11));
        temp.add(new WallNode(board[6][13], 0));
        temp.add(new WallNode(board[6][13], 1));

        wallNodes = temp.toArray(new WallNode[temp.size()]);

        if (GameDriver.TEST_RUN) {
            assertWallNodes();
        }
    }

    void assertWallNodes() {
        for (int i = 0; i < wallNodes.length; i++) {
            final Space wallSpace = locateAdjacent(wallNodes[i].space, wallNodes[i].node);
            assert wallSpace.state == State.Wall : i + "] " + wallNodes[i];
        }
    }
}
