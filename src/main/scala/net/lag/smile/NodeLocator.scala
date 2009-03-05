/*
 * Copyright (c) 2008, Robey Pointer <robeypointer@gmail.com>
 * ISC licensed. Please see the included LICENSE file for more information.
 */

package net.lag.smile

import scala.collection.mutable


/**
 * Used by MemcacheClient to map a memcache key to the server node that should contain it.
 */
trait NodeLocator {
  /**
   * Set the server pool. This will be called when MemcacheClient is initialized, and also
   * whenever the server list is changed.
   */
  def setPool(pool: ServerPool): Unit

  /**
   * Return the server node that should contain this key.
   */
  def findNode(key: Array[Byte]): MemcacheConnection
}


/**
 * Registry of NodeLocator classes by name, for use in defining a node locator in a config
 * file.
 */
object NodeLocator {
  type Factory = (KeyHasher) => NodeLocator

  private val locators = new mutable.HashMap[String, (String, Factory)]

  register("default", "crc32-itu") { h => new RoundRobinNodeLocator(h) }
  register("round-robin", "crc32-itu") { h => new RoundRobinNodeLocator(h) }
  register("ketama", "ketama") { h => new KetamaNodeLocator(h) }
  register("sequential", "") { h => new SequentialNodeLocator(h) }


  /**
   * Register a node locator by name. If used before creating a memcache client from a
   * config file, this will let you create custom node locators and specify them by name
   * in the config file. (It is not necessary to register a node locator unless you want it
   * to be identified in config files.)
   *
   * @param name name of the node locator
   * @param defaultHash name of the default hash function for this node locator (looked up
   *   in KeyHasher)
   * @param factory a function taking a KeyHasher and returning a new NodeLocator
   */
  def register(name: String, defaultHash: String)(factory: Factory) = {
    locators += (name -> (defaultHash, factory))
  }

  /**
   * Return one of the node locator factory functions (and its default hash algorithm) by
   * name. This is used to configure a memcache client from a config file.
   */
  def byName(name: String): (String, Factory) = {
    locators.get(name) match {
      case Some((h, f)) => (h, f)
      case None => throw new IllegalArgumentException("unknown node locator: " + name)
    }
  }
}
