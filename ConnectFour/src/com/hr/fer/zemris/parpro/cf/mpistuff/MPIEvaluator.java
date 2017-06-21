
package com.hr.fer.zemris.parpro.cf.mpistuff;

import mpi.*;

/**
 *
 * @author marko
 */
public class MPIEvaluator {
    public static void main(String[] args) {       
        MPI.Init(args);

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        MPI.COMM_WORLD.Barrier();

        MPIAgent agent = MPIAgent.createMPIAgent(rank, size, args);
        agent.run();

        MPI.Finalize();
    }
}
