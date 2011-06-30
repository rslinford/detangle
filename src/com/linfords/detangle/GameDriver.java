package com.linfords.detangle;

import com.linfords.detangle.Board.TraceOpenResult;
import com.linfords.detangle.Board.TraceWallResult;
import com.linfords.detangle.Event.Type;
import com.linfords.detangle.Space.State;

/**
 *
 * @author Scott
 */
public class GameDriver {

    final static boolean TEST_RUN = false;
    final static boolean VERBOSE = false;

    static class Record {

        volatile static int highScore = 0;
        long gamesCount = 0;
        EventStack active = new EventStack();
        final String tag;
        final int startMove;

        Record(final int startMove) {
            this.startMove = startMove;
            this.tag = "<T" + startMove + "> ";
        }

        void add(final Type type, final int posX, final int posY, final int nodeMarker, final int rotation,
                final int score, final TraceWallResult wallResult, final TraceOpenResult openResult) {
            active.push(new Event(type, posX, posY, nodeMarker, rotation, score, wallResult, openResult));
        }

        String toStringDetail() {
            StringBuilder sb = new StringBuilder();
            for (Event m : active) {
                sb.append(tag).append(m.type).append("{").append(m.posX).append(", ").append(m.posY).append("} r(").append(m.rotation).append(") s(").append(m.score).append("); ");
            }
            return sb.toString();
        }

        String toStringVerbose() {
            StringBuilder sb = new StringBuilder();
            for (Event m : active) {
                sb.append(m).append('\n');
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
            if ((gamesCount % 1_000_000) == 0) {
                System.out.println(toStringSummary());
//                System.out.println(toStringVerbose());
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
        
        private int potentialRating() {
            final Event recent = active.peek();
            return recent.wallResult.longest + recent.openResult.longest;
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
            if (startMove != 0) {
                return;
            }
            switch ((int) gamesCount) {
                case 549:
                    assert toStringSummary().equals("<T0> 549] >000100013-0--------0010-101010-100-011301-------00---422--------5-----------| length(76) moves(35) score(252)") : toStringSummary();
                    break;
                case 144349:
                    assert toStringSummary().equals("<T0> 144349] >000100013-0--------0010-101010-103-014450----------50----24-----------10---------------| length(87) moves(35) score(378)") : toStringSummary();
                    break;
            }
        }
    }

    static int triangle(final int n) {
        assert n >= 0 : n;
        return n * (n + 1) / 2;
    }

    static int calculatePotential(Board board, Record record) {
        return Board.SEGMENTS_PER_BOARD - board.traceWallPaths(false).totalSegments;
    }

    private void spinAndChoose(Board board) {
        int maxActivePath = Integer.MIN_VALUE;
        int maxActivePathRotation = 0;
        int minUsedSegments = Integer.MAX_VALUE;
        int minUsedSegmentsRotation = 0;
//        System.out.println("Path scores for {" + board.adjacent.posX + "," + board.adjacent.posY + "} ");
        for (int i = 0; i < 6; i++) {
            if (i > 0) {
                board.adjacent.tile.rotateOne();
            }
            TraceWallResult tr = board.traceWallPaths(true);
//            System.out.println("  r(" + board.adjacent.tile.getRotation() + ") seg(" + tr.totalSegments + ") act(" + tr.activePath.size() + ") go(" + tr.gameOver + ")");
            if (minUsedSegments > tr.totalSegments && !tr.gameOver) {
                minUsedSegments = tr.totalSegments;
                minUsedSegmentsRotation = board.adjacent.tile.getRotation();
            }
            if (maxActivePath < tr.activePath.size()) {
                maxActivePath = tr.activePath.size();
                maxActivePathRotation = board.adjacent.tile.getRotation();
            }
        }

        if (minUsedSegments == Integer.MAX_VALUE) {
            board.adjacent.tile.setRotation(maxActivePathRotation);
//            System.out.println("   selecting max active(" + maxActivePathRotation + ")");
        } else {
            board.adjacent.tile.setRotation(minUsedSegmentsRotation);
//            System.out.println("   selecting min used(" + minUsedSegmentsRotation + ")");
        }
//        System.out.println();
    }

//    private void spinAndChoose2(Board board) {
//        int maxOpenPath = Integer.MIN_VALUE;
//        int maxActivePathRotation = 0;
////        System.out.println("Path scores for {" + board.adjacent.posX + "," + board.adjacent.posY + "} ");
//        for (int i = 0; i < 6; i++) {
//            if (i > 0) {
//                board.adjacent.tile.rotateOne();
//            }
//            final int openLength = board.traceOpenPaths();
////            System.out.println("  r(" + board.adjacent.tile.getRotation() + ") seg(" + tr.totalSegments + ") act(" + tr.activePath.size() + ") go(" + tr.gameOver + ")");
//            if (maxOpenPath < openLength) {
//                maxOpenPath = openLength;
//                maxActivePathRotation = board.adjacent.tile.getRotation();
//            }
//        }
//
//        if (minUsedSegments == Integer.MAX_VALUE) {
//            board.adjacent.tile.setRotation(maxActivePathRotation);
////            System.out.println("   selecting max active(" + maxActivePathRotation + ")");
//        } else {
//            board.adjacent.tile.setRotation(minUsedSegmentsRotation);
////            System.out.println("   selecting min used(" + minUsedSegmentsRotation + ")");
//        }
////        System.out.println();
//    }
    private void grind() {
        grind(0);
    }

    private boolean lowPotential(Record record) {
        final int move = record.tilesPlayed();
        return (move + (int)(move / 3)) > record.potentialRating();
    }

    private void grind(final int startMove) {
        Board board = new Board();
        Record record = new Record(startMove);
        record.add(Event.Type.Start, board.current.posX, board.current.posY, board.current.nodeMarker, 0, 0, board.traceWallPaths(true), board.traceOpenPaths());
        if (VERBOSE) {
            System.out.println(board.current + " (start)");
        }

        while (!record.isLastGame()) {
            if (!record.inProgress()) {
                record.rewind(board);
                
                while (lowPotential(record)) {
                    record.rewind(board);
                }

//                // Too many flowed
//                final int tilePlayed = record.tilesPlayed();
//                if (tilePlayed < 26 && record.score() < 1000) {
//                    while (record.tilesFlowed() > 5) {
//                        record.rewind(board);
//                    }
//                }

            }
            while (board.adjacent.state == State.Playable) {

                final Space playable = board.adjacent;
                int p = 1;
                if (VERBOSE) {
                    System.out.println(playable + " (playing) +" + p);
                }

                if (record.pathLength() == 0) {
                    board.adjacent.tile.setRotation(startMove);
                }
                final TraceWallResult wallPaths = board.traceWallPaths(true);
                final TraceOpenResult openPaths = board.traceOpenPaths();
                board.play();
                record.add(Event.Type.Play, playable.posX, playable.posY, playable.nodeMarker, playable.tile.getRotation(), record.score() + p, wallPaths, openPaths);

                while (board.adjacent.state == State.Played) {
                    final Space flowable = board.adjacent;
                    p++;
                    if (VERBOSE) {
                        System.out.println(flowable + " (flowing) +" + p);
                    }
                    board.flow();
                    record.add(Event.Type.Flow, flowable.posX, flowable.posY, flowable.nodeMarker, flowable.tile.getRotation(), record.score() + p, wallPaths, openPaths);
                }
            }
            record.add(Event.Type.End, board.adjacent.posX, board.adjacent.posY, board.adjacent.nodeMarker, 0, record.score(), board.traceWallPaths(true), board.traceOpenPaths());

            if (VERBOSE) {
                System.out.println(board.adjacent + " (end)");
                System.out.println(record.toStringSummary());
                System.out.println(record.toStringDetail());
                System.out.println();
            } else if (record.isHighScore()) {
                System.out.println(record.toStringSummary());
                System.out.println(record.toStringDetail());
//                System.out.println(record.toStringVerbose());
                System.out.println();
            }
        }
    }

    private static void multiThreaded() {
        for (int i = 0; i < 6; i++) {
            final int startMove = i;
            new Thread() {

                @Override
                public void run() {
                    new GameDriver().grind(startMove);
                }
            }.start();
        }
    }

    private static void singleThread() {
        new GameDriver().grind();
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

        multiThreaded();
    }
}
