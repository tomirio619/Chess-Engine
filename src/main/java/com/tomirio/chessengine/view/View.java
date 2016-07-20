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
package com.tomirio.chessengine.view;

import com.tomirio.chessengine.chessboard.ChessBoard;
import com.tomirio.chessengine.chessboard.ChessPiece;
import com.tomirio.chessengine.chessboard.Log;
import com.tomirio.chessengine.chessboard.Position;
import com.tomirio.chessengine.chessboard.State;
import com.tomirio.chessengine.controller.MouseListener;
import com.tomirio.chessengine.game.Game;
import com.tomirio.chessengine.moves.Move;
import java.util.ArrayList;
import java.util.LinkedList;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;

/**
 *
 * @author Tom Sandmann
 */
public final class View {

    /**
     * The borderpane.
     */
    public BorderPane borderPane;

    /**
     * The chessboard.
     */
    public ChessBoard chessBoard;

    /**
     * The gridpane.
     */
    public GridPane chessboardGrid;
    /**
     * The game
     */
    public Game game;

    /**
     * The imageloader
     */
    public ImageLoader imageLoader;

    /**
     * The labels
     */
    public LinkedList<Label> labels;
    /**
     * The log.
     */
    public Log log;
    /**
     * the mainWindow.
     */
    public Stage mainWindow;
    /**
     * The mouse listener, will be set on mouse clicked to every visual tile.
     */
    public MouseListener mouseListener;
    /**
     * The stackpane.
     */
    public StackPane root;
    /**
     * The state.
     */
    public State state;
    /**
     * The visualBoard contains all the visual tiles.
     */
    public VisualTile[][] visualBoard;

    /**
     *
     * @param primaryStage The primary stage.
     */
    public View(Stage primaryStage) {
        log = new Log();
        state = new State();
        imageLoader = new ImageLoader();
        createMainWindow(primaryStage);
    }

    /**
     * Adds listeners to the width and height property of the gridpane.
     */
    private void addResizeHandlers() {
        chessboardGrid.widthProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) -> {
            handleResize(chessboardGrid.getWidth(), chessboardGrid.getHeight());
        });
        chessboardGrid.heightProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) -> {
            handleResize(chessboardGrid.getWidth(), chessboardGrid.getHeight());
        });

    }

    private void createMainWindow(Stage primaryStage) {
        mainWindow = primaryStage;
        visualBoard = new VisualTile[8][8];
        chessBoard = new ChessBoard();
        game = new Game(state, chessBoard, this);
        mouseListener = new MouseListener(this, game);

        // Root will contain every visual aspect
        root = new StackPane();
        borderPane = new BorderPane();
        chessboardGrid = new GridPane();
        labels = new LinkedList<>();

        // Initialize
        createVisualTiles();
        addResizeHandlers();
        setLabels();

        // Create graphical structure
        BorderPane visualChessBoard = new BorderPane();
        chessboardGrid.setAlignment(Pos.CENTER);
        visualChessBoard.setCenter(chessboardGrid);

        Image doMoveImage = new Image("png/buttons/do.png");
        Image undoMoveImage = new Image("png/buttons/undo.png");

        Button b2 = new Button();
        b2.setGraphic(new ImageView(doMoveImage));
        Button b1 = new Button();
        b1.setGraphic(new ImageView(undoMoveImage));
        HBox buttonBar = new HBox();
        buttonBar.getChildren().addAll(b1, b2);
        buttonBar.setAlignment(Pos.TOP_CENTER);
        visualChessBoard.setBottom(buttonBar);

        borderPane.setCenter(visualChessBoard);
        borderPane.setRight(log);
        root.getChildren().add(borderPane);
        Scene mainWindowScene = new Scene(root);
        mainWindow.getIcons().add(ImageLoader.icon);
        mainWindow.setTitle("Chess");
        mainWindow.centerOnScreen();
        mainWindow.setScene(mainWindowScene);
        mainWindow.sizeToScene();
        mainWindow.setResizable(true);
        mainWindow.show();
        mainWindow.setMinWidth(primaryStage.getWidth());
        mainWindow.setMinHeight(primaryStage.getHeight());

    }

    /**
     * Create the visual tiles. Also add the mouse listener to the visual tile.
     */
    private void createVisualTiles() {
        for (int row = 0; row < ChessBoard.ROWS; row++) {
            for (int column = 0; column < ChessBoard.COLS; column++) {
                VisualTile t;
                if (chessBoard.isOccupiedPosition(row, column)) {
                    ChessPiece p = chessBoard.getPiece(row, column);
                    t = new VisualTile(row, column, p);
                } else {
                    t = new VisualTile(row, column);
                }
                t.setInitialTileImageAndChessPiece();
                t.setOnMouseClicked(mouseListener);
                visualBoard[row][column] = t;
                chessboardGrid.add(t, column + 1, row + 1);
            }
        }
    }

    /**
     * Draws the current board on the screen.
     */
    public void drawBoard() {
        removeChessboard();
        double currentSize = visualBoard[0][0].getPrefWidth();
        visualBoard = new VisualTile[ChessBoard.ROWS][ChessBoard.COLS];
        for (int row = 0; row < ChessBoard.ROWS; row++) {
            for (int column = 0; column < ChessBoard.COLS; column++) {
                VisualTile t = new VisualTile(row, column,
                        chessBoard.getPiece(row, column), currentSize, currentSize);
                t.updateTileImage(currentSize);
                t.setOnMouseClicked(mouseListener);
                visualBoard[row][column] = t;
                chessboardGrid.add(t, column + 1, row + 1);
            }
        }
        updateLabelSize(currentSize);
    }

    /**
     * Handles resize of the scene. It makes sure that the chessboard structure
     * maintains a square ratio.
     *
     * @param gridWidth The current width of the gridpane
     * @param gridHeight The current height of the gridpane
     */
    private void handleResize(double gridWidth, double gridHeight) {
        double gridMinWidthHeight = Math.min(gridWidth, gridHeight);
        double newSquareSize = Math.floor(gridMinWidthHeight / 9.0);
        if (newSquareSize < visualBoard[0][0].getMinWidth()) {

        } else {
            for (VisualTile[] columns : visualBoard) {
                for (VisualTile visualTile : columns) {
                    visualTile.setPrefSize(newSquareSize, newSquareSize);
                }
            }
            labels.stream().forEach((l) -> {
                l.setPrefSize(newSquareSize, newSquareSize);
            });

        }
    }

    /**
     * Removes the chessboard structure from the gridpane. This must be done
     * when a new chessboard structure will be added to the gridpane.
     */
    public void removeChessboard() {
        for (VisualTile[] columns : visualBoard) {
            for (VisualTile visualTile : columns) {
                chessboardGrid.getChildren().remove(visualTile);
            }
        }
    }

    /**
     *
     * @param possibleMoves The possible moves for this chess piece.
     */
    public void removeTilesAsMoves(ArrayList<Move> possibleMoves) {
        possibleMoves.stream().forEach((move) -> {
            Position newPos = move.getNewPos();
            visualBoard[newPos.getRow()][newPos.getColumn()].removeAsPossibleMove();
        });
    }

    /**
     * Set the labels in the GUI. This is called when the program is first
     * started.
     */
    private void setLabels() {
        Label tmp = new Label("");
        tmp.setContentDisplay(ContentDisplay.TEXT_ONLY);
        tmp.setStyle("-fx-background-color: #500000 ; "
                + "-fx-border-color: lightblue ; ");
        tmp.setTextFill(Paint.valueOf("white"));
        tmp.setPrefSize(VisualTile.WIDTH, VisualTile.HEIGHT);
        tmp.setMinSize(VisualTile.WIDTH, VisualTile.HEIGHT);
        labels.add(tmp);
        chessboardGrid.add(tmp, 0, 0);

        // Adding the labels on the rows
        for (int column = 0; column < ChessBoard.COLS; column++) {
            char c = Character.toChars(97 + column)[0];
            Label l = new Label(String.valueOf(c));
            l.setContentDisplay(ContentDisplay.TEXT_ONLY);
            l.setAlignment(Pos.CENTER);
            l.setPrefSize(VisualTile.WIDTH, VisualTile.HEIGHT);
            l.setMinSize(VisualTile.WIDTH, VisualTile.HEIGHT);
            l.setStyle("-fx-background-color: #500000 ; "
                    + "-fx-border-color:  lightblue ; ");
            l.setTextFill(Paint.valueOf("white"));
            labels.add(l);
            chessboardGrid.add(l, column + 1, 0);
        }

        // Adding the labels on the columns
        for (int row = 0; row < ChessBoard.ROWS; row++) {
            Label l = new Label(Integer.toString(8 - row));
            l.setContentDisplay(ContentDisplay.TEXT_ONLY);
            l.setAlignment(Pos.CENTER);
            l.setPrefSize(VisualTile.WIDTH, VisualTile.HEIGHT);
            l.setMinSize(VisualTile.WIDTH, VisualTile.HEIGHT);
            l.setStyle("-fx-background-color: #500000 ; "
                    + "-fx-border-color:  lightblue ; ");
            l.setTextFill(Paint.valueOf("white"));
            labels.add(l);
            chessboardGrid.add(l, 0, row + 1);
        }
    }

    /**
     *
     * @param possibleMoves The possible moves for this chess piece.
     */
    public void showTilesAsMoves(ArrayList<Move> possibleMoves) {
        possibleMoves.stream().forEach((move) -> {
            Position newPos = move.getNewPos();
            visualBoard[newPos.getRow()][newPos.getColumn()].showAsPossibleMove();
        });
    }

    /**
     * Update the view.
     *
     * @param chessBoard The chess board.
     */
    public void update(ChessBoard chessBoard) {

//        else{
//            ChessBoard b = (ChessBoard) arg;
//            chessBoard = b;
//            state = chessBoard.getState();
//            if (state.weHaveAWinner()) {
//                System.out.println("The winner is " + state.getWinner());
//            } else if (state.isDraw()) {
//                System.out.println("It is a draw!");
//            }
        drawBoard();
    }

    /**
     * Updates all the labels to the new size.
     *
     * @param newSize The new size of the labels.
     */
    private void updateLabelSize(double newSize) {
        labels.stream().forEach((l) -> {
            l.setPrefSize(newSize, newSize);
        });
    }
}
