package com.stackmob.customcode.dev

sealed abstract class ConfigKey(key: String)
case object EnableDatastoreService extends ConfigKey("EnableDatastoreService")

sealed trait ConfigVal {
  def fold[U](enabled: Boolean => U): U = {
    this match {
      case Enabled(bool) => enabled(bool)
    }
  }
}
case class Enabled(value: Boolean) extends ConfigVal
