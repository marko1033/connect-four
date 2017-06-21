package com.hr.fer.zemris.parpro.cf;

import com.hr.fer.zemris.parpro.cf.eval.core.Evaluator;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.DuringEvaluationException;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author marko
 */
public class CpuVsHumanProtocol<S, M, P> implements GameProtocol<S, M, P> {

    private final Game<S, M, P> game;
    private final Evaluator<S, M, P> evaluator;
    private final P human;

    public CpuVsHumanProtocol(Game<S, M, P> game, Evaluator<S, M, P> evaluator,
            P human) {
        this.game = game;
        this.evaluator = evaluator;

        if (!human.equals(game.getFirstPlayer())
                && !human.equals(game.getSecondPlayer())) {
            throw new IllegalArgumentException("Player received as human doesn't exist.");
        }

        this.human = human;
    }

    @Override
    public void startGame() {
        game.resetGame();
        System.out.println(game.getWelcomeMsg());

        Scanner sc = new Scanner(System.in);

        while (!game.isOver()) {
            System.out.println();
            System.out.println("current state of the game:");
            System.out.println(game.getCurrState().toString().replaceAll("(?m)^", "  "));

            List<M> legalMoves = game.getLegalMoves();
            System.out.println("legal moves:");
            for (int i = 0; i < legalMoves.size(); i++) {
                System.out.format("  %d: move[%s]\n", i + 1, legalMoves.get(i));
            }

            if (game.getCurrPlayer().equals(human)) {
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
                assert (chosenMoveIndex != -1);

                if (chosenMoveIndex == -42) {
                    game.undoMove();
                    continue;
                }

                game.performMove(legalMoves.get(chosenMoveIndex - 1));
            } else {
                M bestMove = legalMoves.get(0);
                double bestEval = -1;
                for (M legalMove : legalMoves) {
                    double eval = 0;
                    try {
                        eval = evaluator.evaluate(legalMove);
                    } catch (DuringEvaluationException ex) {
                        System.err.println(ex.getMessage());
                        System.exit(1);
                    }
                    if (eval > bestEval) {
                        bestEval = eval;
                        bestMove = legalMove;
                    } 
                }
                System.out.format("CPU (%s) plays : %s\n", game.getCurrPlayer(), bestMove);
                game.performMove(bestMove);
            }
        }
        
        System.out.println();
        System.out.println("FINAL state of the game:");
        System.out.println(game.getCurrState().toString().replaceAll("(?m)^", "  "));
        System.out.format("GAME OVER! %s won.\nBye!\n", game.getPrevPlayer());
    }
}
