package com.hr.fer.zemris.parpro.cf.mpistuff;


/**
 *
 * @author marko
 */
public abstract class MPIAgent {
    
    public static MPIAgent createMPIAgent(int rank, int size, String[] args) {
        if (rank == 0) {
            return new Master(rank, size, args);
        } else {
            return new Worker(rank, size);
        }
    }
    
    public abstract void run();
}
