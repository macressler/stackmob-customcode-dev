package com.stackmob.customcode.localrunner

import org.eclipse.jetty.server.Server
import java.net.InetSocketAddress
import com.stackmob.core.jar.JarEntryObject
import org.slf4j.LoggerFactory

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
  private lazy val logger = LoggerFactory.getLogger(classOf[CustomCodeServer])
  def serve() {
    val host = "localhost"
    val port = 8080
    val addr = new InetSocketAddress(host, port)
    val svr = new Server(addr)
    svr.setHandler(new CustomCodeHandler(jarEntry))
    logger.info("Starting custom code local runner session %s on %s:%d".format(uuid.toString, host, port))
    svr.start()
    svr.join()
  }

}
