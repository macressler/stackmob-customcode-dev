package com.stackmob.customcode.dev.localrunner.sdk.data

case class NoSMValueFoundException[T](t: T) extends Exception("no SMValue found for %s".format(t.getClass.toString))
