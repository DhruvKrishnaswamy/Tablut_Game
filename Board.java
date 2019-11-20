package tablut;


import java.util.ArrayList;
import java.util.Stack;
import java.util.HashSet;
import java.util.List;
import java.util.Formatter;


import static tablut.Piece.*;
import static tablut.Square.*;
import static tablut.Move.mv;


/**
 * The state of a Tablut Game.
 *
 * @author Dhruv Krishnaswamy
 */
class Board {

    /**
     * The number of squares on a side of the board.
     */
    static final int SIZE = 9;

    /**
     * The throne (or castle) square and its four surrounding squares..
     */
    static final Square THRONE = sq(4, 4),
            NTHRONE = sq(4, 5),
            STHRONE = sq(4, 3),
            WTHRONE = sq(3, 4),
            ETHRONE = sq(5, 4);

    /**
     * Initial positions of attackers.
     */
    static final Square[] INITIAL_ATTACKERS = {
            sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
            sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
            sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
            sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /**
     * Initial positions of defenders of the king.
     */
    static final Square[]INITIAL_DEFENDERS = {NTHRONE,
        ETHRONE, STHRONE, WTHRONE,
            sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /**
     * Initializes a game board with SIZE squares on a side in the
     * initial position.
     */
    Board() {
        init();
    }

    /**
     * Initializes a copy of MODEL.
     */
    Board(Board model) {
        copy(model);
    }


    /**
     * Copies MODEL into me.
     */
    void copy(Board model) {
        init();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this._board[i][j] = model._board[i][j];
            }
        }

        this._board = model._board;
        this._turn = model._turn;
        this._winner = model._winner;
        this._repeated = model._repeated;
        this._lim = model._lim;
        this._moveCount = model._moveCount;
        this._kingpos = model._kingpos;
        this.boars = model.boars;
    }

    /**
     * Clears the board to the initial position.
     */
    void init() {
        _turn = BLACK;
        _winner = null;
        clearUndo();
        _board = new Piece[SIZE][SIZE];
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                this._board[i][j] = EMPTY;
            }
        }
        for (Square s : INITIAL_ATTACKERS) {
            put(BLACK, s);
        }

        for (Square def : INITIAL_DEFENDERS) {
            put(WHITE, def);
        }
        put(KING, THRONE);
        boars.add(encodedBoard());
    }

    /**
     * Set the move limit to LIM.  It is an error if 2*LIM <= moveCount().
     *
     * @param n : This is the number of moves.
     */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException();
        } else {
            _lim = n;
        }
    }

    /**
     * Return a Piece representing whose move it is (WHITE or BLACK).
     */
    Piece turn() {
        return _turn;
    }

    /**
     * Return the winner in the current position, or null if there is no winner
     * yet.
     */
    Piece winner() {
        return _winner;
    }

    /**
     * Returns true iff this is a win due to a repeated position.
     */
    boolean repeatedPosition() {
        return _repeated;
    }

    /**
     * Record current position and set winner() next mover if the current
     * position is a repeat.
     */
    private void checkRepeated() {
        String state = encodedBoard();
        if (boars.search(state) == -1) {
            boars.add(state);
        } else {
            _winner = _turn.opponent();
        }
    }


    /**
     * Return the number of moves since the initial position that have not been
     * undone.
     */
    int moveCount() {
        return _moveCount;
    }

    /**
     * Return location of the king.
     */
    Square kingPosition() {
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (get(i, j) == KING) {
                    return sq(i, j);
                }
            }
        }
        return null;
    }

    /**
     * Return the contents the square at S.
     */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /**
     * Return the contents of the square at (COL, ROW), where
     * 0 <= COL, ROW <= 9.
     */
    final Piece get(int col, int row) {
        if (col < SIZE && row < SIZE && col >= 0 && row >= 0) {
            return _board[col][row];
        } else {
            return EMPTY;
        }
    }

    /**
     * Return the contents of the square at COL ROW.
     */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /**
     * Set square S to P.
     */
    final void put(Piece p, Square s) {
        _board[s.col()][s.row()] = p;
        if (p == KING) {
            _kingpos = s;
        }
    }

    /**
     * Set square S to P and record for undoing.
     */
    final void revPut(Piece p, Square s) {

        Board newb = new Board();
        newb.copy(this);
    }

    /**
     * Set square COL ROW to P.
     */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /**
     * Return true iff FROM - TO is an unblocked rook move on the current
     * board.  For this to be true, FROM-TO must be a rook move and the
     * squares along it, other than FROM, must be empty.
     */
    boolean isUnblockedMove(Square from, Square to) {
        if (!from.isRookMove(to)) {
            return false;
        }
        int direc = from.direction(to);
        if (from.row() == to.row()) {
            int dist = Math.abs(from.col() - to.col());
            for (int a = 1; a <= dist; a++) {
                if (get(from.rookMove(direc, a)) != EMPTY) {
                    return false;
                }
            }
        }
        if (from.col() == to.col()) {
            int dist = Math.abs(from.row() - to.row());
            for (int b = 1; b <= dist; b++) {
                if (get(from.rookMove(direc, b)) != EMPTY) {
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Return true iff FROM is a valid starting square for a move.
     */
    boolean isLegal(Square from) {

        if (get(from) == KING && _turn == WHITE) {
            return true;
        } else {
            return get(from).side() == _turn;
        }
    }

    /**
     * Return true iff FROM-TO is a valid move.
     */
    boolean isLegal(Square from, Square to) {

        if (to == THRONE) {
            if (get(from) != KING) {
                return false;
            }
        }
        if (from.col() >= SIZE || from.row() >= SIZE) {
            return false;
        }
        if (to.col() >= SIZE || to.row() >= SIZE) {
            return false;
        }

        if (!isUnblockedMove(from, to)) {
            return false;
        }
        return true;
    }

    /**
     * Return true iff MOVE is a legal move in the current
     * position.
     */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     */
    void makeMove(Square from, Square to) {
        assert isLegal(from, to);

        if (!isLegal(from)) {
            throw new IllegalArgumentException();
        }
        put(get(from), to);
        put(EMPTY, from);
        for (int x = 0; x < 4; x++) {
            Square c = to.rookMove(x, 2);
            if (c != null) {
                capture(to, c);
            }
        }
        if ((get(to.col(), to.row()) == KING) && to.isEdge()) {
            _winner = WHITE;
        }

        _moveCount++;
        checkRepeated();
        _turn = _turn.opponent();
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     *
     * @param s : takes a board string to decode.
     * @return string of decoded board
     *
     */
    Piece[][] decode(String s) {

        char[] arr = s.toCharArray();
        int a = 0;
        int b = 0;
        Piece[][] newb1 = new Piece[SIZE][SIZE];
        for (int i = 1; i < arr.length; i++) {
            Piece c = convPiece(arr[i]);

            newb1[a][b] = c;
            if (a == SIZE - 1) {
                b += 1;
                a = 0;
            } else {
                a++;
            }
        }
        return newb1;
    }

    /**
     * Move FROM-TO, assuming this is a legal move.
     *
     * @param c : takes a char to convert to piece.
     * @return Piece
     *
     */
    Piece convPiece(char c) {
        if (c == 'W') {
            return WHITE;
        } else if (c == 'B') {
            return BLACK;
        } else if (c == '-') {
            return EMPTY;
        } else {
            return KING;
        }
    }

    /**
     * Move according to MOVE, assuming it is a legal move.
     */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /**
     * Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     * SQ0 and the necessary conditions are satisfied.
     */
    private void capture(Square sq0, Square sq2) {
        assert sq0.between(sq2) != null;
        assert sq2 != null;
        Piece p2 = get(sq2.col(), sq2.row());
        Square middlesq = sq0.between(sq2);
        Piece pMiddle = get(middlesq.col(), middlesq.row());
        Piece p0 = get(sq0.col(), sq0.row());
        if (Square.exists(sq0.col(), sq2.row())
                && Square.exists(sq2.col(), sq2.row())) {
            if (sq2 != THRONE && middlesq
                    != THRONE && sq0 != THRONE) {
                if (p2.side() == p0.side()) {
                    if (pMiddle == p0.opponent()) {
                        put(EMPTY, middlesq);
                    }
                } else if (p0 == KING || p2 == KING) {
                    if (p0 == WHITE || p2 == WHITE) {
                        if (pMiddle == BLACK) {
                            put(EMPTY, middlesq);
                        }
                    }
                }
            }
            if (pMiddle == KING) {
                if ((p0 == BLACK || p2 == BLACK)
                        && (sq2 == THRONE || sq0 == THRONE)) {
                    if (get(sq0.diag1(sq2).col(),
                            sq0.diag1(sq2).row()) == BLACK
                            && get(sq0.diag2(sq2).col(),
                            sq0.diag2(sq2).row()) == BLACK) {
                        put(EMPTY, middlesq);
                        _winner = BLACK;
                    }
                } else if (p0 == BLACK && p2 == BLACK) {
                    if ((get(sq0.diag1(sq2).col(),
                            sq0.diag1(sq2).row()) == BLACK
                            || get(sq0.diag2(sq2).col(),
                            sq0.diag2(sq2).row()) == BLACK)
                            && (sq0.diag1(sq2) == THRONE
                            || sq0.diag2(sq2) == THRONE)) {
                        put(EMPTY, middlesq);
                        _winner = BLACK;
                    }
                }
            }
            capture2(sq0, sq2);
        }
    }
    /** This method is continuing capture.
     * @param sq0 square0
     * @param sq2 square2
     * */
    private void capture2(Square sq0, Square sq2) {
        Piece p2 = get(sq2.col(), sq2.row());
        Square middlesq = sq0.between(sq2);
        Piece pMiddle = get(middlesq.col(), middlesq.row());
        Piece p0 = get(sq0.col(), sq0.row());
        Piece sThrone = get(STHRONE.col(), STHRONE.row());
        Piece nThrone = get(NTHRONE.col(), NTHRONE.row());
        Piece wThrone = get(WTHRONE.col(), WTHRONE.row());
        Piece eThrone = get(ETHRONE.col(), ETHRONE.row());
        if (sq2 == THRONE && p2 == KING
                || p0 == KING && sq0 == THRONE) {
            if (p0 == BLACK && pMiddle == WHITE
                    || p2 == BLACK && pMiddle == WHITE) {
                if (nThrone == BLACK && wThrone
                        == BLACK && sThrone == BLACK
                        || wThrone == BLACK && sThrone
                        == BLACK && eThrone == BLACK
                        || sThrone == BLACK && eThrone
                        == BLACK && nThrone == BLACK
                        || eThrone == BLACK && nThrone
                        == BLACK && wThrone == BLACK) {
                    put(EMPTY, middlesq);
                }
            }
        }
        if (p0 == BLACK && p2
                == BLACK && pMiddle == WHITE) {
            put(EMPTY, middlesq);
        }
        if (p0 == WHITE && p2
                == WHITE && pMiddle == BLACK) {
            put(EMPTY, middlesq);
        }
        if (p0 == BLACK && p2 == BLACK && pMiddle
                == KING && middlesq != THRONE) {
            if (middlesq != WTHRONE || middlesq != ETHRONE
                    || middlesq != STHRONE || middlesq != NTHRONE) {
                put(EMPTY, middlesq);
                _winner = BLACK;
            }
        }
        capture3(sq0, sq2);
    }

    /** This method is continuing capture2.
     * @param sq0 square0
     * @param sq2 square2
     * */
    public void capture3(Square sq0, Square sq2) {
        Piece p2 = get(sq2.col(), sq2.row());
        Square middlesq = sq0.between(sq2);
        Piece pMiddle = get(middlesq.col(), middlesq.row());
        Piece p0 = get(sq0.col(), sq0.row());
        Piece sThrone = get(STHRONE.col(), STHRONE.row());
        Piece nThrone = get(NTHRONE.col(), NTHRONE.row());
        Piece wThrone = get(WTHRONE.col(), WTHRONE.row());
        Piece eThrone = get(ETHRONE.col(), ETHRONE.row());
        if (sq0 == THRONE || sq2 == THRONE) {
            if (sq0 == THRONE && p0 == KING) {
                if (pMiddle == p2.opponent()) {
                    put(EMPTY, middlesq);
                }
            } else if (sq2 == THRONE && p2 == KING) {
                if (p0 == WHITE && pMiddle == p0.opponent()) {
                    put(EMPTY, middlesq);
                }
            }
        }
        if (pMiddle == KING && middlesq == THRONE) {
            if (wThrone == BLACK && eThrone == BLACK
                    && sThrone == BLACK && nThrone == BLACK) {
                put(EMPTY, middlesq);
                _winner = BLACK;
            }
        }
        if (sq2 == THRONE && get(ETHRONE) == KING
                && get(WTHRONE) == WHITE
                && get(STHRONE) == WHITE) {
            if (p0 == BLACK && pMiddle == WHITE) {
                put(EMPTY, middlesq);
            }
            if (p0 == WHITE && pMiddle == BLACK) {
                put(EMPTY, middlesq);
            }
        }
    }
    /**
     * Undo one move.  Has no effect on the initial board.
     */
    void undo() {
        if (_moveCount > 0) {
            undoPosition();
            this._turn = convPiece(boars.peek().charAt(0)).opponent();
        }
    }

    /**
     * Remove record of current position in the set of positions encountered,
     * unless it is a repeated position or we are at the first move.
     */
    private void undoPosition() {
        if (!_repeated && moveCount() > 0) {
            boars.pop();
            this._board = decode(boars.peek());
            _moveCount = _moveCount - 1;
        }
    }

    /**
     * Clear the undo stack and board-position counts. Does not modify the
     * current position or win status.
     */
    void clearUndo() {

        while (!boars.isEmpty()) {
            boars.pop();
        }
        _moveCount = 0;
        _repeated = false;
    }

    /**
     * Return a new mutable list of all legal moves on the current board for
     * SIDE (ignoring whose turn it is at the moment).
     */
    List<Move> legalMoves(Piece side) {
        ArrayList<Square> squa = new ArrayList<Square>();

        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (get(i, j) == side) {
                    squa.add(sq(i, j));
                }
            }
        }
        ArrayList<Move> possiblemv = new ArrayList<Move>();
        for (Square s : squa) {
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {
                    if (isLegal(s, sq(x, y))) {
                        possiblemv.add(mv(s, sq(x, y)));
                    }
                }
            }
        }
        return possiblemv;
    }

    /**
     * Return true iff SIDE has a legal move.
     */
    boolean hasMove(Piece side) {

        if (legalMoves(side).size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /**
     * Return a text representation of this Board.  If COORDINATES, then row
     * and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /**
     * Return the locations of all pieces on SIDE.
     */
    private HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> plocations = new HashSet<Square>();
        for (int i = 0; i < SIZE; i++) {
            for (int j = 0; j < SIZE; j++) {
                if (get(i, j) == side) {
                    plocations.add(sq(i, j));
                }
            }
        }
        return plocations;
    }

    /**
     * Return the contents of _board in the order of SQUARE_LIST as a sequence
     * of characters: the toString values of the current turn and Pieces.
     */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /**
     * Piece whose turn it is (WHITE or BLACK).
     */
    private Piece _turn = BLACK;
    /**
     * Cached value of winner on this board, or EMPTY if it has not been.
     * computed.
     */
    private Piece _winner = EMPTY;
    /**
     * Number of (still undone) moves since initial position.
     */
    private int _moveCount = 0;
    /**
     * True when current board is a repeated position (ending the game).
     */

    /**
     * True when current board is a repeated position (ending the game).
     */
    private boolean _repeated;

    /**
     * This is the limit on the number of moves.
     */
    private int _lim;

    /**
     * This is my board.
     */
    private Piece[][] _board;
    /**
     * This is a kingposition.
     */
    private Square _kingpos;

    /**
     * Hashet of encoded boards.
     */
    private Stack<String> boars = new Stack<String>();

    /**
     * This is a variable which stores the capture counts of black.
     */

    private int blackcapcount = 0;

    /**
     * This is a variable which stores the capture counts of white.
     */

    private int whitecapcount = 0;

    /**
     * This is the current move.
     */
    private Move currmove = null;


}






