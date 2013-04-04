package com.stackmob.customcode.localrunner.sdk.data

case class NoSMValueFoundException[T](t: T) extends Exception("no SMValue found for %s".format(t.getClass.toString))
