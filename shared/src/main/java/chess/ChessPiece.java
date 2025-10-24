package chess;

import java.lang.reflect.Type;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessPiece)) {
            return false;
        }
        ChessPiece other = (ChessPiece) o;
        return this.pieceColor == other.pieceColor &&
                this.type == other.type;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(pieceColor, type);
    }


    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */

    private void loop(ChessBoard board, ChessPosition myPosition, ArrayList<ChessMove> moves, ChessPiece myPiece, int[] rowDirections, int[] colDirections) {
        for (int d = 0; d < 4; d++) {
            int r = myPosition.getRow()+rowDirections[d];
            int c = myPosition.getColumn()+colDirections[d];
            while (true) {
                ChessPosition newPosition = new ChessPosition(r, c);
                ChessPiece otherPiece = board.getPiece(newPosition);
                if (r < 1 || r > 8 || c < 1 || c > 8) {
                    break;
                }
                if (otherPiece == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break; // cause we killed the piece
                } else {
                    break;
                }
                r += rowDirections[d];
                c += colDirections[d];
            }
        }
    }

    private void loopKing(ChessBoard board, ChessPosition myPosition, List<ChessMove> moves, ChessPiece myPiece, int[] rowDirections, int[] colDirections) {
        for (int d = 0; d < 8; d++) {
            int r = myPosition.getRow()+rowDirections[d];
            int c = myPosition.getColumn()+colDirections[d];
            ChessPosition newPosition = new ChessPosition(r, c);
            ChessPiece otherPiece = board.getPiece(newPosition);
            if (r < 1 || r > 8 || c < 1 || c > 8) {
                continue;
            }
            if (otherPiece == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
    }

    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) return moves;
        int[] rowDirections = {1, 1, -1, -1};
        int[] colDirections = {1, -1, 1, -1};
        loop(board, myPosition, moves, myPiece, rowDirections, colDirections);
        return moves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) {
            return moves;
        }
        int[] rowDirections = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] colDirections = {0, 1, 1, 1, 0, -1, -1, -1};
        loopKing(board, myPosition, moves, myPiece, rowDirections, colDirections);
        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) {
            return moves;
        }
        int[] rowDirections = {2, 1, -1, -2, -2, -1, 1, 2};
        int[] colDirections = {1, 2, 2, 1, -1, -2, -2, -1};
        loopKing(board, myPosition, moves, myPiece, rowDirections, colDirections);
        return moves;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        int currRow = myPosition.getRow();
        int currCol = myPosition.getColumn();
        if (myPiece == null) {
            return moves;
        }
        int forwardDirection = (myPiece.pieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        if ((myPiece.pieceColor == ChessGame.TeamColor.WHITE && currRow == 2) || (myPiece.pieceColor == ChessGame.TeamColor.BLACK && currRow == 7)) {
            ChessPosition singleStepPos = new ChessPosition(currRow+forwardDirection, currCol);
            ChessPiece singlePiece = board.getPiece(singleStepPos);
            ChessPosition doubleStepPos = new ChessPosition(currRow+2*forwardDirection, currCol);
            ChessPiece doublePiece = board.getPiece(doubleStepPos);
            if (singlePiece == null && doublePiece == null) {
                moves.add(new ChessMove(myPosition, doubleStepPos, null));
            }
        }
        int r = currRow+forwardDirection;
        int c = currCol;
        ChessPosition nonCapturingPosition = new ChessPosition(r, c);
        ChessPiece otherPiece = board.getPiece(nonCapturingPosition);
        if (otherPiece == null) {
            if (r == 8 || r == 1) {
                for (PieceType promotion: new ChessPiece.PieceType[] {
                        PieceType.QUEEN,
                        PieceType.ROOK,
                        PieceType.BISHOP,
                        PieceType.KNIGHT })
                    moves.add(new ChessMove(myPosition, nonCapturingPosition, promotion));
            }
            else {
                moves.add(new ChessMove(myPosition, nonCapturingPosition, null));
            }
        }
        int[] rowDirs = {1, -1};
        for (int d = 0; d < 2; d++) {
            r = currRow + forwardDirection;
            c = currCol + rowDirs[d];
            if (r < 1 || r > 8 || c < 1 || c > 8) {
                continue;
            }
            ChessPosition capturingPosition = new ChessPosition(r, c);
            otherPiece = board.getPiece(capturingPosition);
            if (otherPiece != null && otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                if (r == 8 || r == 1) {
                    for (PieceType promotion: new ChessPiece.PieceType[] {
                            PieceType.QUEEN,
                            PieceType.ROOK,
                            PieceType.BISHOP,
                            PieceType.KNIGHT })
                        moves.add(new ChessMove(myPosition, capturingPosition, promotion));
                }
                else {
                    moves.add(new ChessMove(myPosition, capturingPosition, null));
                }
            }
        }
        return moves;
    }

    @Override
    public String toString() {
        return "ChessPiece{" +
                "pieceColor=" + pieceColor +
                ", type=" + type +
                '}';
    }

    private Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) {
            return moves;
        }
        int[] rowDirections = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] colDirections = {0, 1, 1, 1, 0, -1, -1, -1};

        loopKing(board, myPosition, moves, myPiece, rowDirections, colDirections);
        return moves;
    }

    private Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition myPosition) {
        ArrayList<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) {
            return moves;
        }
        int[] rowDirections = {1, 0, -1, 0};
        int[] colDirections = {0, 1, 0, -1};
        loop(board, myPosition, moves, myPiece, rowDirections, colDirections);
        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece == null) {
            return List.of();
        }
        List<ChessMove> moves = new ArrayList<>();
        if (piece.getPieceType() == PieceType.BISHOP) {
            return bishopMoves(board, myPosition);
        }
        else if (piece.getPieceType() == PieceType.KING) {
            return kingMoves(board, myPosition);
        }
        else if (piece.getPieceType() == PieceType.KNIGHT) {
            return knightMoves(board, myPosition);
        }
        else if (piece.getPieceType() == PieceType.PAWN) {
            return pawnMoves(board, myPosition);
        }
        else if (piece.getPieceType() == PieceType.QUEEN) {
            return queenMoves(board, myPosition);
        }
        else {
            return rookMoves(board, myPosition);
        }
    }
}
