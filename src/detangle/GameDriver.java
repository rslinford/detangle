package detangle;

import detangle.GameDriver.Event.Type;
import detangle.Space.State;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 * @author Scott
 */
public class GameDriver {

    static class Record {

        int highScore = 0;
        long gameCount = 0;
        Stack<Event> active = new Stack();
        Map<Integer, List<Event>> goodGames = new TreeMap();
        private final static int GOOD_GAME_THRESHOLD = 1000;

        void add(final Event.Type type, final Space.Coordinates pos, final int marker, final int rotation, final int score) {
            active.push(new Event(type, pos, marker, rotation, score));
        }

        String toStringHuman() {
            StringBuilder sb = new StringBuilder();
            for (Event m : active) {
                sb.append(m.type).append(m.pos).append(" r(").append(m.rotation).append(") s(").append(m.score).append("); ");
            }
            return sb.toString();
        }

        boolean inProgress() {
            return active.peek().type != Event.Type.End;
        }

        boolean isLastGame() {
            // Have to play the game out to know for sure.
            if (inProgress()) {
                return false;
            }

            for (Event m : active) {
                if (m.rotation != (Tile.SIDE_QTY - 1)) {
                    return false;
                }
            }

            return true;
        }

        void rewind(Board board) {
            gameCount++;
            // copy record before rewind
            if (score() > GOOD_GAME_THRESHOLD) {
                goodGames.put(score(), new ArrayList(active));
            }
            
            if (score() > highScore) {
                highScore = score();
            }

            while (active.size() > 1) {
                Event e1;
                do {
                    e1 = active.pop();
                } while (e1.type != Event.Type.Play);

                final int r = e1.rotation + 1;

                if (r == Tile.SIDE_QTY) {
                    board.putTileBack(e1.pos);
                } else {
                    Event e2 = active.peek();
                    board.undoPlay(e2.pos, e2.marker, e1.pos, r);
                    break;
                }
            }
        }

        @Override
        public String toString() {
            return active.toString();
        }

        private int score() {
            return active.peek().score;
        }

        private int size() {
            return active.size();
        }

        private String rotationSequence() {
            StringBuilder sb = new StringBuilder();
            for (Event m : active) {
                if (m.type == Event.Type.Play) {
                    sb.append(m.rotation);
                }
            }
            return sb.toString();
        }

        private boolean isHighScore() {
            return score() > highScore;
        }
    }

    static class Event {

        enum Type {

            Start, Play, Flow, End
        }
        final Type type;
        final Space.Coordinates pos;
        final int marker;
        final int rotation;
        final int score;

        Event(final Type type, final Space.Coordinates pos, final int marker, final int rotation, final int score) {
            this.type = type;
            this.pos = pos;
            this.marker = marker;
            this.rotation = rotation;
            this.score = score;
        }

        @Override
        public String toString() {
            return "played(" + pos + ") rotation(" + rotation + ") score(" + score + ")";
        }
    }

    private void grind() {
        Board board = new Board();
        Record record = new Record();
        record.add(Event.Type.Start, board.current.pos, board.current.marker, 0, 0);
//        System.out.println(board.current + " (start)");

        while (!record.isLastGame()) {
            if (!record.inProgress()) {
                record.rewind(board);
            }

            while (board.adjacent.state == State.Playable) {
                final Space playable = board.adjacent;
                int p = 1;
//                System.out.println(playable + " (playing) +" + p);

                board.play();
                record.add(Event.Type.Play, playable.pos, playable.marker, playable.tile.getRotation(), record.score() + p);
                while (board.adjacent.state == State.Played) {
                    final Space flowable = board.adjacent;
                    p++;
//                    System.out.println(flowable + " (flowing) +" + p);
                    board.flow();
                    record.add(Event.Type.Flow, flowable.pos, flowable.marker, flowable.tile.getRotation(), record.score() + p);
                }
            }

            record.add(Event.Type.End, board.adjacent.pos, board.adjacent.marker, 0, record.score());

//            System.out.println(board.adjacent + " (end)");
            if (record.isHighScore()) {
                System.out.println(record.highScore + "] " + record.rotationSequence() + " score(" + record.score() + ") length(" + record.size() + ")");
                System.out.println("Path: " + record.toStringHuman());
                System.out.println();
            }
            else if ((record.gameCount % 100000) == 0) {
                System.out.println(record.highScore + "] " + record.rotationSequence() + " score(" + record.score() + ") length(" + record.size() + ")");
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        new GameDriver().grind();
    }
}
