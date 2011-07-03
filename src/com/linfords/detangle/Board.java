package com.linfords.detangle;

import com.linfords.detangle.Space.State;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Scott
 */
final class Board {

    final static boolean VERBOSE = false;
    final static int SEGMENTS_PER_BOARD = 6 * 36;
    final static int OPTIMAL_WALL_SEGMENTS = (12 + 84) / 2;
    final static int SEGMENTS_PER_BOARD_WALLED_OPTIMALY = SEGMENTS_PER_BOARD - OPTIMAL_WALL_SEGMENTS;
    final static int THEORETICAL_MAX_FINAL_MOVE = SEGMENTS_PER_BOARD - (36 - 1) - (OPTIMAL_WALL_SEGMENTS - 1);
    private final Space[][] board = new Space[18][18];
    private final TileStack tiles;
    private Tile swapTile;
    Space current;
    Space adjacent;
    private List<Node> wallNodes = new ArrayList();

    Board() throws IOException {
        this.tiles = new TileStack();
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
    }

    void play() {
        play(null);
    }

    void play(Integer rotation) {
        adjacent.state = State.Played;
        if (rotation != null) {
            adjacent.tile.setRotation(rotation);
        }
        advance();
    }

    void flow() {
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
        final Space newAdjacent = board[adjacentPosX][adjacentPosY];
        if (!newAdjacent.equals(adjacent)) {
            if (adjacent.state == adjacent.state.Playable) {
                adjacent.state = State.Covered;
                adjacent.tile = null;
                tiles.unpop();
            }
        }
        adjacent = newAdjacent;
        adjacent.nodeMarker = Tile.adjacentNode(currentMarker);
        adjacent.tile.setRotation(rotation);
        adjacent.state = Space.State.Playable;
    }

    class Node {

        final Space space;
        final int node;

        public Node(final Space space, final int node) {
            this.space = space;
            this.node = node;
        }

        Node connected() {
            return new Node(space, space.tile.connectedNode(node));
        }

        private Node adjacent() {
            return new Node(locateAdjacent(space, node), Tile.adjacentNode(node));
        }

        @Override
        public String toString() {
            return "{" + space.posX + "," + space.posY + "}n(" + node + ")";
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Node)) {
                return false;
            }
            Node wn = (Node) obj;
            if (this.node != wn.node || this.space.posX != wn.space.posX || this.space.posY != wn.space.posY) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 59 * hash + this.space.posX;
            hash = 59 * hash + this.space.posY;
            hash = 59 * hash + this.node;
            return hash;
        }
    }
    Node startNode = null;

    void initWallNodes() {
        // Nodes around the center tile. Clockwise.
        startNode = new Node(board[8][10], 7);
        wallNodes.add(startNode); // the active path starts here
        wallNodes.add(new Node(board[8][10], 6));
        wallNodes.add(new Node(board[10][9], 8));
        wallNodes.add(new Node(board[10][9], 9));
        wallNodes.add(new Node(board[10][7], 10));
        wallNodes.add(new Node(board[10][7], 11));
        wallNodes.add(new Node(board[8][6], 0));
        wallNodes.add(new Node(board[8][6], 1));
        wallNodes.add(new Node(board[6][7], 2));
        wallNodes.add(new Node(board[6][7], 3));
        wallNodes.add(new Node(board[6][9], 4));
        wallNodes.add(new Node(board[6][9], 5));
        // Top Corner
        wallNodes.add(new Node(board[8][14], 10));
        wallNodes.add(new Node(board[8][14], 11));
        wallNodes.add(new Node(board[8][14], 0));
        wallNodes.add(new Node(board[8][14], 1));
        wallNodes.add(new Node(board[8][14], 2));
        wallNodes.add(new Node(board[8][14], 3));
        // North East Side
        wallNodes.add(new Node(board[10][13], 0));
        wallNodes.add(new Node(board[10][13], 1));
        wallNodes.add(new Node(board[10][13], 2));
        wallNodes.add(new Node(board[10][13], 3));
        wallNodes.add(new Node(board[12][12], 0));
        wallNodes.add(new Node(board[12][12], 1));
        wallNodes.add(new Node(board[12][12], 2));
        wallNodes.add(new Node(board[12][12], 3));
        // North East Corner
        wallNodes.add(new Node(board[14][11], 0));
        wallNodes.add(new Node(board[14][11], 1));
        wallNodes.add(new Node(board[14][11], 2));
        wallNodes.add(new Node(board[14][11], 3));
        wallNodes.add(new Node(board[14][11], 4));
        wallNodes.add(new Node(board[14][11], 5));
        // East Side
        wallNodes.add(new Node(board[14][9], 2));
        wallNodes.add(new Node(board[14][9], 3));
        wallNodes.add(new Node(board[14][9], 4));
        wallNodes.add(new Node(board[14][9], 5));
        wallNodes.add(new Node(board[14][7], 2));
        wallNodes.add(new Node(board[14][7], 3));
        wallNodes.add(new Node(board[14][7], 4));
        wallNodes.add(new Node(board[14][7], 5));
        // South East Corner
        wallNodes.add(new Node(board[14][5], 2));
        wallNodes.add(new Node(board[14][5], 3));
        wallNodes.add(new Node(board[14][5], 4));
        wallNodes.add(new Node(board[14][5], 5));
        wallNodes.add(new Node(board[14][5], 6));
        wallNodes.add(new Node(board[14][5], 7));
        // South East Side
        wallNodes.add(new Node(board[12][4], 4));
        wallNodes.add(new Node(board[12][4], 5));
        wallNodes.add(new Node(board[12][4], 6));
        wallNodes.add(new Node(board[12][4], 7));
        wallNodes.add(new Node(board[10][3], 4));
        wallNodes.add(new Node(board[10][3], 5));
        wallNodes.add(new Node(board[10][3], 6));
        wallNodes.add(new Node(board[10][3], 7));
        // South Corner
        wallNodes.add(new Node(board[8][2], 4));
        wallNodes.add(new Node(board[8][2], 5));
        wallNodes.add(new Node(board[8][2], 6));
        wallNodes.add(new Node(board[8][2], 7));
        wallNodes.add(new Node(board[8][2], 8));
        wallNodes.add(new Node(board[8][2], 9));
        // South West Side
        wallNodes.add(new Node(board[6][3], 6));
        wallNodes.add(new Node(board[6][3], 7));
        wallNodes.add(new Node(board[6][3], 8));
        wallNodes.add(new Node(board[6][3], 9));
        wallNodes.add(new Node(board[4][4], 6));
        wallNodes.add(new Node(board[4][4], 7));
        wallNodes.add(new Node(board[4][4], 8));
        wallNodes.add(new Node(board[4][4], 9));
        // South West Corner
        wallNodes.add(new Node(board[2][5], 6));
        wallNodes.add(new Node(board[2][5], 7));
        wallNodes.add(new Node(board[2][5], 8));
        wallNodes.add(new Node(board[2][5], 9));
        wallNodes.add(new Node(board[2][5], 10));
        wallNodes.add(new Node(board[2][5], 11));
        // West Side
        wallNodes.add(new Node(board[2][7], 8));
        wallNodes.add(new Node(board[2][7], 9));
        wallNodes.add(new Node(board[2][7], 10));
        wallNodes.add(new Node(board[2][7], 11));
        wallNodes.add(new Node(board[2][9], 8));
        wallNodes.add(new Node(board[2][9], 9));
        wallNodes.add(new Node(board[2][9], 10));
        wallNodes.add(new Node(board[2][9], 11));
        // North West Corner
        wallNodes.add(new Node(board[2][11], 8));
        wallNodes.add(new Node(board[2][11], 9));
        wallNodes.add(new Node(board[2][11], 10));
        wallNodes.add(new Node(board[2][11], 11));
        wallNodes.add(new Node(board[2][11], 0));
        wallNodes.add(new Node(board[2][11], 1));
        // North West Side
        wallNodes.add(new Node(board[4][12], 10));
        wallNodes.add(new Node(board[4][12], 11));
        wallNodes.add(new Node(board[4][12], 0));
        wallNodes.add(new Node(board[4][12], 1));
        wallNodes.add(new Node(board[6][13], 10));
        wallNodes.add(new Node(board[6][13], 11));
        wallNodes.add(new Node(board[6][13], 0));
        wallNodes.add(new Node(board[6][13], 1));
    }

    static class Potential {

        int wastedSegments = 0;
        int longestFringe = 0;
    }

    List<Node> tracePath(final Node start, boolean includePlayable) {
        final List<Node> path = new ArrayList();
        for (Node node1 = start; (node1.space.state == Space.State.Played)
                || (includePlayable && node1.space.state == Space.State.Playable);) {
            final Node node2 = node1.connected();
            path.add(node1);
            path.add(node2);
            node1 = node2.adjacent();
        }
        return path;
    }

    List<Node> openNodeForSpace(Space s) {
        List<Node> list = new ArrayList();
        for (int i = 0; i < Tile.NODE_QTY; i++) {
            Space sa = locateAdjacent(s, i);
            if (sa.state == State.Covered) {
                list.add(new Node(s, i));
            }
        }
        return list;
    }

    static class TraceOpenResult {

        int total = 0;
        int longest = 0;

        @Override
        public String toString() {
            return "totalOpen(" + total + ") longestOpen(" + longest + ")";
        }
    }

    TraceOpenResult traceOpenPaths() {
        List<Node> nodes = new ArrayList();
        for (int x = 0; x < board.length; x++) {
            for (int y = 0; y < board[x].length; y++) {
                switch (board[x][y].state) {
                    case Played:
                    case Playable:
                        nodes.addAll(openNodeForSpace(board[x][y]));
                        break;
                }
            }
        }
        TraceOpenResult result = new TraceOpenResult();
        while (!nodes.isEmpty()) {
            Node n = nodes.remove(0);
            final List<Node> path = tracePath(n, true);
            // Don't count dead-end paths
            if (path.get(path.size() - 1).adjacent().space.state == State.Wall) {
                continue;
            }
            final int segments = path.size() / 2;
            if (result.longest < segments) {
                result.longest = segments;
            }
            result.total += segments;
        }
        return result;
    }

    static class TraceWallResult {

        List<Node> activePath = Collections.EMPTY_LIST;
        boolean gameOver = false;
        int totalSegments = 0;
        int longest = 0;

        @Override
        public String toString() {
            return "totalWall(" + totalSegments + ") longestWall(" + longest + ")";
        }
    }

    TraceWallResult traceWallPaths(boolean includePlayable) {
        int pathCount = 0;
        TraceWallResult result = new TraceWallResult();
        for (List<Node> wn = new ArrayList(wallNodes); !wn.isEmpty();) {
            Node n = wn.remove(0);
            List<Node> path = tracePath(n, includePlayable);
            if (startNode.equals(n)) {
                result.activePath = path;
            }
            if (path.isEmpty()) {
                continue;
            }
            final int segments = path.size() / 2;
            result.totalSegments += segments;
            pathCount++;
            if (VERBOSE) {
                System.out.print("segments(" + segments + ") " + path);
            }
            Node last = path.get(path.size() - 1);
            Node beyond = last.adjacent();
            switch (beyond.space.state) {
                case Covered:
                case Playable:
                    if (startNode.equals(n)) {
                        result.gameOver = false;
                    } else if (result.longest < segments) {
                        result.longest = segments;
                    }
                    if (VERBOSE) {
                        System.out.println(" (open)");
                    }
                    break;
                case Wall:
                    if (startNode.equals(n)) {
                        result.gameOver = true;
                    }
                    wn.remove(last);
                    if (VERBOSE) {
                        System.out.println(" (closed)");
                    }
                    break;
                case Played:
                    assert false;
            }
        }
        return result;
    }
}
