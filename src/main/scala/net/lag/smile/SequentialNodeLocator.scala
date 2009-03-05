/*
 * Copyright (c) 2009, Robey Pointer <robeypointer@gmail.com>
 * ISC licensed. Please see the included LICENSE file for more information.
 */

package net.lag.smile

import java.util.Random
import net.lag.extensions._
import scala.collection.mutable


/**
 * Choose the next node, a true round-robin. Useful for communicating with a cluster of
 * Kestrel queues.
 */
class SequentialNodeLocator(hasher: KeyHasher) extends NodeLocator {
  def this() = this(null)

  var pool: ServerPool = null
  var continuum: Array[MemcacheConnection] = null
  var count = 0
  var current = 0

  def setPool(pool: ServerPool) = {
    this.pool = pool
    val fanout = new mutable.ArrayBuffer[MemcacheConnection]
    for (s <- pool.servers) {
      for (i <- 1 to s.weight) {
        fanout += s
      }
    }

    val rand = new Random(System.currentTimeMillis)
    val randomized = new mutable.ListBuffer[MemcacheConnection]
    while (fanout.size > 0) {
      val idx = rand.nextInt(fanout.size)
      randomized += fanout(idx)
      fanout.remove(idx)
    }

    continuum = randomized.toArray
    count = continuum.size
  }

  /**
   * Return the server node that should contain this key.
   */
  def findNode(key: Array[Byte]): MemcacheConnection = {
    this.synchronized {
      current += 1
      if (current >= count) current = 0
      println("findNode " + continuum(current).port)
      continuum(current)
    }
  }

  override def toString() = {
    "<SequentialNodeLocator hash=null>"
  }
}
