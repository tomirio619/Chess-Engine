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
package com.tomirio.schakert.game;

import com.tomirio.schakert.agent.AI;
import com.tomirio.schakert.chessboard.ChessBoard;
import com.tomirio.schakert.chessboard.ChessColour;
import com.tomirio.schakert.chesspieces.King;
import com.tomirio.schakert.moves.Move;
import com.tomirio.schakert.view.Log;
import com.tomirio.schakert.view.View;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 *
 * @author Tom Sandmann
 */
public class Game {

    /**
     * Index of the move we have done.
     */
    private int appliedMove;
    /**
     * Black player.
     */
    public Player blackPlayer;
    /**
     * The chess board.
     */
    private ChessBoard chessBoard;
    /**
     * The colour having turn.
     */
    private ChessColour hasTurn;

    /**
     * The log.
     */
    private final Log log;
    /**
     * All the moves that led to the current state of the chess board.
     */
    private final ArrayList<Move> moveList;
    /**
     * The view.
     */
    private final View view;
    /**
     * White player
     */
    public Player whitePlayer;
    /**
     * The colour of the player that wins.
     */
    private ChessColour winner;

    /**
     * Constructor
     *
     * @param chessBoard The chessboard.
     * @param view The view.
     * @param log The log.
     */
    public Game(ChessBoard chessBoard, View view, Log log) {
        this.log = log;
        this.chessBoard = chessBoard;
        this.view = view;
        moveList = new ArrayList<>();
        // We have not applied any move, so the index of the applied move is -1
        appliedMove = -1;
        hasTurn = ChessColour.White;
        whitePlayer = new HumanPlayer(ChessColour.White, chessBoard);
        blackPlayer = new HumanPlayer(ChessColour.Black, chessBoard);

        String startingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
        String behtingStudy = "8/8/7p/3KNN1k/2p4p/8/3P2p1/8 w - -";
        loadFEN(behtingStudy);
        notifyPlayers();
    }

    /**
     * Calls the right method if an agent has turn.
     */
    public void agentPlay() {
        // User cannot do and undo moves while agent will calculate the best move
        view.disableMoveButtons();
        ExecutorService es = Executors.newSingleThreadExecutor();
        Future<Move> agentPlay = (hasTurn == ChessColour.Black)
                ? es.submit((Callable) blackPlayer) : es.submit((Callable) whitePlayer);
        new Thread() {
            @Override
            public void run() {
                try {
                    Move moveToPlay = agentPlay.get();
                    moveList.add(moveToPlay);
                    // Agent has fount the movce to play, enable doing and undoing of moves
                    Platform.runLater(() -> view.enableMoveButtons());
                    Platform.runLater(() -> doMove());
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }.start();
    }

    /**
     * Do the move.
     */
    public void doMove() {
        if (moveList.size() > 0) {
            // There are moves in the list
            if (appliedMove + 1 < moveList.size()) {
                // There is a next move in the list we can apply
                Move move = moveList.get(appliedMove + 1);
                /*
                We need to add the move before it is applied.
                This due to toString() using doMove() and undoMove() to determine
                the PGN notaton of the move.
                 */
                log.addMove(move);
                move.doMove();
                appliedMove++;
                view.update(chessBoard);
                updateTurn();
                updateGameStatus();
                /**
                 * When redoing a move made in the past, and both players are
                 * AI, we do not want the AI play further. Only let them play
                 * when the applied move is points to...
                 */
                if (blackPlayer instanceof AI && whitePlayer instanceof AI) {
                    if (appliedMove + 1 == moveList.size()) {
                        /**
                         * When doing and undoing moves, we only want the AI to
                         * continu playing when whe redid the last known move.
                         */
                        notifyPlayers();
                    }
                } else {
                    notifyPlayers();
                }
            }
        }
    }
    public ChessBoard getBoard() {
        return chessBoard;
    }

    /**
     * Get the player that belongs to the given color.
     *
     * @param playerColour The color of the player.
     * @return The player that plays with this color.
     */
    public Player getPlayer(ChessColour playerColour) {
        switch (playerColour) {
            case Black:
                return blackPlayer;
            case White:
                return whitePlayer;
            default:
                throw new NoSuchElementException();
        }
    }

    /**
     * Get the colour of the player that currently has turn.
     *
     * @return ChessColour having turn.
     */
    public ChessColour getTurnColour() {
        return hasTurn;
    }

    /**
     * Handles a chess move done by a human player.
     *
     * @param move The move.
     */
    public void humanPlay(Move move) {
        removeObsoleteMoves();
        moveList.add(move);
        doMove();
    }

    /**
     * For a given colour, return if this colour is checkmate.
     *
     * @param colour The colour of the player.
     * @return <code>True</code> if the player with that colour is checkmate,
     * <code>False</code> otherwise.
     */
    public boolean inCheckmate(ChessColour colour) {
        return chessBoard.inCheckmate(colour);
    }


    public final void loadFEN(String FEN) {
        FENParser fenParser = new FENParser(FEN);
        chessBoard = fenParser.getChessBoard();
        hasTurn = fenParser.getHasTurn();
    }
    /**
     * Notifies the right player after a move has been made. If the next player
     * is a human player, he will be able to make his move by interacting with
     * the GUI.
     */
    public final void notifyPlayers() {
        if (weHaveAWinner() || chessBoard.inStalemate()) {
            // The game has ended
        } else {
            switch (hasTurn) {
                case Black:
                    if (blackPlayer instanceof AI) {
                        agentPlay();
                    }
                    break;
                case White:
                    if (whitePlayer instanceof AI) {
                        agentPlay();
                    }
                    break;
            }
        }
    }

    /**
     * When the makes a move, while there are other moves avaiable in the move
     * list, we have to remove those 'obsolete' move. That is exactly what this
     * function does.
     */
    public void removeObsoleteMoves() {
        if (moveList.size() - 1 != appliedMove) {
            // We are not looking at the most 'recent' move
            if (appliedMove == -1) {
                // We are in our initial position, clear all the moves.
                moveList.clear();
            } else if (!moveList.isEmpty()) {
                // Remove the obsolete moves.
                moveList.subList(appliedMove + 1, moveList.size()).clear();
            }
        }
    }

    /**
     * Undo the move
     */
    public void undoMove() {
        if (appliedMove > -1) {
            // There is a move to undo
            if (appliedMove < moveList.size()) {
                // we can index the move
                Move move = moveList.get(appliedMove);
                /*
                Before undoing the move, log will check whether the current state
                was an end state (check mate, stale mate). Based on this information,
                it will update the move log correctly.
                 */
                log.undoMove();
                move.undoMove();

                appliedMove--;

                updateTurn();
                view.update(chessBoard);
            }
        }
    }

    /**
     * Update the status of by updating the variables indicating that a certain
     * colour is check mate. Also sets the value for stalemate.
     */
    public void updateGameStatus() {
        King blackKing = chessBoard.getKing(ChessColour.Black);
        King whiteKing = chessBoard.getKing(ChessColour.White);

        if (whiteKing.getPossibleMoves().isEmpty()
                && !whiteKing.inCheck()
                && !chessBoard.canMakeAMove(ChessColour.White)
                || blackKing.getPossibleMoves().isEmpty()
                && !blackKing.inCheck()
                && !chessBoard.canMakeAMove(ChessColour.Black)) {
            log.gameFinished();
        } else if (whiteKing.inCheck()
                && whiteKing.getPossibleMoves().isEmpty()
                && !chessBoard.canMakeAMove(ChessColour.White)) {
            winner = ChessColour.Black;
            log.gameFinished();
        } else if (blackKing.inCheck()
                && blackKing.getPossibleMoves().isEmpty()
                && !chessBoard.canMakeAMove(ChessColour.Black)) {
            winner = ChessColour.White;
            log.gameFinished();
        }
    }

    /**
     * Update the colour of the player having turn.
     */
    public void updateTurn() {
        hasTurn = (hasTurn == ChessColour.White) ? ChessColour.Black : ChessColour.White;
    }

    /**
     *
     * @return <code>True</code> if the current chess game has a winner.
     * <code>False</code> otherwise.
     */
    public boolean weHaveAWinner() {
        return winner != null;
    }


}