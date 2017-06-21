package com.hr.fer.zemris.parpro.cf.eval.core;

import com.hr.fer.zemris.parpro.cf.eval.exceptions.DuringEvaluationException;
import com.hr.fer.zemris.parpro.cf.eval.exceptions.EvaluatorCloseException;

/**
 *
 * @author marko
 */
public interface Evaluator<S, M, P> extends AutoCloseable {
    double evaluate(M move) throws DuringEvaluationException;
    double evaluate(M move, P player);  
    @Override
    void close() throws EvaluatorCloseException;
}
