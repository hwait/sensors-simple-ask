package sensorsapp

import pureconfig._
import pureconfig.generic.auto._

package object server {
  case class ServerConfig(host: String, port: Int)

  val serverConfig: ServerConfig = ConfigSource.default.at("server-config").loadOrThrow[ServerConfig]
}
