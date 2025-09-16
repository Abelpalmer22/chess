package chess;

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

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    private Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) return moves;
        int[] rowDirections = {1, 1, -1, -1};
        int[] colDirections = {1, -1, 1, -1};

        for (int d = 0; d < 4; d++) {
            int r = myPosition.getRow()+rowDirections[d];
            int c = myPosition.getColumn()+colDirections[d];
            while (true) {
                ChessPosition newPosition = new ChessPosition(r, c);
                ChessPiece otherPiece = board.getPiece(newPosition);
                if (r < 1 || r > 8 || c < 1 || c > 8) break;
                if (otherPiece == null) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                } else if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                    moves.add(new ChessMove(myPosition, newPosition, null));
                    break; // cause we killed the piece
                } else {break;}
                r += rowDirections[d];
                c += colDirections[d];
            }
        }
        return moves;
    }

    private Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) return moves;
        int[] rowDirections = {1, 1, 0, -1, -1, -1, 0, 1};
        int[] colDirections = {0, 1, 1, 1, 0, -1, -1, -1};
        for (int d = 0; d < 8; d++) {
            int r = myPosition.getRow()+rowDirections[d];
            int c = myPosition.getColumn()+colDirections[d];
            ChessPosition newPosition = new ChessPosition(r, c);
            ChessPiece otherPiece = board.getPiece(newPosition);
            if (r < 1 || r > 8 || c < 1 || c > 8) continue;
            if (otherPiece == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition myPosition) {
        List<ChessMove> moves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        if (myPiece == null) return moves;
        int[] rowDirections = {2, 1, -1, -2, -2, -1, 1, 2};
        int[] colDirections = {1, 2, 2, 1, -1, -2, -2, -1};
        for (int d = 0; d < 8; d++) {
            int r = myPosition.getRow()+rowDirections[d];
            int c = myPosition.getColumn()+colDirections[d];
            ChessPosition newPosition = new ChessPosition(r, c);
            ChessPiece otherPiece = board.getPiece(newPosition);
            if (r < 1 || r > 8 || c < 1 || c > 8) continue;
            if (otherPiece == null) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            } else if (otherPiece.getTeamColor() != myPiece.getTeamColor()) {
                moves.add(new ChessMove(myPosition, newPosition, null));
            }
        }
        return moves;
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition);
        if (piece == null) return List.of();
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
        return List.of();
    }
}
