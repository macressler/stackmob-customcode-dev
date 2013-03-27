package com.stackmob.customcode.localrunner

import org.eclipse.jetty.server.Server
import java.net.InetSocketAddress
import com.stackmob.core.jar.JarEntryObject

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 2:45 PM
 */
class CustomCodeServer(jarEntry: JarEntryObject) {

  def serve() {
    val addr = new InetSocketAddress("localhost", 8080)
    val svr = new Server(addr)
    svr.setHandler(new CustomCodeHandler(jarEntry))
    svr.start()
    svr.join()
  }

}
