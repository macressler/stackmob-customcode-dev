package com.stackmob.customcode.dev.server.sdk.data

case class NoSMValueFoundException[T](t: T) extends Exception("no SMValue found for %s".format(t.getClass.toString))
