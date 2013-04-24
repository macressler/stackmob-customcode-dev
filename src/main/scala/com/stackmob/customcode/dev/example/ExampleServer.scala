package com.stackmob.customcode.dev
package example

import server.CustomCodeServer

object ExampleServer {
  def main(args: Array[String]) {
    val entryObject = new EntryPointExtender
    CustomCodeServer.serve(entryObject, "example-api-key", "example-api-secret")
  }
}
