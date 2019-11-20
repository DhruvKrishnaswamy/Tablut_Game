package tablut;


import java.util.HashSet;

import static java.lang.Math.*;

import static tablut.Square.sq;
import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Dhruv Krishnaswamy
 */
class AI extends Player {

    /**
     * A position-score magnitude indicating a win (for white if positive,
     * black if negative).
     */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /**
     * A position-score magnitude indicating a forced win in a subsequent
     * move.  This differs from WINNING_VALUE to avoid putting off wins.
     */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /**
     * A magnitude greater than a normal value.
     */
    private static final int INFTY = Integer.MAX_VALUE;

    /**
     * A new AI with no piece or controller (intended to produce
     * a template).
     */
    AI() {
        this(null, null);
    }


    /**
     * A new AI playing PIECE under control of CONTROLLER.
     */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        return "";

    }

    @Override
    boolean isManual() {
        return false;
    }

    /**
     * Return a move for me from the current position, assuming there
     * is a move.
     */
    private Move findMove() {
        Board b = new Board();


        if (myPiece() == BLACK) {
            findMove(b, maxDepth(b), true, -1, INFTY, -INFTY);
        } else {
            findMove(b, maxDepth(b), true, 1, INFTY, -INFTY);
        }
        return _lastFoundMove;
    }

    /**
     * The move found by the last call to one of the ...FindMove methods
     * below.
     */
    private Move _lastFoundMove;

    /**
     * Find a move from position BOARD and return its value, recording
     * the move found in _lastFoundMove iff SAVEMOVE. The move
     * should have maximal value or have value > BETA if SENSE==1,
     * and minimal value or value < ALPHA if SENSE==-1. Searches up to
     * DEPTH levels.  Searching at level 0 simply returns a static estimate
     * of the board value and does not set _lastMoveFound.
     */

    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (depth == 0 || board.winner() != null) {
            return staticScore(board);
        }
        if (sense == 1)  {
            int value = -INFTY;
            for (Move m : board.legalMoves(WHITE)) {
                board.makeMove(m);
                int res = findMove(board, depth - 1, false,
                            -sense, alpha, beta);
                board.undo();
                value = Math.max(alpha, value);
                if (value >= res && saveMove) {
                    _lastFoundMove = m;
                }
                alpha = Math.max(alpha, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        } else {
            int value = INFTY;
            for (Move b : board.legalMoves(BLACK)) {
                board.makeMove(b);
                int res = findMove(board, depth - 1, false,
                        -sense, alpha, beta);
                board.undo();
                value = Math.min(beta, value);
                if (res >= value && saveMove) {
                    _lastFoundMove = b;
                }
                beta = Math.min(beta, value);
                if (alpha >= beta) {
                    break;
                }
            }
            return value;
        }
    }
    /**
     * Return a heuristically determined maximum search depth
     * based on characteristics of BOARD.
     * @param board : this is a board object
     */
    private static int maxDepth(Board board) {
        return 1;
    }
    /**
     * Return a king position.
     * @param board : this is a board object
     */
    Square kingPosition(Board board) {
        for (int i = 0; i < Board.SIZE; i++) {
            for (int j = 0; j < Board.SIZE; j++) {
                if (board.get(sq(i, j)) == KING) {
                    return sq(i, j);
                }
            }
        }
        return null;
    }
    /**
     * Return a heuristic value for BOARD.
     * @param board : this is a board object
     */
    private int staticScore(Board board) {
        double score;
        final int increm = 50;
        score =  numpieces(board) + unblocked(board) + shortdistedge(board);
        //return (int) score;
        return 2;
    }
    /** Determining the number of pieces of black and white and their ratio.
     * @param board  : this is a board object
     * @return This returns a double */
    public double numpieces(Board board) {
        double score = 0;
        int blackpieces = 0;
        int whitepieces = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; i < 9; i++) {
                if (board.get(sq(i, j)) == WHITE
                        || board.get(sq(i, j)) == KING) {
                    whitepieces++;
                }   else if (board.get(sq(i, j)) == BLACK) {
                    blackpieces++;
                }
            }
        }
        score = whitepieces - blackpieces;
        return score;
    }
    /** This returns the score for the no of black pieces.
     * rook moves way from king
     * @param board : this is a board object
     * @param inc : increm */
    public double tworookmovesking(Board board, double inc) {
        double score = 0;
        for (int j = 0; j < 4; j++) {
            if (board.get(board.kingPosition().rookMove(j, 2)) == BLACK) {
                score += inc;
            }
        }
        return score;
    }
    /** This returns the shortest distance from the king.
     * to an edge
     * @param board : : this is a board object  */
    public double shortdistedge(Board board) {
        double score = 0;
        HashSet<Double> distances = new HashSet<Double>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; i < 9; i++) {
                if (sq(i, j).isEdge()) {
                    double dis = Math.sqrt(Math.pow((i
                            - board.kingPosition().col()), 2)
                            + Math.pow((j - board.kingPosition().row()), 2));
                    distances.add(dis);
                }
            }
        }
        double min = 1000;
        for (double x : distances) {
            if (x < min) {
                min = x;
            }
        }
        if (min < 3) {
            score = score + min;
        }
        return score;
    }
    /**
     * Move FROM-TO, assuming this is a legal move.
     *
     * @param board : takes a board
     * @return integer
     *
     */
    public int unblocked(Board board) {
        int numrookmoves = 0;
        for (Move x : board.legalMoves(KING)) {
            if (x.to().isEdge()) {
                return WILL_WIN_VALUE;
            }
        }
        return numrookmoves;
    }
    /**
     * Move FROM-TO, assuming this is a legal move.
     *
     * @param board : takes a board.
     * @return int
     *
     */
    public int blackaroundking(Board board) {
        int black = 0;
        for (int i = 0; i < 4; i++) {
            Square b  = board.kingPosition().rookMove(i, 2);
            if (board.get(b) == BLACK) {
                black++;
            }
        }
        return -10 * black;
    }

}
