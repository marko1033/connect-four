package com.hr.fer.zemris.parpro.cf.mpistuff;

import com.hr.fer.zemris.parpro.cf.concretegames.Board;
import com.hr.fer.zemris.parpro.cf.eval.core.BruteForceEvaluator;
import com.hr.fer.zemris.parpro.cf.eval.core.Evaluator;
import com.hr.fer.zemris.parpro.cf.Game;
import static com.hr.fer.zemris.parpro.cf.mpistuff.Const.*;
import mpi.MPI;
import mpi.Status;

/**
 *
 * @author marko
 */
public class Worker extends MPIAgent {

    private final int rank;
    private final int size;

    public Worker(int rank, int size) {
        this.rank = rank;
        this.size = size;
    }

    @Override
    public void run() {
        while (true) {
            Game<Board, Integer, String>[] cfBuff = new Game[1];
            MPI.COMM_WORLD.Bcast(cfBuff, 0, 1, MPI.OBJECT, MASTER_RANK);

            Game<Board, Integer, String> game = cfBuff[0];

            int[] workerDepthBuff = new int[1];
            MPI.COMM_WORLD.Bcast(workerDepthBuff, 0, 1, MPI.INT, MASTER_RANK);
            int workerDepth = workerDepthBuff[0];

            String clientPlayer = game.getCurrPlayer();

            while (true) {
                MPI.COMM_WORLD.Send(DUMMY_BUFF, 0, 0, MPI.INT, MASTER_RANK, TASK_REQUEST);
                Status status = null;
                while (status == null) {
                    status = MPI.COMM_WORLD.Iprobe(MASTER_RANK, MPI.ANY_TAG);
                }
                Task task = null;
                switch (status.tag) {
                    case TASK_GRANT:
                        Task[] taskBuff = new Task[1];
                        MPI.COMM_WORLD.Recv(taskBuff, 0, 1, MPI.OBJECT, MASTER_RANK, TASK_GRANT);
                        task = taskBuff[0];
                        // System.out.println("Worker_" + rank + ": received new task!");
                        break;
                    case SESSION_FINISHED:
                        MPI.COMM_WORLD.Recv(DUMMY_BUFF, 0, 0, MPI.INT, MASTER_RANK, SESSION_FINISHED);
                        // System.out.println("Worker_" + rank + ": received instructions to terminate!");
                        break;
                    default:
                        System.err.println("Unkonwn message received!");
                        System.exit(1);
                }
                
                // tell us that session has finished
                if (task == null) {
                    break;
                }

                // get the game to the appropriate state at which targetMove is analyzed
                for (Integer move : task.getMovePath()) {
                    game.performMove(move);
                }

                Evaluator<Board, Integer, String> evaluator = new BruteForceEvaluator<>(game, workerDepth);

                // evaluate the task
                double eval = evaluator.evaluate(task.getTargetMove(), clientPlayer);

                // get the game to the original state -- undo the move path
                for (Integer move : task.getMovePath()) {
                    game.undoMove();
                }

                // send the resulting evaluation to the master
                double[] evalBuff = new double[1];
                evalBuff[0] = eval;
                MPI.COMM_WORLD.Send(evalBuff, 0, 1, MPI.DOUBLE, MASTER_RANK, TASK_COMPLETED);

            }
        }
    }
}
