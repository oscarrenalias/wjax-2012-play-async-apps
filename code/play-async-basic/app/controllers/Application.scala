package controllers

import play.api._
import play.api.libs.concurrent.Akka
import libs.json._
import play.api.mvc._
import models.SalesOrder
import models.SalesOrder._
import play.api.Play.current

object Application extends Controller {
  
  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def JsonError(message: String) = JsObject(
    List("error" -> JsBoolean(true), "message" -> JsString(message))
  )

  /**
   * This is the most basic way to have an asynchronous response, using the AsyncResult placeholder, which
   * receives a Promise[Result]
   * @return
   */
  def basicAsyncResponse = Action {
    val p = Akka.future {            [[]]
      Ok("I am done")
    }
    new AsyncResult(p)
  }

  /**
   * Improved implementation of the action above, now using a timeout and the Async construct
   * @return
   */
  def betterAsyncResponse = Action {
    Async {
      Akka.future {
        "I am done"
      } orTimeout(Ok("There was an error"), 5, java.util.concurrent.TimeUnit.SECONDS) map { result =>
        result.fold(
          content => Ok("content: " + content),
          error => Ok("There was an error: " + error.toString)
        )
      }
    }
  }

  def orders = Action {
    Async {
      Akka.future {
        SalesOrder.findAll
      } orTimeout(Ok(Json.toJson(JsonError("Timeout while reading data"))), 5, java.util.concurrent.TimeUnit.SECONDS) map { orders =>
        orders.fold(
          orders => Ok(Json.toJson(orders)),
          error => Ok(Json.toJson(JsonError("Error")))
        )
      }
    }
  }

  /**
   * Some glue code can make code look prettier
   */
  def WithFuture[T](timeoutSeconds:Int)(f: => T)(implicit jsonHelper:Writes[T]) = {
    Async {
      Akka.future {
        f
      } orTimeout(Ok(Json.toJson(JsonError("Timeout while reading data"))), timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS) map { result =>
        result.fold(
          data => Ok(Json.toJson(data)),
          error => Ok(Json.toJson(JsonError("Error")))
        )
      }
    }
  }

  def prettyOrders = Action {
    WithFuture(1) {
      SalesOrder.findAll
    }
  }
}