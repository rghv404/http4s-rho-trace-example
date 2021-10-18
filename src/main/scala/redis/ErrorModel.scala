package redis

sealed trait JobError

sealed trait RedisFailure extends JobError {
  val message: String
}

case class KeyNotFound(message: String) extends RedisFailure

case class DecodeError(message: String) extends RedisFailure