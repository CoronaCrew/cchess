package org.ccrew.cchess.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.ccrew.cchess.lib.ChessClock;
import org.ccrew.cchess.lib.ChessGame;
import org.ccrew.cchess.lib.ChessPiece;
import org.ccrew.cchess.lib.PGNError;
import org.ccrew.cchess.lib.ChessGame.MovedSource;
import org.ccrew.cchess.util.Handler;
import org.ccrew.cchess.util.SignalSource;

public class ChessBoard extends JPanel {

    private static final long serialVersionUID = 1L;

    Square[][] board = new Square[8][8];
    ChessGame game = null;
    Square selectedSquare = null;

    private ChessWindow window;

    public ChessBoard( ChessWindow window ) {

        setLayout(new GridLayout(8, 8));
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Square square = new Square(i, j);
                board[i][j] = square;
                add(square);
            }
        }

        try {
            setGame(new ChessGame());
        } catch (PGNError e) {
            e.printStackTrace();
        }

        game.moved.connect(handler);

        this.window = window;

        setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5, Color.BLACK));
    }

    public int index = 0;

    Handler<MovedSource, Class<Void>> handler = (MovedSource s) -> {
        processFen(s.getSource().moveStack.get(0).getFen());
        window.historyCombo.addItem(s.getMove().getSan());
        window.historyCombo.setSelectedIndex(window.historyCombo.getModel().getSize()-1);
        return Void.TYPE;
    };

    public Handler<SignalSource<ChessClock>, Class<Void>> whiteHandler = new Handler<SignalSource<ChessClock>,Class<Void>>(){
        @Override
        public Class<Void> handle(SignalSource<ChessClock> e) {
            int remainingSeconds = game.getClock().getWhiteRemainingSeconds();
            window.whiteTimeLabel.setText(String.format("%d:%d",remainingSeconds/60, remainingSeconds%60));
            return null;
        }
    };

    public Handler<SignalSource<ChessClock>, Class<Void>> blackHandler = new Handler<SignalSource<ChessClock>,Class<Void>>(){
        @Override
        public Class<Void> handle(SignalSource<ChessClock> e) {
            int remainingSeconds = game.getClock().getBlackRemainingSeconds();
            window.blackTimeLabel.setText(String.format("%d:%d",remainingSeconds/60, remainingSeconds%60));
            return null;
        }
    };

    public void setGame(ChessGame game) {
        if (game.getClock() != null) {
            game.getClock().tick.disconnect(whiteHandler);
            game.getClock().tick.disconnect(blackHandler);
            game.getClock().stop();
        }
        this.game = game;
        game.ended.connect(new Handler<SignalSource<ChessGame>,Class<Void>>(){
			@Override
			public Class<Void> handle(SignalSource<ChessGame> e) {
                switch (game.result) {
                    case WHITE_WON:
                        JOptionPane.showMessageDialog(null, "The winner is: WHITE");
                        break;
                    case BLACK_WON:
                    JOptionPane.showMessageDialog(null, "The winner is: BLACK");
                        break;
                    case DRAW:
                    JOptionPane.showMessageDialog(null, "DRAW");
                        break;
                    default:
                        break;
                }
                return null;
			}  
        });
        game.setClock(new ChessClock(180, 180));
        game.getClock().tick.connect(whiteHandler);
        game.getClock().tick.connect(blackHandler);
        game.start();
        processFen(game.getCurrentState().getFen());
        game.moved.connect(handler);
    }

    public void recursiveProcessFen(Iterator<ChessPiece> piece, int i) {
        if (piece.hasNext()) {
            ChessPiece chessPiece = piece.next();
            int rank = 7 - i / 8;
            int file = i % 8;
            if (chessPiece != null) {
                if(board[rank][file].piece==null || board[rank][file].piece.pieceType!=chessPiece.type || board[rank][file].piece.pieceColor!=chessPiece.getColor()){
                    board[rank][file].setPiece(new Piece(chessPiece.type, chessPiece.getColor()));
                    board[rank][file].repaint();
                    board[rank][file].revalidate();
                }
            } else {
                if(board[rank][file].piece!=null){
                    board[rank][file].setPiece(null);
                    board[rank][file].repaint();
                    board[rank][file].revalidate();
                }
            }
            recursiveProcessFen(piece, i+1);
        }
    }

    public void processFen(String fen) {

        try {
            if (game == null) {
                game = new ChessGame(fen);
                game.start();
            }
        } catch (PGNError e) {
            e.printStackTrace();
        }

        ChessPiece[] unprocessedBoard = game.moveStack.get(index).board;
        Iterator<ChessPiece> list = Arrays.asList(unprocessedBoard).iterator();
        recursiveProcessFen(list, 0);
    }

    // public void startingState() {

    //     board[6][0].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][1].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][2].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][3].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][4].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][5].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][6].setPiece(Piece.createWhite(PieceType.PAWN));
    //     board[6][7].setPiece(Piece.createWhite(PieceType.PAWN));

    //     board[1][0].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][1].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][2].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][3].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][4].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][5].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][6].setPiece(Piece.createBlack(PieceType.PAWN));
    //     board[1][7].setPiece(Piece.createBlack(PieceType.PAWN));

    //     board[0][0].setPiece(Piece.createBlack(PieceType.ROOK));
    //     board[0][1].setPiece(Piece.createBlack(PieceType.KNIGHT));
    //     board[0][2].setPiece(Piece.createBlack(PieceType.BISHOP));
    //     board[0][3].setPiece(Piece.createBlack(PieceType.QUEEN));
    //     board[0][4].setPiece(Piece.createBlack(PieceType.KING));
    //     board[0][5].setPiece(Piece.createBlack(PieceType.BISHOP));
    //     board[0][6].setPiece(Piece.createBlack(PieceType.KNIGHT));
    //     board[0][7].setPiece(Piece.createBlack(PieceType.ROOK));

    //     board[7][0].setPiece(Piece.createWhite(PieceType.ROOK));
    //     board[7][1].setPiece(Piece.createWhite(PieceType.KNIGHT));
    //     board[7][2].setPiece(Piece.createWhite(PieceType.BISHOP));
    //     board[7][3].setPiece(Piece.createWhite(PieceType.QUEEN));
    //     board[7][4].setPiece(Piece.createWhite(PieceType.KING));
    //     board[7][5].setPiece(Piece.createWhite(PieceType.BISHOP));
    //     board[7][6].setPiece(Piece.createWhite(PieceType.KNIGHT));
    //     board[7][7].setPiece(Piece.createWhite(PieceType.ROOK));

    // }

    @Override
    public Dimension getPreferredSize() {
        Container parent = getParent();
        int width = parent.getWidth();
        int height = parent.getHeight();
        int size = Math.min(width, height);
        return new Dimension(size, size);
    }

}
