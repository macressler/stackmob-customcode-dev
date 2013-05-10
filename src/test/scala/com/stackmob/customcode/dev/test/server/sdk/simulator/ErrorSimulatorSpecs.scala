package com.stackmob.customcode.dev
package test
package server
package sdk
package simulator

import org.specs2.{ScalaCheck, Specification}
import org.specs2.mock.Mockito
import java.util.concurrent.atomic.AtomicInteger
import com.stackmob.customcode.dev.server.sdk.simulator.{Frequency, ErrorSimulator, ThrowableFrequency}
import com.twitter.util.Duration
import java.util.concurrent.TimeUnit

class ErrorSimulatorSpecs extends Specification with CustomMatchers with Mockito with ScalaCheck { def is =
  "ErrorSimulatorSpecs".title                                                                                           ^ end ^
  "ErrorSimulator is responsible for simulating 0 or more errors, represented as ThrowableFrequency"                    ^ end ^
  "if no simulation, and there are multiple errors, the operation is executed exactly once"                             ! exactlyOnce ^ end ^
  end

  private def simulator(freqs: ThrowableFrequency*): ErrorSimulator = {
    new ErrorSimulator(freqs.toSeq)
  }

  private def exactlyOnce = {
    val atomicInt = new AtomicInteger(0)
    val sim = simulator(
      ThrowableFrequency(new Exception("hello world"), Frequency(0, Duration(1, TimeUnit.SECONDS))),
      ThrowableFrequency(new Exception("hello world"), Frequency(0, Duration(1, TimeUnit.MILLISECONDS)))
    )
    val ret = sim.apply {
      atomicInt.incrementAndGet
    }
    val numExec = atomicInt.get() must beEqualTo(1)
    val res = ret must beEqualTo(1)
    numExec and res
  }

}
