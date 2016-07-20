/*
 * Copyright (C) 2016 Tom Sandmann
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.tomirio.chessengine.chesspieces;

import com.tomirio.chessengine.chessboard.ChessBoard;
import com.tomirio.chessengine.chessboard.ChessColour;
import com.tomirio.chessengine.chessboard.ChessPiece;
import com.tomirio.chessengine.chessboard.MoveDetails;
import com.tomirio.chessengine.chessboard.PieceType;
import com.tomirio.chessengine.chessboard.Position;
import com.tomirio.chessengine.moves.CaptureMove;
import com.tomirio.chessengine.moves.CastlingMove;
import com.tomirio.chessengine.moves.Move;
import com.tomirio.chessengine.moves.NormalMove;
import java.util.ArrayList;

/**
 *
 * @author Tom Sandmann
 */
public class King extends ChessPiece {

    private boolean castlingPossible;
    private boolean isCheck;

    /**
     * This constructor MUST be used when the chessboard is not known when a new
     * chess piece is made. The chessboard must be set later on with
     * <code>setBoard()</code>.
     *
     * @param colour The colour of the chess piece.
     * @param pos The position of the chess piece.
     */
    public King(ChessColour colour, Position pos) {
        super(PieceType.King, colour, pos);
        isCheck = false;
        castlingPossible = true;
    }

    /**
     *
     * @return <code>True</code> if castling is possible, <code>False</code>
     * otherwise.
     */
    public boolean castlingPossible() {
        return castlingPossible;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    private CastlingMove getCastlingMove(Rook rook) {
        if (getColumn() > rook.getColumn()) {
            // Left castle involved
            Position newKingPos = new Position(getRow(), getColumn() - 2);
            Position newRookPos = new Position(getRow(), getColumn() - 1);
            if (isSafePosition(newKingPos) && isSafePosition(newRookPos)) {
                // King does not move over pieces on which it would stand check
                return new CastlingMove(this, newKingPos, rook, newRookPos);
            }
        } else if (getColumn() < rook.getColumn()) {
            // Right castle involved
            Position newKingPos = new Position(getRow(), getColumn() + 2);
            Position newRookPos = new Position(getRow(), getColumn() + 1);
            if (isSafePosition(newKingPos) && isSafePosition(newRookPos)) {
                // King does not move over pieces on which it would stand check
                return new CastlingMove(this, newKingPos, rook, newRookPos);
            }
        }
        return null;
    }

    /**
     * @return The possible castling moves for this king.
     */
    private ArrayList<Move> getCastlingMoves() {
        ArrayList<Move> castlingMoves = new ArrayList<>();

        if (!castlingPossible) {
            // Castling not possible, return empty list
            return castlingMoves;
        }

        ArrayList<Rook> rooks = chessBoard.getRooks(getColour());
        if (rooks.isEmpty() || isCheck) {
            return castlingMoves;
        } else {
            for (Rook rook : rooks) {
                if (rook.castlingPossible() && rook.getRow() == getRow()) {
                    if (chessBoard.isEmptySubRow(getPos(), rook.getPos())) {
                        CastlingMove castlingMove = getCastlingMove(rook);
                        if (castlingMove != null) {
                            castlingMoves.add(castlingMove);
                        }
                    }

                }
            }
            return castlingMoves;
        }
    }

    /**
     *
     * @param newValue Set the variable castling possible
     */
    public void setCastlingPossible(boolean newValue) {
        castlingPossible = newValue;
    }

    @Override
    public ArrayList<Position> getCoveredPositions() {
        return getKingMoves().coveredFriendlyPieces;
    }

    /**
     *
     * @return All the possible moves for the king.
     */
    private MoveDetails getKingMoves() {
        MoveDetails moveDetails = new MoveDetails();
        for (int row = getRow() - 1; row <= getRow() + 1; row++) {
            for (int column = getColumn() - 1; column <= getColumn() + 1; column++) {
                Position newPos = new Position(row, column);
                if (chessBoard.isValidCoordinate(row, column) && newPos.isValid()) {
                    if (!(newPos.equals(getPos()))) {
                        // Calculated position is not equal to current position
                        if (!chessBoard.isOccupiedPosition(newPos) && isSafePosition(newPos)) {
                            // Normal move
                            NormalMove normalMove = new NormalMove(this, newPos);
                            moveDetails.moves.add(normalMove);
                        } else if (chessBoard.isOccupiedPosition(newPos)
                                && chessBoard.getColour(newPos) != getColour()
                                && isSafePosition(newPos)) {
                            // Capture move
                            CaptureMove captureMove = new CaptureMove(this, newPos);
                            moveDetails.moves.add(captureMove);
                        } else if (chessBoard.isOccupiedPosition(newPos)
                                && chessBoard.getColour(newPos) == getColour()) {
                            moveDetails.coveredFriendlyPieces.add(newPos);
                        }
                    }
                }
            }
        }
        return moveDetails;
    }

    @Override
    public ArrayList<Move> getPossibleMoves() {
        ArrayList<Move> possibleMoves = getKingMoves().moves;
        ArrayList<Move> castlingMoves = getCastlingMoves();
        possibleMoves.addAll(castlingMoves);
        return filterMoves(possibleMoves);
    }

    @Override
    public ArrayList<Move> getRawPossibleMoves() {
        ArrayList<Move> possibleMoves = getKingMoves().moves;
        possibleMoves.addAll(getCastlingMoves());
        return possibleMoves;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash + (this.isCheck ? 1 : 0);
        return hash;
    }

    /**
     * @return the value isCheck.
     */
    public boolean isCheck() {
        return isCheck;
    }

    /**
     * Setter for the isCheck value.
     *
     * @param value the new value for isCheck.
     */
    public void setCheck(boolean value) {
        isCheck = value;
    }

    /**
     *
     * @param pos The position.
     * @return <code>True</code> if the position is a safe position. This means
     * that the king cannot be captured on this position and that the king is
     * not check after moving to this position. <code>False</code> otherwise.
     */
    public boolean isSafePosition(Position pos) {
        for (int row = 0; row < ChessBoard.ROWS; row++) {
            for (int column = 0; column < ChessBoard.COLS; column++) {
                if (chessBoard.isOccupiedPosition(row, column)
                        && chessBoard.getColour(row, column) != getColour()) {
                    ChessPiece piece = chessBoard.getPiece(row, column);
                    if (piece.posCanBeCaptured(pos) || piece.posIsCovered(pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean posCanBeCaptured(Position p) {
        int distRow = Math.abs(p.getRow() - getRow());
        int distCol = Math.abs(p.getColumn() - getColumn());
        if (!p.isValid()) {
            // The position itself is not valid, so we return false
            return false;
        }
        if (distRow > 1 || distCol > 1 || p.equals(getPos())) {
            /*
            The position is not within reach or is the same as the current
            position of the king.
             */
            return false;
        } else if (!chessBoard.isOccupiedPosition(p)) {
            // This position is not occupied and within reach (previous if)
            return true;
        } else {
            /*
            We can only capture a position if the piece on it has a different
            colour than this piece has.
             */
            return chessBoard.getColour(p) != getColour();
        }
    }

    @Override
    public boolean posIsCovered(Position p) {
        for (int row = getRow() - 1; row <= getRow() + 1; row++) {
            for (int column = getColumn() - 1; column <= getColumn() + 1; column++) {
                Position newPos = new Position(row, column);
                if (newPos.isValid() && !(newPos.equals(getPos())) && newPos.equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
