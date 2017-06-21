package com.hr.fer.zemris.parpro.cf.mpistuff;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author marko
 */
public class Task implements Serializable {
    private final Integer[] movePath;
    private final Integer targetMove;
    
    public Task(Integer[] movePath, Integer targetMove) {
        this.movePath = movePath;
        this.targetMove = targetMove;
    }
    
    public Integer[] getMovePath() {
        return movePath;
    }
    
    public Integer getTargetMove() {
        return targetMove;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }
        Task other = (Task) o;
        return targetMove.equals(other.getTargetMove()) && Arrays.equals(movePath, other.getMovePath());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Arrays.deepHashCode(this.movePath);
        hash = 79 * hash + Objects.hashCode(this.targetMove);
        return hash;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("-----------------------\n");
        sb.append("move path:\n");
        sb.append("[ ");
        for (Integer move : movePath) {
            sb.append(move);
            sb.append(" ");
        }
        sb.append(" ]");
        sb.append("\n");
        sb.append("target move:\n");
        sb.append(targetMove);
        sb.append("\n");
        sb.append("#######################\n");
        return sb.toString();
    }
}
