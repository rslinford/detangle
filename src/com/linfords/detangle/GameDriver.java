package com.linfords.detangle;

import com.linfords.detangle.Board.TraceOpenResult;
import com.linfords.detangle.Board.TraceWallResult;

import com.linfords.detangle.Space.State;
import java.io.IOException;

/**
 *
 * @author Scott
 */
public class GameDriver {

    final static boolean TEST_RUN = false;
    final static boolean MULTI_THREADED = true;
    final static float PFACTOR_INITIAL = 1.489f;
    final static float PFACTOR_INCREMENT = 0.012f;

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

        if (MULTI_THREADED) {
            multiThreaded(PFACTOR_INITIAL);
        } else {
            singleThread(PFACTOR_INITIAL);
        }
    }

    static int triangle(final int n) {
        assert n >= 0 : n;
        return n * (n + 1) / 2;
    }

    static int calculatePotential(Board board, GameRecord record) {
        return Board.SEGMENTS_PER_BOARD - board.traceWallPaths(false).totalSegments;
    }

    private static boolean lowPotential(final float pfactor, final GameRecord record, final Board board) {
        final TraceWallResult wallResult = board.traceWallPaths(true);
        final TraceOpenResult openResult = board.traceOpenPaths();
        final int potentialRating = wallResult.longest + openResult.longest;
        final int move = record.tilesPlayed();
        return (move + (int) (move / pfactor)) > potentialRating;
    }

    private void grind(final float pfactor, final int startMove, final boolean multiThreaded, final String previousMark) throws IOException {
        String mark = "unintialized";
        Board board = new Board();
        GameRecord record = new GameRecord(pfactor, startMove);
        record.add(Event.Type.Start, board.current.posX, board.current.posY, board.current.nodeMarker, 0, 0);

        while (!record.isLastGame(multiThreaded)) {
            if (!record.inProgress()) {
                record.rewind(board);

                while (lowPotential(pfactor, record, board)) {
                    record.rewind(board);
                }

            }
            while (board.adjacent.state == State.Playable) {

                final Space playable = board.adjacent;
                int p = 1;

                if (record.pathLength() == 0 && record.gamesCount == 0) {
                    board.adjacent.tile.setRotation(startMove);
                }
                board.play();
                record.add(Event.Type.Play, playable.posX, playable.posY, playable.nodeMarker, playable.tile.getRotation(), record.score() + p);

                while (board.adjacent.state == State.Played) {
                    final Space flowable = board.adjacent;
                    p++;
                    board.flow();
                    record.add(Event.Type.Flow, flowable.posX, flowable.posY, flowable.nodeMarker, flowable.tile.getRotation(), record.score() + p);
                }
            }
            record.add(Event.Type.End, board.adjacent.posX, board.adjacent.posY, board.adjacent.nodeMarker, 0, record.score());

            if (record.isHighScore()) {
                System.out.println(record.toStringSummary() + " HS");
            }
            
            if (record.gamesCount == 1000) {
                mark = record.summaryMarker();
                if (mark.equals(previousMark)){
//                    System.out.println("Marker matches previous. Skipping: " + record.toStringSummary());
                    break;
                }
            }
        }

        if (pfactor < 5) {
            launchThread(pfactor + PFACTOR_INCREMENT, startMove, multiThreaded, mark);
        } else {
            System.out.println("END of the line: " + Thread.currentThread().getName());
        }
    }

    private static void launchThread(final float pfactor, final int startMove, final boolean multiThreaded, final String mark) {
        Thread t = new Thread() {

            @Override
            public void run() {
                try {
                    new GameDriver().grind(pfactor, startMove, multiThreaded, mark);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        };
        t.setName("GameDriver_T" + startMove + "_p" + pfactor);
        t.start();
    }

    private static void multiThreaded(final float pfactor) {
        for (int i = 0; i < 6; i++) {
            launchThread(pfactor, i, true, "");
        }
    }

    private static void singleThread(float pfactor) {
        launchThread(pfactor, 0, false, "");
    }
}
