package cloudflow

import buildinfo.BuildInfo
import wvlet.log.Logger
import wvlet.log.LogLevel

class CliLogger(level: String) {

  private val logger = {
    Logger.init
    val _logger = Logger(BuildInfo.name)
    _logger.setLogLevel(LogLevel(level))
    _logger
  }

  def trace(msg: => String) = logger.trace(msg)
  def info(msg: => String) = logger.info(msg)
  def warn(msg: => String) = logger.warn(msg)
  def error(msg: => String) = logger.error(msg)

  def close() = {
    logger.getHandlers.foreach { handler =>
      handler.flush()
      handler.close()
    }
  }
}
