package com.stackmob.customcode.dev.localrunner

import org.eclipse.jetty.server.Server
import java.net.InetSocketAddress
import com.stackmob.core.jar.JarEntryObject
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner
 * 
 * User: aaron
 * Date: 3/27/13
 * Time: 2:45 PM
 */
object CustomCodeServer {
  private lazy val logger = LoggerFactory.getLogger(CustomCodeServer.getClass)
  def serve(jarEntryObject: JarEntryObject, apiKey: String, apiSecret: String, port: Int = 8080) {
    implicit val session = UUID.randomUUID()
    val handler = new CustomCodeHandler(apiKey, apiSecret, jarEntryObject)

    val host = "localhost"
    val addr = new InetSocketAddress(host, port)
    val svr = new Server(addr)
    svr.setHandler(handler)
    logger.info(s"Starting custom code local runner session ${session.toString} on $host:$port")
    svr.start()
    svr.join()
  }

}
