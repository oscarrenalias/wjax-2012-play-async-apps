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
  
  def index = Action {
    Ok(views.html.websocket())
  }

  def websocketHello = WebSocket.using[String] { request =>
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }
    val out = Enumerator("Hello!")

    (in, out)
  }

  // standard 'echo' websocket service
  def echo = WebSocket.using[String] { request =>
    val out = Enumerator.imperative[String]()
    val in = Iteratee.foreach[String] { message =>
      Logger.debug("Received message: " + message)
      out.push(message)
    }.mapDone(_ => println("Disconnected"))
    (in, out)
  }

  // 'echo' websocket service implemented using asynchronous capabilities
  def asyncEcho = WebSocket.async[String] { request =>
    Akka.future {
      val out = Enumerator.imperative[String]()
      val in = Iteratee.foreach[String] { message =>
        out.push(message)
      }.mapDone(_ => println("Disconnected"))
      (in, out)
    }
  }

  // returns file contents via websocket
  def catFile = WebSocket.async[String] { request =>
    Akka.future {
      val fileEnumerator = Enumerator.fromFile(new File("/etc/passwd")).map(x => new String(x))

      val in = Iteratee.foreach[String]{println(_)}

      (in, fileEnumerator)
    }
  }

  // websocket service that returns the time when connected to the endpoint, very 5 seconds or so
  def websocketTime = WebSocket.async[String] { request =>
    Akka.future {
      val timeEnumerator = Enumerator.fromCallback { () =>
        Promise.timeout(Some((new Date).toString()), 5000 milliseconds)
      }

      val in = Iteratee.foreach[String] { message => println(message) }

      (in, timeEnumerator)
    }
  }

  def test = WebSocket.using[String]{ request =>
    val in = Iteratee.foreach[String] { message => println(message) }
    val channel = Enumerator.pushee[String] { pushee =>
      pushee.push("Hello")
      pushee.push("World")
    }

    /*val onStart = () => println("enumerator started")
    val onComplete = () => println("enumerator complete")
    val onError = (s:String, input:Input[String]) => println("enumerator error")
    val out = new PushEnumerator[String](onStart, onComplete, onError)*/

    (in, channel)
  }
}