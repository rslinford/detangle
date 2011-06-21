package com.linfords.detangle;

import java.util.Arrays;

/**
 *
 * @author Scott
 */
class Tile {

    private static int NEXT_ID = 0;
    static final int SIDE_QTY = 6;
    static final int NODE_QTY = SIDE_QTY * 2;
    final int[] connections;
    private int rotation = 0;
    final int id;

    Tile(int[] connections) {
        this.connections = connections;
        this.id = NEXT_ID;
        NEXT_ID++;
    }

    void rotateOne() {
        rotation = (rotation + 1) % SIDE_QTY;
    }

    void setRotation(int rotation) {
        this.rotation = rotation % SIDE_QTY;
    }

    int getRotation() {
        return rotation;
    }

    void resetRotation() {
        rotation = 0;
    }

    private int adjustForInternal(final int externalNode) {
        return ((Tile.NODE_QTY + externalNode) - (rotation * 2)) % Tile.NODE_QTY;
    }

    private int adjustForExternal(final int internalNode) {
        return (internalNode + (rotation * 2)) % Tile.NODE_QTY;
    }

    /** Mapping function for node connections within a given tile. */
    int connectedNode(final int node) {
        final int internalNode = adjustForInternal(node);
        // Follow the path on the adjacent tile.
        for (int i = 0; i < NODE_QTY; i++) {
            if (internalNode == connections[i]) {
                boolean even = i % 2 == 0;
                if (even) {
                    return adjustForExternal(connections[i + 1]);
                } else {
                    return adjustForExternal(connections[i - 1]);
                }
            }
        }
        throw new IllegalArgumentException("Invalid node(" + node + ") for tile " + this);
    }

    /** Mapping function for touching nodes from adjacent tiles. */
    static int adjacentNode(int node) {
        switch (node) {
            case 0:
                return 7;
            case 1:
                return 6;
            case 2:
                return 9;
            case 3:
                return 8;
            case 4:
                return 11;
            case 5:
                return 10;
            case 6:
                return 1;
            case 7:
                return 0;
            case 8:
                return 3;
            case 9:
                return 2;
            case 10:
                return 5;
            case 11:
                return 4;
            default:
                throw new IllegalArgumentException("Invalid node(" + node + ")");
        }
    }

    @Override
    public String toString() {
        return id + "] " + Arrays.toString(connections);
    }
}
