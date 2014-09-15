package io.github.gatling.cql

import com.datastax.driver.core.ResultSet
import com.datastax.driver.core.Statement
import com.google.common.util.concurrent.FutureCallback
import com.typesafe.scalalogging.slf4j.StrictLogging

import akka.actor._
import io.gatling.core.result.message._
import io.gatling.core.result.writer.DataWriterClient
import io.gatling.core.session.Session
import io.gatling.core.util.TimeHelper.nowMillis

class CqlResponseHandler(next: ActorRef, session: Session, start: Long, tag: String, stmt: Statement)
  extends FutureCallback[ResultSet]
  with DataWriterClient
  with StrictLogging {
  
  private def writeData(status: Status, message: Option[String]) = writeRequestData(session, tag, start, nowMillis, session.startDate, nowMillis, status, message, Nil)
  
  def onSuccess(result: ResultSet) = {
    writeData(OK, None)
    next ! session.markAsSucceeded
  }

  def onFailure(t: Throwable) = {
    logger.error(s"$stmt", t)
    writeData(KO, Some(s"Error executing statement: $t"))
    next ! session.markAsFailed
  }
}