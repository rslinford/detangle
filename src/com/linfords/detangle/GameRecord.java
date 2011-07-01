package com.linfords.detangle;

import com.linfords.detangle.Event.Type;

class GameRecord {

    volatile static int highScore = 0;
    long gamesCount = 0;
    EventStack active = new EventStack();
    final String tag;
    final int startMove;

    GameRecord(final int startMove) {
        this.startMove = startMove;
        this.tag = "<T" + startMove + "> ";
    }

    void add(final Type type, final int posX, final int posY, final int nodeMarker, final int rotation, final int score) {
        active.push(new Event(type, posX, posY, nodeMarker, rotation, score));
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

    boolean isLastGame(final boolean multiThreaded) {
        if (multiThreaded && active.size() > 1) {
            if (active.get(1).rotation != startMove) { // true when thread has moved into another thread's chunk
                return true;
            }
        }

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
        if (GameDriver.TEST_RUN) {
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

    int tilesFlowed() {
        int length = 0;
        for (Event e : active) {
            if (e.type == Event.Type.Flow) {
                length++;
            }
        }
        return length;
    }

    int tilesPlayed() {
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

    int score() {
        return active.peek().score;
    }

    int size() {
        return active.size();
    }

    String rotationSequence() {
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

    boolean isHighScore() {
        return score() > highScore;
    }

    /** Validate results against a known data set: TILE_SET_TEST_DATA */
    void validateRecord() {
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
