package chess;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessMove {

    private final ChessPosition startPosition;
    private final ChessPosition endPosition;
    private final ChessPiece.PieceType promotionPiece;

    public ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                     ChessPiece.PieceType promotionPiece) {
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.promotionPiece = promotionPiece;
    }

    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition getStartPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition getEndPosition() {
        return endPosition;
    }

    @Override
    public String toString() {
        return String.format("%s%s %s", startPosition, endPosition, promotionPiece);
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */
    public ChessPiece.PieceType getPromotionPiece() {
        return promotionPiece;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessMove)) {
            return false;
        }
        ChessMove other = (ChessMove) o;
        if (!this.startPosition.equals(other.startPosition)) {
            return false;
        }
        if (!this.endPosition.equals(other.endPosition)) {
            return false;
        }
        if (this.promotionPiece == null) {
            if (other.promotionPiece != null) {
                return false;
            }
        } else {
            if (!this.promotionPiece.equals(other.promotionPiece)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = startPosition.hashCode();
        result = 31 * result + endPosition.hashCode();
        result = 31 * result + (promotionPiece != null ? promotionPiece.hashCode() : 0);
        return result;
    }
}
