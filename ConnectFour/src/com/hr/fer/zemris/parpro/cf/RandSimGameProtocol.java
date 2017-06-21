package com.hr.fer.zemris.parpro.cf;

import java.util.List;
import java.util.Random;

/**
 *
 * @author marko
 */
public class RandSimGameProtocol<S, M, P> implements GameProtocol<S, M, P> {
    private final Game<S, M, P> game;
    
    public RandSimGameProtocol(Game<S, M, P> game) {
        this.game = game;
    }
    
    @Override
    public void startGame() {
        game.resetGame();
        System.out.println(game.getWelcomeMsg());
        
        Random rand = new Random();
        
        while(! game.isOver()) {
            System.out.println();
            System.out.println("current state of the game:");
            System.out.println(game.getCurrState().toString().replaceAll("(?m)^", "  "));
            
            List<M> legalMoves = game.getLegalMoves();
            System.out.println("legal moves:");
            for (int i = 0; i < legalMoves.size(); i++) {
                System.out.format("  %d: move[%s]\n", i + 1, legalMoves.get(i));
            }
            
            M chosenMove = legalMoves.get(rand.nextInt(legalMoves.size()));
            
            System.out.print(game.getCurrPlayer() + ": " + chosenMove);
            
            
            try {
                Thread.sleep((long) 1000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            
          
            game.performMove(chosenMove);      
        }
        
        System.out.println();
        System.out.println("FINAL state of the game:");
        System.out.println(game.getCurrState().toString().replaceAll("(?m)^", "  "));
        System.out.format("GAME OVER! %s won.\nBye!\n", game.getPrevPlayer());
    }
    
}
