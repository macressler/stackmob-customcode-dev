package com.stackmob.customcode.dev.server.sdk.data

class NoSMValueFoundException[T](t: T) extends Exception(s"no SMValue found for ${t.getClass.toString}")
object NoSMValueFoundException {
  def apply[T](t: T): NoSMValueFoundException[T] = {
    new NoSMValueFoundException[T](t)
  }
}
