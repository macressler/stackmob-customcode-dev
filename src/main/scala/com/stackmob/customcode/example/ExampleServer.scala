package com.stackmob.customcode.example

import com.stackmob.customcode.localrunner.CustomCodeServer

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.example
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 3:04 PM
 */

object ExampleServer {
  def main(args: Array[String]) {
    val entryObject = new EntryPointExtender
    val svr = new CustomCodeServer(entryObject)
    svr.serve()
  }
}
