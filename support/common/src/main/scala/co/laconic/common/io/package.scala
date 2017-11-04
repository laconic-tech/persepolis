package co.laconic.common

import java.io.InputStream

package object io {

  implicit class InputStreamExtensions(val is: InputStream) extends AnyVal {
    def toByteArray: Array[Byte] =
      Iterator
        .continually(is.read)
        .takeWhile(-1 !=)
        .map(_.toByte)
        .toArray
  }
}
