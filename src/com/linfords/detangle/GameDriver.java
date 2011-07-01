package com.linfords.detangle;

import com.linfords.detangle.Board.TraceOpenResult;
import com.linfords.detangle.Board.TraceWallResult;

import com.linfords.detangle.Space.State;

/**
 *
 * @author Scott
 */
public class GameDriver {

    final static boolean TEST_RUN = false;
    final static boolean VERBOSE = false;

    static int triangle(final int n) {
        assert n >= 0 : n;
        return n * (n + 1) / 2;
    }

    static int calculatePotential(Board board, GameRecord record) {
        return Board.SEGMENTS_PER_BOARD - board.traceWallPaths(false).totalSegments;
    }

    private static boolean lowPotential1(final GameRecord record, final Board board) {
        final TraceWallResult wallResult = board.traceWallPaths(true);
        final TraceOpenResult openResult = board.traceOpenPaths();
        final int potentialRating = wallResult.longest + openResult.longest;
        final int move = record.tilesPlayed();
        return (move + (int) (move / 3.5)) > potentialRating;
    }

    private void grind() {
        grind(0, false);
    }

    private void grind(final int startMove, final boolean multiThreaded) {
        Board board = new Board();
        GameRecord record = new GameRecord(startMove);
        record.add(Event.Type.Start, board.current.posX, board.current.posY, board.current.nodeMarker, 0, 0);
        if (VERBOSE) {
            System.out.println(board.current + " (start)");
        }

        while (!record.isLastGame(multiThreaded)) {
            if (!record.inProgress()) {
                record.rewind(board);

                while (lowPotential1(record, board)) {
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

                if (record.pathLength() == 0 && record.gamesCount == 0) {
                    board.adjacent.tile.setRotation(startMove);
                }
                board.play();
                record.add(Event.Type.Play, playable.posX, playable.posY, playable.nodeMarker, playable.tile.getRotation(), record.score() + p);

                while (board.adjacent.state == State.Played) {
                    final Space flowable = board.adjacent;
                    p++;
                    if (VERBOSE) {
                        System.out.println(flowable + " (flowing) +" + p);
                    }
                    board.flow();
                    record.add(Event.Type.Flow, flowable.posX, flowable.posY, flowable.nodeMarker, flowable.tile.getRotation(), record.score() + p);
                }
            }
            record.add(Event.Type.End, board.adjacent.posX, board.adjacent.posY, board.adjacent.nodeMarker, 0, record.score());

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
        System.out.println();
        System.out.println("END Thread " + Thread.currentThread().getName());
    }

    private static void multiThreaded() {
        for (int i = 0; i < 6; i++) {
            final int startMove = i;
            Thread t = new Thread() {

                @Override
                public void run() {
                    new GameDriver().grind(startMove, true);
                }
            };
            t.setName("GameDriver_" + i);
            t.start();
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
