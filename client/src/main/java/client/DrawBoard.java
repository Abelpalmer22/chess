package client;

import chess.*;
import ui.EscapeSequences;

public class DrawBoard {

    public static String draw(ChessGame game, boolean whitePerspective) {
        StringBuilder out = new StringBuilder();
        out.append(EscapeSequences.ERASE_SCREEN);
        var board = game.getBoard();
        int startRow, endRow, rowStep;
        int startCol, endCol, colStep;
        if (whitePerspective) {
            startRow = 8;
            endRow   = 1;
            rowStep  = -1;
            startCol = 1;
            endCol   = 8;
            colStep  = 1;
        } else {
            startRow = 1;
            endRow   = 8;
            rowStep  = 1;
            startCol = 8;
            endCol   = 1;
            colStep  = -1;
        }

        out.append("   ");
        for (int c = startCol; c != endCol + colStep; c += colStep) {
            out.append(" ").append((char) ('a' + c - 1)).append(" ");
        }
        out.append("\n");
        boolean isDark; //for dark squares
        for (int r = startRow; r != endRow + rowStep; r += rowStep) {
            out.append(" ").append(r).append(" ");

            for (int c = startCol; c != endCol + colStep; c += colStep) {
                isDark = (r + c) % 2 == 0;

                if (isDark) {
                    out.append(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                } else {
                    out.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                }
                ChessPiece piece = board.getPiece(new ChessPosition(r, c));
                if (piece == null) {
                    out.append(EscapeSequences.EMPTY);
                } else {
                    if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
                        out.append(getWhitePiece(piece.getPieceType())); // super super jank i know but it works
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
            out.append(" ").append((char) ('a' + c - 1)).append(" ");
        }
        out.append('\n');
        return out.toString();
    }

    public static chess.ChessPosition parsePosition(String s) {
        if (s.length() != 2) {
            throw new IllegalArgumentException("Bad position");
        }

        char file = s.toLowerCase().charAt(0);
        char rank = s.charAt(1);

        int col = file - 'a' + 1;
        int row = rank - '0';

        if (col < 1 || col > 8 || row < 1 || row > 8) {
            throw new IllegalArgumentException("Bad position");
        }

        return new chess.ChessPosition(row, col);
    }


    public static String drawHighlight(ChessGame game, ChessPosition origin, boolean whitePerspective) {
        StringBuilder out = new StringBuilder();
        out.append(EscapeSequences.ERASE_SCREEN);
        var board = game.getBoard();
        var moves = game.validMoves(origin);
        var highlightDestinations = new java.util.HashSet<ChessPosition>();
        for (var m : moves) {
            highlightDestinations.add(m.getEndPosition());
        }
        int startRow, endRow, rowStep;
        int startCol, endCol, colStep;
        if (whitePerspective) {
            startRow = 8;
            endRow   = 1;
            rowStep  = -1;
            startCol = 1;
            endCol   = 8;
            colStep  = 1;
        } else {
            startRow = 1;
            endRow   = 8;
            rowStep  = 1;
            startCol = 8;
            endCol   = 1;
            colStep  = -1;
        }
        out.append("   ");
        for (int c = startCol; c != endCol + colStep; c += colStep) {
            out.append(" ").append((char)('a' + c - 1)).append(" ");
        }
        out.append("\n");

        for (int r = startRow; r != endRow + rowStep; r += rowStep) {
            out.append(" ").append(r).append(" ");

            for (int c = startCol; c != endCol + colStep; c += colStep) {

                ChessPosition pos = new ChessPosition(r, c);

                boolean isOrigin = pos.equals(origin);
                boolean isDestination = highlightDestinations.contains(pos);

                if (isOrigin) {
                    out.append(EscapeSequences.SET_BG_COLOR_GREEN);
                } else if (isDestination) {
                    out.append(EscapeSequences.SET_BG_COLOR_YELLOW);
                } else {
                    boolean isDark = (r + c) % 2 == 0;
                    if (isDark) {
                        out.append(EscapeSequences.SET_BG_COLOR_DARK_GREY);
                    } else {
                        out.append(EscapeSequences.SET_BG_COLOR_LIGHT_GREY);
                    }
                }

                ChessPiece piece = board.getPiece(pos);
                if (piece == null) {
                    out.append(EscapeSequences.EMPTY);
                } else {
                    if (piece.getTeamColor() == ChessGame.TeamColor.BLACK) {
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
            out.append(" ").append((char) ('a' + c - 1)).append(" ");
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
