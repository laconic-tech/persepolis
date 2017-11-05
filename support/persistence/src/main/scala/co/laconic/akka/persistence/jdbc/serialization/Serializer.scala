package co.laconic.akka.persistence.jdbc.serialization

import java.io.InputStream

import akka.serialization.Serialization
import co.laconic.common.io._

case class Serializer[T <: AnyRef](serialization: Serialization, clazz: Class[T]) {

  def serialize(o: T): Array[Byte] =
    serialization.serialize(o).get

  def deserialize(is: InputStream): T =
    serialization.deserialize(is.toByteArray, clazz).get
}
