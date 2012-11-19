import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.ConsoleAppender

import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.WARN

appender("STDOUT", ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = "%d{HH:mm:ss} %-5level %logger{0} - %msg%n"
  }
}

logger("info.bonjean.beluga.util.HTTPUtil", INFO)
logger("info.bonjean.beluga.gui.UIBrowserListener", INFO)

root(DEBUG, ["STDOUT"])
