/*
 * Copyright (c) 2008, Robey Pointer <robeypointer@gmail.com>
 * ISC licensed. Please see the included LICENSE file for more information.
 */

package net.lag.smile

import net.lag.logging.Logger
import org.specs.runner.SpecsFileRunner


object TestRunner extends SpecsFileRunner("src/test/scala/**/*.scala", ".*") {
  if (System.getProperty("debugtrace") == null) {
    Logger.get("").setLevel(Logger.FATAL)
  } else {
    Logger.get("").setLevel(Logger.TRACE)
  }
}
