package com.hr.fer.zemris.parpro.cf.concretegames;

import com.hr.fer.zemris.parpro.cf.Game;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author marko
 */
public class ConnectFour extends Game<Board, Integer, String> {

    private final Board currBoard;

    public ConnectFour(String firstPlayer, String secondPlayer, int rowCnt, int colCnt) {
        super(firstPlayer, secondPlayer);

        currBoard = new Board(rowCnt, colCnt);
    }
    

    @Override
    public Board getCurrState() {
        return currBoard;
    }

    @Override
    public List<Integer> getLegalMoves() {
        List<Integer> legalMoves = new ArrayList<>();

        for (int i = 0; i < currBoard.getColCnt(); i++) {
            if (currBoard.getTopRowCellLabel(i) == Board.CellLabel.Empty) {
                legalMoves.add(i);
            }
        }

        return legalMoves;
    }

    @Override
    protected void changeState(Integer move) {
        int col = move;// here "move" is index of column the disc will be placed in
        int row = 0;
        while (row < currBoard.getRowCnt()
                && currBoard.getCellLabel(row, col) == Board.CellLabel.Empty) {
            row++;
        }
        // this one is ours
        row--;

        currBoard.setCellLabel(row, col, getPlayerColor(getCurrPlayer()));
    }

    @Override
    public boolean isLegalMove(Integer move) {
        return getLegalMoves().contains(move);
    }

    @Override
    public boolean isTerminalState(Board state) {
        for (int col = 0; col < state.getColCnt(); col++) {
            if (isTerminalStateFrom(state, col)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isTerminalStateFrom(Board state, int col) {
        // find first disc in current column
        int row = 0;
        while (row < state.getRowCnt()
                && state.getCellLabel(row, col) == Board.CellLabel.Empty) {
            row++;
        }
        if (row == state.getRowCnt()) { // no discs in this column
            return false;
        }
        // now we know that first disc is at (row, col)
        Board.CellLabel targetLabel = state.getCellLabel(row, col);
        // check for the following:

        // 1) 4 in a row vertically ?
        if (row + 3 < state.getRowCnt()
                && state.getCellLabel(row, col) == state.getCellLabel(row + 1, col)
                && state.getCellLabel(row + 1, col) == state.getCellLabel(row + 2, col)
                && state.getCellLabel(row + 2, col) == state.getCellLabel(row + 3, col)) {
            return true;
        }

        // 2) 4 in a row horizontally?   
        boolean leftJammed = false;
        int left = col;
        boolean rightJammed = false;
        int right = col;
        while (!(leftJammed && rightJammed)) {
            if (!leftJammed) {
                if (left - 1 >= 0 && state.getCellLabel(row, left - 1) == targetLabel) {
                    left--;
                } else {
                    leftJammed = true;
                }
            }
            if (!rightJammed) {
                if (right + 1 < state.getColCnt() && state.getCellLabel(row, right + 1) == targetLabel) {
                    right++;
                } else {
                    rightJammed = true;
                }
            }
            if (right - left >= 3) {
                return true;
            }
        }

        // 3) 4 in a row diagonally ?
        boolean neJammed = false;
        boolean nwJammed = false;
        boolean seJammed = false;
        boolean swJammed = false;
        int neRow = row;
        int neCol = col;
        int nwRow = row;
        int nwCol = col;
        int seRow = row;
        int seCol = col;
        int swRow = row;
        int swCol = col;

        while (!(neJammed && nwJammed && seJammed && swJammed)) {
            if (!neJammed) {
                if (neRow - 1 >= 0 && neCol + 1 < state.getColCnt()) {
                    if (state.getCellLabel(neRow - 1, neCol + 1) == targetLabel) {
                        neRow--;
                        neCol++;
                    } else {
                        neJammed = true;
                    }
                } else {
                    neJammed = true;
                }
            }
            if (!nwJammed) {
                if (nwRow - 1 >= 0 && nwCol - 1 >= 0) {
                    if (state.getCellLabel(nwRow - 1, nwCol - 1) == targetLabel) {
                        nwRow--;
                        nwCol--;
                    } else {
                        nwJammed = true;
                    }
                } else {
                    nwJammed = true;
                }
            }
            if (!seJammed) {
                if (seRow + 1 < state.getRowCnt() && seCol + 1 < state.getColCnt()) {
                    if (state.getCellLabel(seRow + 1, seCol + 1) == targetLabel) {
                        seRow++;
                        seCol++;
                    } else {
                        seJammed = true;
                    }
                } else {
                    seJammed = true;
                }
            }
            if (!swJammed) {
                if (swRow + 1 < state.getRowCnt() && swCol - 1 >= 0) {
                    if (state.getCellLabel(swRow + 1, swCol - 1) == targetLabel) {
                        swRow++;
                        swCol--;
                    } else {
                        swJammed = true;
                    }
                } else {
                    swJammed = true;
                }
            }
            if (neCol - swCol >= 3) {
                assert (swRow - neRow == neCol - swCol);
                return true;
            }
            if (seCol - nwCol >= 3) {
                assert (seRow - nwRow == neCol - swCol);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOver() {
        if (getLastMove() == null) { // the game hasn't really started
            return false;
        }
        return isTerminalStateFrom(currBoard, getLastMove());
        //return isTerminalState(currBoard);
    }

    private Board.CellLabel getPlayerColor(String player) {
        if (player.equals(getFirstPlayer())) {
            return Board.CellLabel.Yellow;
        } else if (player.equals(getSecondPlayer())) {
            return Board.CellLabel.Red;
        } else {
            throw new IllegalArgumentException("Uknown player: " + player);
        }
    }

    @Override
    public String getWelcomeMsg() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("Player %s gets YELLOW discs. Player %s gets RED discs. \n",
                getFirstPlayer(), getSecondPlayer()));
        sb.append(String.format("Player %s starts first.", getFirstPlayer()));

        return sb.toString();
    }

    @Override
    public void reset() {
        currBoard.reset();
    }

    @Override
    protected void cancelLastChangeToState() {
        int col = getLastMove();// here "move" is index of column the disc will be placed in
        int row = 0;
        while (row < currBoard.getRowCnt()
                && currBoard.getCellLabel(row, col) == Board.CellLabel.Empty) {
            row++;
        }

        currBoard.setCellLabel(row, col, Board.CellLabel.Empty);
    }


}
