package com.hr.fer.zemris.parpro.cf.eval.core;

import com.hr.fer.zemris.parpro.cf.Game;
import java.util.List;

/**
 *
 * @author marko
 */
public class BruteForceEvaluator<S, M, P> implements Evaluator<S, M, P> {
    private int maxDepth;
    private final Game<S, M, P> game;
    private P clientPlayer;
    
    public BruteForceEvaluator(Game<S, M, P> game, int maxDepth) {
        this.game = game;
        this.maxDepth = maxDepth;
    }
    
    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }
    
    public int getMaxDepth() {
        return maxDepth;
    }
    
    @Override
    public double evaluate(M move, P player) {
        clientPlayer = player;
        return evaluateRecursively(move, maxDepth);
    }
    
    
    @Override
    public double evaluate(M move) {
        clientPlayer = game.getCurrPlayer();
        return evaluateRecursively(move, maxDepth);
    }
    
    private double evaluateRecursively(M move, int depth) {
        if (depth == 0) {
            return 0;
        }
        game.performMove(move);
        if (game.isOver()) {
            game.undoMove();
            if (game.getCurrPlayer() == clientPlayer) {
                return 1;
            } else {
                return -1;
            }
        }
        double totalEval = 0;
        boolean allLose = true;
        boolean allWin = true;
        List<M> legalMoves = game.getLegalMoves();
        for (M newMove : legalMoves) {
            double eval = evaluateRecursively(newMove, depth - 1);
            if (eval > -1) {
                allLose = false;
            }
            if (eval != 1) {
                allWin = false;
            }
            if (eval == 1 && game.getCurrPlayer() == clientPlayer) {
                game.undoMove();
                return 1;
            }
            if (eval == -1 &&  game.getCurrPlayer() != clientPlayer) {
                game.undoMove();
                return -1;
            }
            totalEval += eval;
        }
        game.undoMove();
        if (allWin) {    
            return 1;
        }
        if (allLose) {
            return -1;
        }
        return totalEval/(double)legalMoves.size();
    }

    @Override
    public void close() {
        
    }

}
