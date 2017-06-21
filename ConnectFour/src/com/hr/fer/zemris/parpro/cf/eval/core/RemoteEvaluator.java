package com.hr.fer.zemris.parpro.cf.eval.core;

import com.hr.fer.zemris.parpro.cf.eval.exceptions.DuringEvaluationException;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.EvaluatorCloseException;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.EvaluatorCreationException;
import com.hr.fer.zemris.parpro.cf.Game;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marko
 */
public class RemoteEvaluator<S, M, P> implements Evaluator<S, M, P> {

    private int maxDepth;
    private final Game<S, M, P> game;

    private final String hostName;
    private final int port;

    private final Socket clientSocket;
    private final ObjectOutputStream outToServer;
    private final ObjectInputStream inFromServer;

    public RemoteEvaluator(Game<S, M, P> game, int maxDepth, String hostName, int port)
            throws EvaluatorCreationException {

        this.game = game;
        this.maxDepth = maxDepth;

        this.hostName = hostName;
        this.port = port;
        
        try {
            clientSocket = new Socket(hostName, port);
            outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            inFromServer = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            throw new EvaluatorCreationException("Something happened to socket"
                    + " or stream during evaluator creation.");
        }
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    @Override
    public double evaluate(M move) throws DuringEvaluationException {
        // we are delegating the job to the remote server that runs some
        // particular version of Evaluator -- BruteForceEvaluator

        // since the evaluator in question is remote, it cannot listen to changes
        // in this class directly -- each time we have to send information about
        // max search depth, as well as the game itself
        // move that is being evaluated is also sent, naturally
        try {
            // ZEROTH, send that that there is some data to be received
            outToServer.writeBoolean(true);
            
            // FIRST, send the game object
            outToServer.writeObject(game);

            // SECOND, send the maxDepth
            outToServer.writeInt(maxDepth);

            // THIRD (and finally), send the move that is being evaluated
            outToServer.writeObject(move);
        } catch (IOException ex) {
            Logger.getLogger(RemoteEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            throw new DuringEvaluationException("Exc. during sending of data to remote evaluator.");
        }

        double eval;
        try {
            // now wait for the response
            eval = inFromServer.readDouble();
        } catch (IOException ex) {
            Logger.getLogger(RemoteEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            throw new DuringEvaluationException("Exc. during receiving of result from remote evaluator.");
        }
        return eval;
    }

    @Override
    public void close() throws EvaluatorCloseException {
        try {
            // notify server that there's no more data to communicate
            outToServer.writeBoolean(false);
            outToServer.flush();
            inFromServer.close();
            outToServer.close();
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(RemoteEvaluator.class.getName()).log(Level.SEVERE, null, ex);
            throw new EvaluatorCloseException("Something happened during closing of streams and socket.");
        }
    }

    @Override
    public double evaluate(M move, P player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
