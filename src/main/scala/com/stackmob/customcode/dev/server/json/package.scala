package com.stackmob.customcode.dev.server

import net.liftweb.json.{NoTypeHints, Serialization}

/**
 * Created by IntelliJ IDEA.
 *
 * com.stackmob.customcode.server.json
 *
 * User: aaron
 * Date: 3/28/13
 * Time: 4:40 PM
 */
package object json {
  private implicit val formats = Serialization.formats(NoTypeHints)

  def read[T: Manifest](s: String) = Serialization.read[T](s)
  def write[T <: AnyRef](t: T) = Serialization.write[T](t)

}
