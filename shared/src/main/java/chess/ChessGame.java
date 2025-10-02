package chess;

import java.lang.reflect.GenericDeclaration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private ChessBoard board;
    private TeamColor currentTurn;
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        currentTurn = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return currentTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        this.currentTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessPosition> getPieces(ChessBoard board, TeamColor color) {
        List<ChessPosition> pieces = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                ChessPiece currPiece = board.squares[i][j];
                if (currPiece == null) continue;
                if (currPiece.getTeamColor() == color) {
                    pieces.add(new ChessPosition(i+1,j+1));
                }
                else {continue;}
            }
        }
        return pieces;
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        if (piece == null) return new ArrayList<>();
        Collection<ChessMove> candidates = piece.pieceMoves(board, startPosition);
        Collection<ChessMove> validMoves = new ArrayList<>();
        TeamColor color = piece.getTeamColor();
        int startRow = startPosition.getRow();
        int startCol = startPosition.getColumn();
        for (ChessMove move: candidates) {
            ChessPosition finalPosition = move.getEndPosition();
            int endRow = finalPosition.getRow();
            int endCol = finalPosition.getColumn();
            ChessBoard fakeBoard = board.copy();
            ChessPiece movingPiece = fakeBoard.getPiece(startPosition);
            fakeBoard.squares[startRow-1][startCol-1] = null;
            fakeBoard.squares[endRow-1][endCol-1] = movingPiece;
            if (!inCheck(fakeBoard, color)) validMoves.add(move);
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPosition startPosition = move.getStartPosition();
        ChessPosition endPosition = move.getEndPosition();
        if (!validMoves(startPosition).contains(move)) throw new InvalidMoveException("Invalid move");
        ChessPiece piece = board.getPiece(startPosition);
        int r1 = startPosition.getRow();
        int c1 = startPosition.getColumn();
        int r2 = endPosition.getRow();
        int c2 = endPosition.getColumn();
        board.squares[r1-1][c1-1] = null;
        if (piece.getPieceType() == ChessPiece.PieceType.PAWN && (r2 == 8 || r2 == 1)) {
            board.squares[r2-1][c2-1] = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }
        else {
            board.squares[r2-1][c2-1] = piece;
        }
        currentTurn = (currentTurn == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
    }

    public boolean inCheck(ChessBoard board, TeamColor teamColor) {
        ChessPosition kingPos = null;
        for (ChessPosition pos : getPieces(board, teamColor)) {
            ChessPiece piece = board.getPiece(pos);
            if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING) {
                kingPos = pos;
                break;
            }
        }
        TeamColor other = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;
        for (ChessPosition enemy : getPieces(board, other)) {
            ChessPiece enemyPiece = board.getPiece(enemy);
            for (ChessMove move : enemyPiece.pieceMoves(board, enemy)) {
                if (move.getEndPosition().equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return inCheck(board, teamColor);
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) return false;
        for (ChessPosition piece : getPieces(board, teamColor)) {
            if (!validMoves(piece).isEmpty()) return false;
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) return false;
        for (ChessPosition piece : getPieces(board, teamColor)) {
            if (!validMoves(piece).isEmpty()) return false;
        }
        return true;

    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
