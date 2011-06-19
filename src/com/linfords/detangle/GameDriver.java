package com.linfords.detangle;

import com.linfords.detangle.Space.State;
import java.util.Arrays;

/**
 *
 * @author Scott
 */
public class GameDriver {

    final static boolean TEST_RUN = false;
    final static boolean VERBOSE = false;

    static class Record {

        int highScore = 0;
        long gamesCount = 0;
        EventStack active = new EventStack();
        final String tag;
        
        Record(String tag) {
            this.tag = tag;
        }

        void add(final Event.Type type, final int posX, final int posY, final int marker, final int rotation, final int score, final int potential) {
            active.push(new Event(type, posX, posY, marker, rotation, score, potential));
        }

        String toStringDetail() {
            StringBuilder sb = new StringBuilder();
            for (Event m : active) {
                sb.append(tag).append(m.type).append("{").append(m.posX).append(", ").append(m.posY).append("} r(").append(m.rotation).append(") s(").append(m.score).append("); ");
            }
            return sb.toString();
        }

        String toStringSummary() {
            return tag + gamesCount + "] " + rotationSequence() + " length(" + pathLength() + ") moves(" + tilesPlayed() + ") score(" + score() + ")";
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
                switch (m.type) {
                    case Play:
                    case Flow:
                        if (m.rotation < (Tile.SIDE_QTY - 1)) {
                            return false;
                        }
                        break;
                    case Start:
                    case End:
                        break;
                }
            }
            return true;
        }

        void rewind(Board board) {
            if (TEST_RUN) {
                validateRecord();
            }
            if ((gamesCount % 2_000_000_000) == 0) {
                System.out.println(toStringSummary());
            }
            gamesCount++;
            if (score() > highScore) {
                highScore = score();
            }
            rewind:
            while (active.size() > 1) {
                final Event e = active.pop();
                switch (e.type) {
                    case End:
                    case Flow:
                        // Nothing to do other than the pop that has already been performed.
                        break;
                    case Play:
                        //Event played = active.pop();
                        final int r = e.rotation + 1;
                        if (r == Tile.SIDE_QTY) {
                            if (active.peek().type == Event.Type.Start) {
                                break rewind;
                            }
                            board.putTileBack(e.posX, e.posY);
                        } else {
                            board.undoPlay(active.peek().posX, active.peek().posY, active.peek().marker, e.posX, e.posY, r);
                            break rewind;
                        }
                        break;
                    case Start:
                    default:
                        throw new IllegalStateException("Rewound down to " + active.peek().type + " size: " + active.size());
                }
            }
        }

        int pathLength() {
            int length = active.size() - 1;
            if (active.peek().type == Event.Type.End) {
                length--;
            }
            return length < 0 ? 0 : length;
        }

        private int tilesFlowed() {
            int length = 0;
            for (Event e : active) {
                if (e.type == Event.Type.Flow) {
                    length++;
                }
            }
            return length;
        }

        private int tilesPlayed() {
            int length = 0;
            for (Event e : active) {
                if (e.type == Event.Type.Play) {
                    length++;
                }
            }
            return length;
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
                switch (m.type) {
                    case Flow:
                        sb.append('-');
                        break;
                    case Play:
                        sb.append(m.rotation);
                        break;
                    case Start:
                        sb.append(">");
                        break;
                    case End:
                        sb.append("|");
                        break;
                }
            }
            return sb.toString();
        }

        private boolean isHighScore() {
            return score() > highScore;
        }

        /** Validate results against a known data set: TILE_SET_TEST_DATA */
        private void validateRecord() {
            switch ((int) gamesCount) {
                case 549:
//                    assert toStringSummary().equals("549] >000100013-0--------0010-101010-100-011301-------00---422--------5-----------| length(76) score(252)") : toStringSummary();
                    break;
                case 144349:
//                    assert toStringSummary().equals("144349] >000100013-0--------0010-101010-103-014450----------50----24-----------10---------------| length(87) score(378)") : toStringSummary();
                    break;
            }
        }
    }

    static int triangle(final int n) {
        assert n >= 0 : n;
        return n * (n + 1) / 2;
    }

    static int calculatePotential(Board board, Record record) {
        return Board.SEGMENTS_PER_BOARD - board.traceWallPaths() + record.tilesPlayed();
    }

    private void grind(int[] startingMoves) {
        final String ttag = "<" + Arrays.toString(startingMoves) + "> ";
        Board board = new Board();
        Record record = new Record(ttag);
        record.add(Event.Type.Start, board.current.posX, board.current.posY, board.current.nodeMarker, 0, 0, -1);
        if (VERBOSE) {
            System.out.println(board.current + " (start)");
        }

        int startingIndex = 0;
        while (!record.isLastGame()) {
            if (!record.inProgress()) {
                record.rewind(board);
            }
            int potential = -1;
            while (board.adjacent.state == State.Playable) {
                if (record.score() < 1000 && record.tilesFlowed() > 7) {
//                    System.out.println(record.toStringSummary() + "(flow back)");
                    while (record.tilesFlowed() > 0) {
                        record.rewind(board);
                    }
                }

                final Space playable = board.adjacent;
                int p = 1;
                if (VERBOSE) {
                    System.out.println(playable + " (playing) +" + p);
                }
                if (startingIndex < startingMoves.length) {
                    board.play(startingMoves[startingIndex++]);
                } else {
                    board.play();
                }
                record.add(Event.Type.Play, playable.posX, playable.posY, playable.nodeMarker, playable.tile.getRotation(), record.score() + p, potential);
                while (board.adjacent.state == State.Played) {
                    final Space flowable = board.adjacent;
                    p++;
                    if (VERBOSE) {
                        System.out.println(flowable + " (flowing) +" + p);
                    }
                    board.flow();
                    record.add(Event.Type.Flow, flowable.posX, flowable.posY, flowable.nodeMarker, flowable.tile.getRotation(), record.score() + p, potential);
                }
            }
            record.add(Event.Type.End, board.adjacent.posX, board.adjacent.posY, board.adjacent.nodeMarker, 0, record.score(), potential);
            if (VERBOSE) {
                System.out.println(board.adjacent + " (end)");
                System.out.println(record.toStringSummary());
                System.out.println(record.toStringDetail());
                System.out.println();
            } else if (record.isHighScore()) {
                System.out.println(record.toStringSummary());
                System.out.println(record.toStringDetail());
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        // Assert that assertions are enabled.
        if (TEST_RUN) {
            try {
                assert false;
                throw new IllegalStateException("Assertions are not enabled. Test run would give a false OK.");
            } catch (AssertionError e) {
                // Expected error
            }
        }

        final int tc = Integer.parseInt(args[0]);
        assert tc > 0 : tc;
        assert tc < 101 : tc;

        int i = 0;
        stopalready:
        for (int c3 = 0; c3 < 6; c3++) {
            for (int c2 = 0; c2 < 6; c2++) {
                for (int c1 = 0; c1 < 6; c1++) {
                    final int d3 = c3;
                    final int d2 = c2;
                    final int d1 = c1;

                    new Thread() {

                        @Override
                        public void run() {
                            new GameDriver().grind(new int[]{d1, d2, d3});
                        }
                    }.start();
                    i++;
                    if (i >= tc) {
                        break stopalready;
                    }
                }
            }
        }
    }
}
