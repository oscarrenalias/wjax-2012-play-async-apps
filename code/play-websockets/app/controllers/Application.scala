package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.iteratee._
import java.io.File
import java.util.Date
import akka.util.duration._

object Application extends Controller {
  
  //
  // Displays the page with the WebSocket Javascript code that can be used to connect
  // to the different endpoints for testing.
  //
  def index = Action {
    Ok(views.html.websocket())
  }

  //
  // The most basic WebSocket possible; will reply with "hello" once a client connects, and
  // immediately close the connection
  //
  def websocketHello = WebSocket.using[String] { request =>
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }
    val out = Enumerator("Hello!")

    (in, out)
  }

  //
  // Standard 'echo' websocket service
  // Use "ws://localhost:9000/asyncEcho" to connect to this endpoint from JavaScript code
  //
  def echo = WebSocket.using[String] { request =>
    val out = Enumerator.imperative[String]()
    val in = Iteratee.foreach[String] { message =>
      Logger.debug("Received message: " + message)
      out.push(message)
    }.mapDone(_ => println("Disconnected"))
    (in, out)
  }

  //
  // 'echo' websocket service implemented using asynchronous capabilities
  // Use "ws://localhost:9000/asyncecho" to connect to this endpoint from JavaScript code
  //
  def asyncEcho = WebSocket.async[String] { request =>
    Akka.future {
      val out = Enumerator.imperative[String]()
      val in = Iteratee.foreach[String] { message =>
        out.push(message)
      }.mapDone(_ => println("Disconnected"))
      (in, out)
    }
  }

  //
  // websocket service that returns the time when connected to the endpoint, every 5 seconds or so
  // Use "ws://localhost:9000/time" to connect to this endpoint from JavaScript code, and the
  // time will be automatically delivered via the WebSocket connection
  //
  def websocketTime = WebSocket.async[String] { request =>
    Akka.future {
      val timeEnumerator = Enumerator.fromCallback { () =>
        Promise.timeout(Some((new Date).toString()), 5000 milliseconds)
      }

      val in = Iteratee.foreach[String] { message => println(message) }

      (in, timeEnumerator)
    }
  }
}