package detangle;

import detangle.Space.Coordinates;
import detangle.Space.SpaceState;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Scott
 */
class Board {

    private final Map<Space.Coordinates, Space> board = new HashMap();
    private final TileIterator tiles = new TileIterator();
    private final Space.Coordinates startPos = new Space.Coordinates(0, 0);
    private Tile swapTile;
    Space current;
    Space adjacent;

    Board() {
        this.swapTile = tiles.next();
        this.current = locateSpace(startPos);
        this.adjacent = locateSpace(calculateAdjacentCoordinates(current));
        this.adjacent.matchMarker(this.current);
    }

    private static Space.Coordinates calculateAdjacentCoordinates(Space space) {
        switch (space.marker) {
            case 0:
            case 1:
                return new Space.Coordinates(space.pos.x, space.pos.y - 1);
            case 2:
            case 3:
                return new Space.Coordinates(space.pos.x + 1, space.pos.y - 0.5f);
            case 4:
            case 5:
                return new Space.Coordinates(space.pos.x + 1, space.pos.y + 0.5f);
            case 6:
            case 7:
                return new Space.Coordinates(space.pos.x, space.pos.y + 1);
            case 8:
            case 9:
                return new Space.Coordinates(space.pos.x - 1, space.pos.y + 0.5f);
            case 10:
            case 11:
                return new Space.Coordinates(space.pos.x - 1, space.pos.y - 0.5f);
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
                space.state = SpaceState.Wall;
                space.marker = 0;
            } else if (outOfBound(pos)) {
                space.state = SpaceState.Wall;
            } else {
                space.state = SpaceState.Playable;
                space.tile = tiles.next();
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
        if (adjacent.state != SpaceState.Playable) {
            throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
        }

        adjacent.state = SpaceState.Played;
        advance();
    }

    void flow() {
        if (adjacent.state != SpaceState.Played) {
            throw new IllegalStateException("SpaceState(" + adjacent.state + ")");
        }

        advance();
    }

    private boolean outOfBound(Coordinates pos) {
        if (Math.abs(pos.x) > 3) {
            return true;
        }
        
        if (Math.abs(pos.y) > Math.abs(3 - (0.5f * pos.x))) {
            return true;
        }

        return false;
    }
}
