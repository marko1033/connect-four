
package com.hr.fer.zemris.parpro.cf.concretegames;

import java.io.Serializable;
import java.util.Arrays;

/**
 *
 * @author marko
 */
public final class Board implements Serializable {
    public enum CellLabel {
        Empty, Yellow, Red;
    }
    
    private final CellLabel[][] grid;
    private final int rowCnt;
    private final int colCnt;
    
    public Board(int rowCnt, int colCnt) {
        grid = new CellLabel[rowCnt][colCnt];
        this.rowCnt = rowCnt;
        this.colCnt = colCnt;
        
        reset();
    }
    
    private Board(CellLabel[][] grid) {
        this.grid = grid;
        this.rowCnt = grid.length;
        this.colCnt = grid[0].length;
    }
    
    public CellLabel[][] getGrid() {
        return grid;
    }
    
    public int getRowCnt() {
        return rowCnt;
    }
    
    public int getColCnt() {
        return colCnt;
    }
    
    public void setCellLabel(int i, int j, CellLabel cl) {
        grid[i][j] = cl;
    }
    
    public CellLabel getCellLabel(int i, int j) {
        return grid[i][j];
    }
    
    public CellLabel getTopRowCellLabel(int j) {
        return grid[0][j];
    }
    
    public void reset() {
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < colCnt; j++) {
                grid[i][j] = CellLabel.Empty;
            }
        }
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < colCnt; j++) {
                sb.append(cellLabelChar(grid[i][j]));
                if(j != colCnt - 1) {
                    sb.append(" ");
                } else {
                    sb.append("\n");
                }   
            }
        }
        
        return sb.toString();
    }
    
    private static String cellLabelChar(CellLabel cl) {
        switch(cl) {
            case Empty:
                return "o";
            case Yellow:
                return "Y";
            default:
                return "R";
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != getClass()) {
            return false;
        }
        
        Board other = (Board) o;
        
        if (rowCnt != other.getRowCnt() || colCnt != other.getColCnt()) {
            return false;
        }
        
        CellLabel[][] otherGrid = other.getGrid();
        
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < colCnt; j++) {
                if (grid[i][j] != otherGrid[i][j]) {
                    return false;
                }
            }
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Arrays.deepHashCode(this.grid);
        hash = 47 * hash + this.rowCnt;
        hash = 47 * hash + this.colCnt;
        return hash;
    }
    
    public Board copy() {
        CellLabel[][] twinGrid = new CellLabel[rowCnt][colCnt];
        for (int i = 0; i < rowCnt; i++) {
            System.arraycopy(grid[i], 0, twinGrid[i], 0, colCnt);
        }
        return new Board(twinGrid);
    }
    
}
