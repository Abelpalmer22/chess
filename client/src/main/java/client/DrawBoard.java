package client;

import chess.*;
import ui.EscapeSequences;

public class DrawBoard {

    public static String draw(ChessGame game, boolean blackPerspective) {
        StringBuilder out = new StringBuilder();

        out.append(EscapeSequences.ERASE_SCREEN);

        var board = game.getBoard();

        int startRow = blackPerspective ? 1 : 8;
        int endRow = blackPerspective ? 8 : 1;
        int rowStep = blackPerspective ? 1 : -1;

        int startCol = blackPerspective ? 8 : 1;
        int endCol = blackPerspective ? 1 : 8;
        int colStep = blackPerspective ? -1 : 1;

        out.append("   ");
        for (int c = startCol; c != endCol + colStep; c += colStep) {
            out.append("  ").append((char) ('a' + c - 1)).append(" ");
        }
        out.append("\n");

        boolean isLight;

        for (int r = startRow; r != endRow + rowStep; r += rowStep) {
            out.append(" ").append(r).append(" ");

            for (int c = startCol; c != endCol + colStep; c += colStep) {

                isLight = (r + c) % 2 == 0;

                if (isLight) {
                    out.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                } else {
                    out.append(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                }

                ChessPiece piece = board.getPiece(new ChessPosition(r, c));

                if (piece == null) {
                    out.append(EscapeSequences.EMPTY);
                } else {
                    if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                        out.append(getWhitePiece(piece.getPieceType()));
                    } else {
                        out.append(getBlackPiece(piece.getPieceType()));
                    }
                }

                out.append(EscapeSequences.RESET_BG_COLOR);
            }

            out.append(" ").append(r).append("\n");
        }

        out.append("   ");
        for (int c = startCol; c != endCol + colStep; c += colStep) {
            out.append("  ").append((char) ('a' + c - 1)).append(" ");
        }

        return out.toString();
    }

    private static String getWhitePiece(ChessPiece.PieceType type) {
        return switch (type) {
            case KING -> EscapeSequences.WHITE_KING;
            case QUEEN -> EscapeSequences.WHITE_QUEEN;
            case BISHOP -> EscapeSequences.WHITE_BISHOP;
            case KNIGHT -> EscapeSequences.WHITE_KNIGHT;
            case ROOK -> EscapeSequences.WHITE_ROOK;
            case PAWN -> EscapeSequences.WHITE_PAWN;
        };
    }

    private static String getBlackPiece(ChessPiece.PieceType type) {
        return switch (type) {
            case KING -> EscapeSequences.BLACK_KING;
            case QUEEN -> EscapeSequences.BLACK_QUEEN;
            case BISHOP -> EscapeSequences.BLACK_BISHOP;
            case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
            case ROOK -> EscapeSequences.BLACK_ROOK;
            case PAWN -> EscapeSequences.BLACK_PAWN;
        };
    }
}
