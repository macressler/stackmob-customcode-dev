package com.stackmob.customcode.dev.localrunner.sdk.logger

import com.stackmob.sdkapi.LoggerService
import org.slf4j.LoggerFactory

/**
 * Created by IntelliJ IDEA.
 * 
 * com.stackmob.customcode.localrunner.sdk.logger
 * 
 * User: aaron
 * Date: 4/2/13
 * Time: 3:34 PM
 */
class LoggerServiceImpl(name: String) extends LoggerService {
  private lazy val logger = LoggerFactory.getLogger(name)
  def trace(s: String) {
    logger.trace(s)
  }

  def trace(s: String, t: Throwable) {
    logger.trace(s, t)
  }

  def debug(s: String) {
    logger.debug(s)
  }

  def debug(s: String, t: Throwable) {
    logger.debug(s, t)
  }

  def info(s: String) {
    logger.info(s)
  }

  def info(s: String, t: Throwable) {
    logger.info(s, t)
  }

  def warn(s: String) {
    logger.warn(s)
  }

  def warn(s: String, t: Throwable) {
    logger.warn(s, t)
  }

  def error(s: String) {
    logger.error(s)
  }

  def error(s: String, t: Throwable) {
    logger.error(s, t)
  }
}
