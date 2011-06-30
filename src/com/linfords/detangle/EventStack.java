package com.linfords.detangle;

import com.linfords.detangle.Board.TraceOpenResult;
import com.linfords.detangle.Board.TraceWallResult;
import java.util.Iterator;

/**
 *
 * @author Scott
 */
class EventStack implements Iterable<Event> {

    /** Max possible path size in a perfect game. Plus 2 for start and end caps. */
    final static int THEORETICAL_MAX_PATH = 169 + 2;
    private int size = 0;
    private final Event[] events = new Event[300];

    Event pop() {
        return events[--size];
    }

    Event peek() {
        return events[size - 1];
    }

    void push(Event event) {
        events[size++] = event;
    }

    int size() {
        return size;
    }

    @Override
    public Iterator<Event> iterator() {
        return new Iterator() {

            private int i = 0;

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Event next() {
                return events[i++];
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
    }
}

class Event {

    enum Type {

        Start, Play, Flow, End
    }
    final Type type;
    final int posX;
    final int posY;
    final int marker;
    final int rotation;
    final int score;
    final TraceWallResult wallResult;
    final TraceOpenResult openResult;

    Event(final Type type, final int posX, final int posY, final int marker, final int rotation, final int score, final TraceWallResult wallResult, final TraceOpenResult openResult) {
        this.type = type;
        this.posX = posX;
        this.posY = posY;
        this.marker = marker;
        this.rotation = rotation;
        this.score = score;
        this.wallResult = wallResult;
        this.openResult = openResult;
    }

    @Override
    public String toString() {
        return type + "{" + posX + ", " + posY + " } r(" + rotation + ") " + wallResult + " " + openResult + " score(" + score + ")";
    }
}
