package com.hr.fer.zemris.parpro.cf.evalservers;

import com.hr.fer.zemris.parpro.cf.concretegames.Board;
import com.hr.fer.zemris.parpro.cf.Game;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.DuringEvaluationException;
import com.hr.fer.zemris.parpro.cf.eval.core.Evaluator;
import com.hr.fer.zemris.parpro.cf.eval.core.BruteForceEvaluator;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marko
 */
public class RBFEServer {

    public static void main(String[] args) throws DuringEvaluationException {

        int portNumber;
        if (args.length != 1) {
            portNumber = 4444;
        } else {
            portNumber = Integer.parseInt(args[0]);
        }

        RBFEServer server = new RBFEServer();
        server.run(portNumber);

    }
    
    public void run(int portNumber) throws DuringEvaluationException {
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                try (
                    Socket clientSocket = serverSocket.accept();
                    ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
                ) {
                    System.out.println("Starting communication with " + clientSocket.toString());
                    // first boolean from communication tells us weather there is more data to be sent
                    while(inFromClient.readBoolean()) {
                        System.out.println("receiving data...");
                        
                        Game<Board, Integer, String> game =
                                (Game<Board, Integer, String>) inFromClient.readObject();
                        
                        int maxDepth = inFromClient.readInt();
                        
                        Integer move = (Integer) inFromClient.readObject();
                        
                        Evaluator<Board, Integer, String> evaluator =
                                new BruteForceEvaluator<>(game, maxDepth);
                        
                        System.out.println("evaluating move...");
                        double evaluation;
                        try {
                            evaluation = evaluator.evaluate(move);
                        } catch (DuringEvaluationException ex) {
                            Logger.getLogger(RBFEServer.class.getName()).log(Level.SEVERE, null, ex);
                            throw new DuringEvaluationException("Local evaluator on server defects.");
                        }
                        
                        System.out.println("sending evaluation back to client...");
                        outToClient.writeDouble(evaluation);
                        outToClient.flush();
                        System.out.println("sent!");
                        System.out.println();
                    }
                    System.out.println("Terminating communication with " + clientSocket.toString());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(RBFEServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(RBFEServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
