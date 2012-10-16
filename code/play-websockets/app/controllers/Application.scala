package controllers

import play.api._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.libs.iteratee._

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def websocket = WebSocket.using[String] { request =>

  // Log events to the console
    val in = Iteratee.foreach[String](println).mapDone { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")

    (in, out)
  }

  def testSocket = Action {
    Ok(views.html.websockettest())
  }
}