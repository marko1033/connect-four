package com.hr.fer.zemris.parpro.cf;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

/**
 * Class abstracting a 
 *  <a href="https://en.wikipedia.org/wiki/Game_theory#Game_types">
 * two-player deterministic perfect-information zero-sum game</a>.
 *
 * @author Marko Bukal
 * @version 0.1
 * @param <S> the type of the state
 * @param <M> the type of the move
 * @param <P> the type of the player
 */
public abstract class Game<S, M, P> implements Serializable {
    private final P firstPlayer;
    private final P secondPlayer;
    
    private P currPlayer;
    
    private final Stack<M> movesHistory;
    
    public Game(P firstPlayer, P secondPlayer) {
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        
        // this one starts the game
        currPlayer = firstPlayer;
        movesHistory = new Stack<>();
    }
    
    // public abstract S getInitState();
    
    public abstract S getCurrState();
    
    // public abstract Set<S> getSuccStates();
    
    public abstract List<M> getLegalMoves();
    
    public final void performMove(M move) {
        if (! isLegalMove(move)) {
            throw new IllegalArgumentException("You provided an illegal move!");
        }
        
        changeState(move);
        
        movesHistory.push(move);
        
        currPlayer = getTheOtherPlayer();
    }
    
    
    // stupidity of Java protected access modifier -- this should only be visible to subclasses
    protected abstract void changeState(M move);
    
    public final boolean undoMove() {
        if (movesHistory.empty()) {
            return false;
        }
        
        cancelLastChangeToState();
        
        movesHistory.pop();
        currPlayer = getTheOtherPlayer();
        
        return true;
    }
    
    protected abstract void cancelLastChangeToState();

    public abstract boolean isLegalMove(M move);
    
    public abstract boolean isTerminalState(S state);
    
    public abstract boolean isOver();
    
    public abstract String getWelcomeMsg();
    
    public void resetGame() {
        // this one starts the game
        currPlayer = firstPlayer;
        movesHistory.clear();
        
        reset();
    }
    
    protected abstract void reset();
    
    private P getTheOtherPlayer() {
        return (currPlayer == firstPlayer) ? secondPlayer : firstPlayer;
    }
    
    public P getFirstPlayer() {
        return firstPlayer;
    }
    
    public P getSecondPlayer() {
        return secondPlayer;
    }
    
    public P getCurrPlayer() {
        return currPlayer;
    }
    
    public P getPrevPlayer() {
        return getTheOtherPlayer();
    }
    
    public M getLastMove() {
        if (movesHistory.empty()) {
            return null;
        }
        return movesHistory.peek();
    }

}
