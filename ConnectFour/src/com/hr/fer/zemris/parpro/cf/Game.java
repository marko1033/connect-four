package com.hr.fer.zemris.parpro.cf;

import java.io.Serializable;
import java.util.List;
import java.util.Stack;

/**
 * Class abstracting a 
 *  <a href="https://en.wikipedia.org/wiki/Game_theory#Game_types">
 * two-player deterministic perfect-information zero-sum game</a>.
 * 
 * <p>
 * Game described above at each moment is in some state. Players make moves
 * and thus change the mentioned state. The state and move objects are of generic
 * type. Player is also generic, although only names (String) of the players are
 * currently needed. So it is necessary for a "Player"-object to override toString
 * method of Object superclass.
 * </p>
 * 
 * <p>
 * Class itself is abstract. It only provides bodies of functions that are
 * same for all derived classes. Concrete classes need to provide concrete types
 * of state, move and player in addition to implementing abstract methods.
 * </p>
 * 
 * <p>
 * Objects of this type are supposed to be serialized and sent over socket.
 * </p>
 * 
 * @author Marko Bukal
 * @version 0.1
 * 
 * @param <S> type of the state
 * @param <M> type of the move
 * @param <P> type of the player
 */
public abstract class Game<S, M, P> implements Serializable {
    private final P firstPlayer;
    private final P secondPlayer;
    
    private P currPlayer;
    
    private final Stack<M> movesHistory;
    
    /**
     * Base constructor for creating "Game"-objects. <br>
     * By convention, the first player provided in constructor starts the game, 
     * in other words makes the first move.
     * 
     * @param firstPlayer player #1 that takes part in the game
     * @param secondPlayer player #2 that takes part in the game
     */
    public Game(P firstPlayer, P secondPlayer) {
        // initialize the players' data
        this.firstPlayer = firstPlayer;
        this.secondPlayer = secondPlayer;
        
        // the first player provided starts the game
        currPlayer = firstPlayer;
        
        // moves history gets initialized
        movesHistory = new Stack<>();
    }
    
    /**
     * Getter that returns current state object.
     * 
     * @return current state
     */
    public abstract S getCurrState();
    
    
    /**
     * Getter that returns all legal moves available from current state.
     * 
     * @return all possible legal moves (as a list object)
     */
    public abstract List<M> getLegalMoves();
    
    
    /**
     * Makes changes to the current state by applying specified move. <br>
     * This method only serves as a template method.
     * The essential part of the job, that is changing the internals (state of
     * the game) is delegated to the abstract method of similar name.
     * 

     * @param move 
     */
    public final void performMove(M move) {
        if (isTerminalState(getCurrState())) {
            throw new IllegalArgumentException("The state of the game is terminal."
                    + " No more moves can be made!");
        }
        
        if (! isLegalMove(move)) {
            throw new IllegalArgumentException("You provided an illegal move!");
        }
        
        // apply the move 
        changeState(move);
        
        // record the move
        movesHistory.push(move);
        
        // change turn
        currPlayer = getTheOtherPlayer();
    }
    
    
    /**
     * Abstract method that changes the state of the game according to some
     * internal logic of a derived class.
     * 
     * Stupidity of Java protected access modifier is visible here. <br>
     * This method should only be visible to subclasses of a "Game"-class.
     * 
     * @param move Move object that ignites the change.
     */
    protected abstract void changeState(M move);
    
    
    /**
     * Method cancels the last move. <br>
     * This method only serves as a template method. Canceling the changes to
     * the state is delegated to the appropriate method. <br>
     * If the game is in the starting position and no moves have been made,
     * boolean value "false" is returned. In all other (successful) cases, "true"
     * is returned.
     * 
     * @return true if there are moves to be canceled, false otherwise
     */
    public final boolean undoMove() {
        if (movesHistory.empty()) {
            // we're already done!
            return false;
        }
        
        cancelLastChangeToState();
        movesHistory.pop();
        currPlayer = getTheOtherPlayer();
        
        return true;
    }
    
    
    /**
     * Abstract method that nullifies the changes last move has made to the state
     * of the game.
     */
    protected abstract void cancelLastChangeToState();

    
    /**
     * Method that decides if provided move is legal in this state or not.
     * 
     * @param move Move that undergoes the testing.
     * @return true if move is indeed legal, false otherwise
     */
    public abstract boolean isLegalMove(M move);
    
    
    /**
     * Method that checks if the provided state is terminal. <br>
     * Terminal state is such from which no more moves can be made because the
     * game has finished according to internal rules of the concrete game.
     * 
     * @param state state to be checked.
     * @return true if state is indeed terminal, false otherwise
     */
    public abstract boolean isTerminalState(S state);
    
    
    /**
     * Method that checks if the game is over. <br>
     * It provides the obvious default implementation that simply checks weather
     * the current state is terminal. Additional optimizations to this method
     * can be made if some internals of a concrete class in question are known so
     * overriding the method is something to consider.
     * 
     * @return true if the game is over, false otherwise
     */
    public boolean isOver() {
        return isTerminalState(getCurrState());
    }
    
    
    /**
     * Method of less importance that returns the welcome message that can be
     * printed out when the game starts.
     * 
     * @return welcome message
     */
    public abstract String getWelcomeMsg();
    
    
    /**
     * Method that resets the game. <br>
     * The first player starts the game and the whole moves history is erased.
     * The internal state changes to the initial one and that part of the job is
     * delegated to the concrete class.
     */
    public void resetGame() {
        // this one starts the game
        currPlayer = firstPlayer;
        movesHistory.clear();
        
        reset();
    }
    
    
    /**
     * Abstract method whose task is to set the state to the starting position.
     */
    protected abstract void reset();
    
    
    /**
     * Method returns the player whose turn it <b>isn't</b> to make the move.
     * 
     * @return the player who waits for a turn
     */
    private P getTheOtherPlayer() {
        return (currPlayer == firstPlayer) ? secondPlayer : firstPlayer;
    }
    
    
    /**
     * Getter that returns player #1
     * 
     * @return first player
     */
    public P getFirstPlayer() {
        return firstPlayer;
    }
    
    
    /**
     * Getter that returns player #2
     * 
     * @return second player
     */
    public P getSecondPlayer() {
        return secondPlayer;
    }
    
    
    /**
     * Getter that returns player who makes the move.
     * 
     * @return current player
     */
    public P getCurrPlayer() {
        return currPlayer;
    }
    
    
    /**
     * Getter that returns player who made the previous move. <br>
     * If the game has just started and no moves have been made, null will be
     * returned.
     * 
     * @return previous player, null if no moves have been made
     */
    public P getPrevPlayer() {
        if (movesHistory.empty()) {
            return null;
        }
        return getTheOtherPlayer();
    }
    
    
    /**
     * Getter that returns the last move made.
     * If the game has just started and no moves have been made, null will be
     * returned.
     * 
     * @return last move, null if no moves have been made
     */
    public M getLastMove() {
        if (movesHistory.empty()) {
            return null;
        }
        return movesHistory.peek();
    }

}
