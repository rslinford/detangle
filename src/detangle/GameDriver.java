package detangle;

import detangle.Space.SpaceState;

/**
 *
 * @author Scott
 */
public class GameDriver {

    private void grind() {
        Board board = new Board();
        System.out.println("Initial state");
        System.out.println(board.current + " (start)");
        System.out.println(board.adjacent + " (first)");
        System.out.println("Play begins");

        int score = 0;
        int p = 0;

        while (board.adjacent.state == SpaceState.Playable) {
            p = 1;
            score = score + p;
            System.out.println(board.adjacent + " (playing) +" + p);
            board.play();
            while (board.adjacent.state == SpaceState.Played) {
                p++;
                score = score + p;
                System.out.println(board.adjacent + " (flowing) +" + p);
                board.flow();
            }
            System.out.println("   total score:" + score);
        }

        System.out.println("Game over");
        System.out.println(board.adjacent + " (dead end)");
    }

    public static void main(String[] args) {
        new GameDriver().grind();
    }
}
