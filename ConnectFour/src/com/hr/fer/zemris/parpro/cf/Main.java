package com.hr.fer.zemris.parpro.cf;

import com.hr.fer.zemris.parpro.cf.concretegames.Board;
import com.hr.fer.zemris.parpro.cf.concretegames.ConnectFourBuilder;
import com.hr.fer.zemris.parpro.cf.eval.core.BruteForceEvaluator;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.EvaluatorCreationException;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.DuringEvaluationException;
import com.hr.fer.zemris.parpro.cf.eval.core.Evaluator;
import com.hr.fer.zemris.parpro.cf.eval.core.RemoteEvaluator;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.EvaluatorCloseException;

/**
 *
 * @author marko
 */
public class Main {

    public static void main(String[] args) {
        // testGame();
        testMPI(args);

    }
    
    public static void testGame() {
        ConnectFourBuilder cfb = new ConnectFourBuilder();
        Game<Board, Integer, String> cf = cfb.createConnectFour();
        Evaluator<Board, Integer, String> evaluator = 
                new BruteForceEvaluator<>(cf, 7);
        GameProtocol<Board, Integer, String> prot = 
                new CpuVsHumanProtocol<>(cf, evaluator, "Bob");
        prot.startGame();
        
    }

    public static void testMPI(String[] args) {
        // default server values
        String hostName = "local";
        int port = 4444;

        if (args.length == 2) {
            hostName = args[0];
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException nfe) {
                System.err.println("Provided port number is not an integer!");
                System.exit(1);
            }
        }

        ConnectFourBuilder cfb = new ConnectFourBuilder();
        Game<Board, Integer, String> cf = cfb.createConnectFour();

        /*
        cf.performMove(1);
        cf.performMove(4);
        cf.performMove(2);
        cf.performMove(5);
        cf.performMove(3);
        cf.performMove(6);
        cf.performMove(3);
        cf.performMove(2);
        cf.performMove(4);
        cf.performMove(5);
        cf.performMove(5);
        cf.performMove(4);
        cf.performMove(6);
        cf.performMove(0);
         */
        int maxDepth = 10;

        try (
                Evaluator<Board, Integer, String> evaluator
                = new RemoteEvaluator<>(cf, maxDepth, hostName, port);) {
            for (Integer move : cf.getLegalMoves()) {
                double eval = evaluator.evaluate(move);
                System.out.format("Stupac %d, vrijednost: %f\n", move, eval);
            }
        } catch (EvaluatorCreationException | EvaluatorCloseException | DuringEvaluationException ex) {
            System.err.println(ex.getMessage());
            System.exit(1);
        }
    }
}
