package co.laconic.common.io

import scala.io.Source.fromInputStream

case class Resource(name: String) {
  def mkString: String = fromInputStream(getClass.getClassLoader.getResourceAsStream(name)).mkString
}
