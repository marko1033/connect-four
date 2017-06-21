package com.hr.fer.zemris.parpro.cf;

import java.util.List;
import java.util.Scanner;

/**
 *
 * @author marko
 */
public class StandardGameProtocol<S, M, P> implements GameProtocol<S, M, P> {
    private final Game<S, M, P> game;
    
    public StandardGameProtocol(Game<S, M, P> game) {
        this.game = game;
    }
    
    @Override
    public void startGame() {
        game.resetGame();
        System.out.println(game.getWelcomeMsg());
        
        Scanner sc = new Scanner(System.in);
        
        while(! game.isOver()) {
            System.out.println();
            System.out.println("current state of the game:");
            System.out.println(game.getCurrState().toString().replaceAll("(?m)^", "  "));
            
            List<M> legalMoves = game.getLegalMoves();
            System.out.println("legal moves:");
            for (int i = 0; i < legalMoves.size(); i++) {
                System.out.format("  %d: move[%s]\n", i + 1, legalMoves.get(i));
            }
            
            int chosenMoveIndex = -1;
            while (true) {
                System.out.println("Enter number before the chosen move!");
                System.out.print(game.getCurrPlayer() + ": ");
                String input = sc.nextLine();
                
                if (input.equals("UNDO")) {
                    chosenMoveIndex = -42;
                    break;
                }
                
                try {
                    chosenMoveIndex = Integer.parseInt(input);
                } catch (NumberFormatException nfe) {
                    System.out.println("Not an integer!");
                    continue;
                }
                
                if (chosenMoveIndex < 1 || chosenMoveIndex > legalMoves.size()) {
                    System.out.println("Number not in range!");
                    continue;
                }
                
                break;
            }
            assert(chosenMoveIndex != -1);
            
            if (chosenMoveIndex == -42) {
                game.undoMove();
                continue;
            }
            
            game.performMove(legalMoves.get(chosenMoveIndex - 1));      
        }
        
        System.out.println();
        System.out.println("FINAL state of the game:");
        System.out.println(game.getCurrState().toString().replaceAll("(?m)^", "  "));
        System.out.format("GAME OVER! %s won.\nBye!\n", game.getPrevPlayer());
    }
    
}
