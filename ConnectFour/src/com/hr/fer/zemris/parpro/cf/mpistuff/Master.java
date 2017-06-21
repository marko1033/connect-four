package com.hr.fer.zemris.parpro.cf.mpistuff;

import com.hr.fer.zemris.parpro.cf.concretegames.Board;
import com.hr.fer.zemris.parpro.cf.Game;
import com.hr.fer.zemris.parpro.cf.evalservers.RBFEServer;
import static com.hr.fer.zemris.parpro.cf.mpistuff.Const.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import mpi.MPI;
import mpi.Status;

/**
 *
 * @author marko
 */
public class Master extends MPIAgent {

    private final int rank;
    private final int size;
    
    private final int port = 4444;
    
    private final int masterDepth = 4;

    private Game<Board, Integer, String> game;
    private int maxDepth;
    private Integer move;
    private String clientPlayer;


    public Master(int rank, int size, String[] args) {
        this.rank = rank;
        this.size = size;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("server created: " + serverSocket.getLocalPort()); 
            while (true) {                
                try (
                    Socket clientSocket = serverSocket.accept();
                    ObjectOutputStream outToClient = new ObjectOutputStream(clientSocket.getOutputStream());
                    ObjectInputStream inFromClient = new ObjectInputStream(clientSocket.getInputStream());
                ) {
                    System.out.println("***************************************");
                    System.out.println("Starting communication with " + clientSocket.toString());
                    System.out.println("***************************************");
                    System.out.println();
                    // first boolean from communication tells us weather there is more data to be sent
                    while(inFromClient.readBoolean()) {
                        System.out.println("receiving data...");
                        
                        game = (Game<Board, Integer, String>) inFromClient.readObject();
                        
                        maxDepth = inFromClient.readInt();
                        
                        move = (Integer) inFromClient.readObject();
                        
                        System.out.println("evaluating move...");
                        
                        long startTime = System.currentTimeMillis();
                        double evaluation = performEvalSession();
                        long stopTime = System.currentTimeMillis();
                        
                        long elapsedTime = stopTime - startTime;
                        System.out.println("eval. time = " + elapsedTime);
                        
                        System.out.println("sending evaluation back to client...");
                        outToClient.writeDouble(evaluation);
                        outToClient.flush();
                        System.out.println("sent!");
                        System.out.println();
                        System.out.println();
                    }
                    System.out.println("########################################");
                    System.out.println("Terminating communication with " + clientSocket.toString());
                    System.out.println("########################################");
                    System.out.println();
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(RBFEServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException ex) {
            Logger.getLogger(RBFEServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        performEvalSession();
             
    }
    
    private double performEvalSession() {
        int workerDepth = maxDepth - masterDepth;

        clientPlayer = game.getCurrPlayer();

        // send game to each worker
        Game<Board, Integer, String>[] cfBuff = new Game[1];
        cfBuff[0] = game;
        MPI.COMM_WORLD.Bcast(cfBuff, 0, 1, MPI.OBJECT, MASTER_RANK);

        // send depth to each worker
        int[] workerDepthBuff = new int[1];
        workerDepthBuff[0] = workerDepth;
        MPI.COMM_WORLD.Bcast(workerDepthBuff, 0, 1, MPI.INT, MASTER_RANK);

        Queue<Task> taskPool = new LinkedList<>();
        populateTasks(move, masterDepth, taskPool);

        int taskCnt = taskPool.size();

        Map<Integer, Task> workerToTaskMap = new HashMap<>();
        Map<Task, Double> taskToEvalMap = new HashMap<>();
        Queue<Integer> requestingWorkers = new LinkedList<>();
        
        while (!(requestingWorkers.size() == size - 1 && taskPool.isEmpty())) {
            Status status = MPI.COMM_WORLD.Iprobe(MPI.ANY_SOURCE, MPI.ANY_TAG);
            if (status != null) {
                switch (status.tag) {
                    case TASK_REQUEST:
                        MPI.COMM_WORLD.Recv(DUMMY_BUFF, 0, 0, MPI.INT, status.source, TASK_REQUEST);
                        requestingWorkers.add(status.source);
                        break;
                    case TASK_COMPLETED:
                        double[] evalBuff = new double[1];
                        MPI.COMM_WORLD.Recv(evalBuff, 0, 1, MPI.DOUBLE, status.source, TASK_COMPLETED);
                        double eval = evalBuff[0];
                        Task completedTask = workerToTaskMap.remove(status.source);
                        if (taskToEvalMap.containsKey(completedTask)) {
                            System.out.println("IMPOSSIBLE!");
                            System.out.println("Task completed!");
                            System.out.println("task:");
                            System.out.println(completedTask);
                            System.out.println("eval: " + eval);
                        }
                        taskToEvalMap.put(completedTask, eval);
                        break;
                    default:
                        System.err.println("Unkonwn message received!");
                        System.exit(1);
                }
            }
            while (!(requestingWorkers.isEmpty() || taskPool.isEmpty())) {
                Task task = taskPool.remove();
                Integer workerRank = requestingWorkers.remove();
                workerToTaskMap.put(workerRank, task);
                Task[] taskBuff = new Task[1];
                taskBuff[0] = task;
                MPI.COMM_WORLD.Send(taskBuff, 0, 1, MPI.OBJECT, workerRank, TASK_GRANT);
            }

        }
        // terminate the workers -- they are done with their job
        for (int workerRank = 1; workerRank <= size - 1; workerRank++) {
            MPI.COMM_WORLD.Send(DUMMY_BUFF, 0, 0, MPI.INT, workerRank, SESSION_FINISHED);
        }

        if (taskToEvalMap.size() != taskCnt) {
            System.out.println("task count: " + taskCnt);
            System.out.println("results count: " + taskToEvalMap.size());
        }
        
        double eval = evaluateMove(move, masterDepth, taskToEvalMap);
        return eval;
    }
    
    private void populateTasks(Integer move, int depth, Queue<Task> taskPool) {
        Stack<Integer> movePath = new Stack<>();
        populateTasksRecur(move, depth, movePath, taskPool);
    }

    private void populateTasksRecur(Integer move, int depth, Stack<Integer> movePath, Queue<Task> taskPool) {
        if (depth == 0) {
            Integer[] currMovePath = new Integer[movePath.size()];
            currMovePath = movePath.toArray(currMovePath);
            Task correspTask = new Task(currMovePath, move);
            if (taskPool.contains(correspTask)) {
                System.out.println("duplicate in task pool");
            }
            taskPool.add(correspTask);
            return;
        }
        game.performMove(move);
        if (game.isOver()) {
            game.undoMove();
            return;
        }
        movePath.push(move);
        for (Integer newMove : game.getLegalMoves()) {
            populateTasksRecur(newMove, depth - 1, movePath, taskPool);
        }
        game.undoMove();
        movePath.pop();
    }
    
    private double evaluateMove(Integer move, int depth, Map<Task, Double> taskToEvalMap) {
        Stack<Integer> movePath = new Stack<>();
        return evaluateRecursively(move, depth, movePath, taskToEvalMap);
    }

    private double evaluateRecursively(Integer move, int depth,
            Stack<Integer> movePath, Map<Task, Double> taskToEvalMap) {
        if (depth == 0) {
            Integer[] currMovePath = new Integer[movePath.size()];
            currMovePath = movePath.toArray(currMovePath);
            Task correspTask = new Task(currMovePath, move);
            if (!taskToEvalMap.containsKey(correspTask)) {
                System.out.println("How on Earth is that possible??!!");
                System.out.println(correspTask);
            }
            return taskToEvalMap.get(correspTask);
        }

        game.performMove(move);
        if (game.isOver()) {
            game.undoMove();
            if (game.getCurrPlayer().equals(clientPlayer)) {
                return 1;
            } else {
                return -1;
            }
        }
        movePath.push(move);
        double totalEval = 0;
        boolean allLose = true;
        boolean allWin = true;
        List<Integer> legalMoves = game.getLegalMoves();
        for (Integer newMove : legalMoves) {
            double eval = evaluateRecursively(newMove, depth - 1, movePath, taskToEvalMap);
            if (eval > -1) {
                allLose = false;
            }
            if (eval != 1) {
                allWin = false;
            }
            if (eval == 1 && game.getCurrPlayer().equals(clientPlayer)) {
                game.undoMove();
                movePath.pop();
                return 1;
            }
            if (eval == -1 && !game.getCurrPlayer().equals(clientPlayer)) {
                game.undoMove();
                movePath.pop();
                return -1;
            }
            totalEval += eval;
        }
        game.undoMove();
        movePath.pop();
        if (allWin) {
            return 1;
        }
        if (allLose) {
            return -1;
        }
        return totalEval / (double) legalMoves.size();
    }

}
