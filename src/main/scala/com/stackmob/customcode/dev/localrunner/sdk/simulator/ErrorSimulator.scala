package com.stackmob.customcode.dev.localrunner.sdk.simulator

/**
 * simulates 0 or more errors with a specific frequency for each error.
 * Example usage:
 *
 * {{{
 * //throw ex1 once every 2 minutes
 * val ex1 = new Exception("ex1")
 * val ex1Freq = ThrowableFrequency(ex1, Frequency(1, Duration(2, TimeUnit.MINUTES)))
 * //throw ex2 twice every 5 seconds
 * val ex2 = new Exception("ex2")
 * val ex2Freq = ThrowableFrequency(ex2, Frequency(2, Duration(5, TimeUnit.SECONDS)))
 *
 * //create the simulator
 * val simulator = ErrorSimulator(ex1Freq, ex2Freq)
 *
 * //run a simulation
 * simulator.simulate {
 *   println("hello world")
 * }}}
 * @param errs the sequence of ThrowableFrequencies to run through on each simulate call
 */
class ErrorSimulator(val errs: Seq[ThrowableFrequency]) {

  /**
   * execute a simulation across each of errs
   * @param op the operation to execute if none of errs threw.
   *           will be executed 0 times if any single err threw, otherwise once
   * @tparam T the return type of op
   * @return the result of executing op
   */
  def apply[T](op: => T): T = {
    errs.foreach { err =>
      err.simulate(op)
    }
    op
  }

  /**
   * combine this ErrorSimulator with an additional sequence of ThrowableFrequency objects
   * @param newErrs the ThrowableFrequency objects to add to the resulting ErrorSimulator
   * @return the new ErrorSimulator
   */
  def and(newErrs: Seq[ThrowableFrequency]): ErrorSimulator = {
    ErrorSimulator(errs ++ newErrs)
  }

  /**
   * combine this and the given ErrorSimulator to create a new one
   * @param newErrSim the new ErrorSimulator to combine with this one
   * @return the ErrorSimulator result of combining this and the given
   */
  def andThen(newErrSim: ErrorSimulator): ErrorSimulator = {
    ErrorSimulator(errs ++ newErrSim.errs)
  }
}

object ErrorSimulator {
  /**
   * create a new ErrorSimulator
   * @param errs the ThrowableFrequencies to
   * @return
   */
  def apply(errs: Seq[ThrowableFrequency]): ErrorSimulator = {
    new ErrorSimulator(errs)
  }
}
