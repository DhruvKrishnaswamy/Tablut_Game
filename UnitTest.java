package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

import java.util.List;

/** The suite of all JUnit tests for the enigma package.
 *  @author Dhruv Krishnaswamy
 */
public class UnitTest {

    /**
     * Run the JUnit tests in this package. Add xxxTest.class entries to
     * the arguments of runClasses to run other JUnit tests.
     */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /**
     * A dummy test as a placeholder for real ones.
     */
    @Test
    public void dummyTest() {
        assertTrue("There are no unit tests!", true);
    }


    @Test
    public void captureTest5() {
        Board a = new Board();
        assertTrue("There is a capture test!", true);

    }

    @Test
    public void testLegalWhiteMoves() {

        Board b = new Board();

        List<Move> movesList = b.legalMoves(Piece.WHITE);

        assertEquals(56, movesList.size());


        assertFalse(movesList.contains(Move.mv("e7-8")));
        assertFalse(movesList.contains(Move.mv("e8-f")));

        assertTrue(movesList.contains(Move.mv("e6-f")));
        assertTrue(movesList.contains(Move.mv("f5-8")));


    }

    /**
     * Tests legalMoves for black pieces to
     * make sure it returns all legal Moves.
     * This method needs to be finished and
     * may need to be changed
     * based on your implementation.
     */
    @Test
    public void testLegalBlackMoves() {
        Board b = new Board();
        List<Move> movesList = b.legalMoves(Piece.BLACK);

        assertEquals(80, movesList.size());

        assertFalse(movesList.contains(Move.mv("e8-7")));
        assertFalse(movesList.contains(Move.mv("e7-8")));


        assertTrue(movesList.contains(Move.mv("f9-i")));
        assertTrue(movesList.contains(Move.mv("h5-1")));


    }
}


