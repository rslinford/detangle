package detangle;

import java.util.Arrays;

/**
 *
 * @author Scott
 */
class Tile {

    static final int NODE_QTY = 12;
    private final int[] connections;
    private static int NEXT_ID = 0;
    final int id;

    Tile(int[] connections) {
        this.connections = connections;
        this.id = NEXT_ID;
        NEXT_ID++;
    }

    /** Mapping function for node connections within a given tile. */
    int connectingNode(int node) {
        // Follow the path on the adjacent tile.
        for (int i = 0; i < NODE_QTY; i++) {
            if (node == connections[i]) {
                boolean even = i % 2 == 0;
                if (even) {
                    return connections[i + 1];
                } else {
                    return connections[i - 1];
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
