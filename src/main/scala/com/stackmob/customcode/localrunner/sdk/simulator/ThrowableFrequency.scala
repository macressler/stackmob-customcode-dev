package com.stackmob.customcode.localrunner.sdk.simulator

import com.twitter.util.Time
import scala.util.Random
import akka.actor.{Props, Actor, ActorSystem}
import akka.pattern.ask
import scala.concurrent.{ExecutionContext, Await, Promise}
import scala.concurrent.duration.Duration
import java.util.concurrent.{Executors, ExecutorService}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.localrunner.sdk.simulator
 *
 * User: aaron
 * Date: 4/17/13
 * Time: 2:42 PM
 */

/**
 * a single throwable and a frequency with which it should be thrown
 * @param err the error to throw
 * @param freq the frequency with which to throw it
 * @param rand the random number generator to be used to decide when to throw
 */
class ThrowableFrequency(val err: Throwable,
                         val freq: Frequency,
                         val rand: Random = DefaultRandom) {

  private case class Execute[T](op: => T)
  private class Executor extends Actor {
    private var count = 0
    private var lastRollover = Time.now
    def receive = {
      case Execute(op) => {
        if(lastRollover + freq.every <= Time.now) {
          //if a rollover happened, reset stuff
          lastRollover = Time.now
          count = 0
          sender ! op
        } else if(count >= freq.number) {
          //if the counter is at max, run the op normally
          count += 1
          sender ! op
        } else {
          //if the counter is not at max, randomly decide if there's an error
          val shouldErr = rand.nextBoolean()
          if(shouldErr) {
            count += 1
            sender ! err
          } else {
            sender ! op
          }
        }
      }
    }
  }
  private val system = ActorSystem("ThrowableFrequency")
  private val executorActor = system.actorOf(Props[Executor])

  /**
   * simulate a call to op, randomly selecting when to throw based on freq
   * @param op the operation to execute. only executed if this method doesn't throw
   * @tparam T the return type of op
   * @return the result of executing op (assuming this method didn't throw)
   */
  def simulate[T](op: => T): T = {
    val ex = Execute(op)
    Await.result(executorActor ? ex, Duration.Inf) match {
      case t: T => t
      case t: Throwable => throw t
    }
  }
}

object ThrowableFrequency {
  def apply(err: Throwable, freq: Frequency, rand: Random = DefaultRandom) = {
    new ThrowableFrequency(err, freq, rand)
  }
}
