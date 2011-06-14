package detangle;

import detangle.Space.Coordinates;
import detangle.Space.State;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Scott
 */
class Board {

    private final Map<Space.Coordinates, Space> board = new HashMap();
    private final TileStack tiles = new TileStack();
    private final Space.Coordinates startPos = new Space.Coordinates(0, 0);
    private Tile swapTile;
    Space current;
    Space adjacent;

    Board() {
        this.swapTile = tiles.pop();
        this.current = locateSpace(startPos);
        this.adjacent = locateSpace(calculateAdjacentCoordinates(current));
        this.adjacent.matchMarker(this.current);
    }

    private static Space.Coordinates calculateAdjacentCoordinates(Space space) {
        switch (space.marker) {
            case 0:
            case 1:
                return new Space.Coordinates(space.pos.x, space.pos.y + 1);
            case 2:
            case 3:
                return new Space.Coordinates(space.pos.x + 1, space.pos.y + 0.5f);
            case 4:
            case 5:
                return new Space.Coordinates(space.pos.x + 1, space.pos.y - 0.5f);
            case 6:
            case 7:
                return new Space.Coordinates(space.pos.x, space.pos.y - 1);
            case 8:
            case 9:
                return new Space.Coordinates(space.pos.x - 1, space.pos.y - 0.5f);
            case 10:
            case 11:
                return new Space.Coordinates(space.pos.x - 1, space.pos.y + 0.5f);
            default:
                throw new IllegalArgumentException("Space token(" + space.marker + ")");
        }
    }

    private Space locateSpace(Space.Coordinates pos) {
        Space space = board.get(pos);
        if (space == null) {
            space = new Space(pos);
            board.put(pos, space);
            if ((pos.x == startPos.x) && (pos.y == startPos.y)) {
                space.state = State.Wall;
                space.marker = 0;
            } else if (outOfBound(pos)) {
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
        adjacent = locateSpace(calculateAdjacentCoordinates(current));
        adjacent.matchMarker(current);
    }

    void play() {
//        if (adjacent.state != State.Playable) {
//            throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
//        }

        adjacent.state = State.Played;
        advance();
    }

    void flow() {
//        if (adjacent.state != State.Played) {
//            throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
//        }

        advance();
    }

    private boolean outOfBound(final Coordinates pos) {
        final int x = Math.abs((int) pos.x);
        if (x > 3) {
            return true;
        }

        final float y = Math.abs(pos.y);

        switch (x) {
            case 0:
                return y > 3;
            case 1:
                return y > 2.5;
            case 2:
                return y > 2;
            case 3:
                return y > 1.5;
        }

        return false;
    }

    void putTileBack(final Coordinates pos) {
        board.remove(pos);
        tiles.unpop();
    }

    /** Undo play on adjacentPos and set its rotation for another try. */
    void undoPlay(final Coordinates currentPos, final int currentMarker,
            final Coordinates adjacentPos, final int rotation) {
        current = locateSpace(currentPos);
        current.marker = currentMarker;
        adjacent = locateSpace(adjacentPos);
        adjacent.marker = Tile.adjacentNode(currentMarker);
        adjacent.tile.setRotation(rotation);
        adjacent.state = Space.State.Playable;
    }
}
