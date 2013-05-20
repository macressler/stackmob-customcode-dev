/**
 * Copyright 2011-2013 StackMob
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.stackmob.customcode.dev.server.sdk.logger

import com.stackmob.sdkapi.LoggerService
import org.slf4j.LoggerFactory

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
