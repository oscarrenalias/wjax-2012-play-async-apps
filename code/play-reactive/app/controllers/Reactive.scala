package controllers

import play.api.libs.iteratee.{Iteratee, Input, Enumerator}
import play.api.libs.concurrent.Akka
import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.Logger

/**
 * This code contains a very naive implementation of a model class implemented in a reactive
 * style.
 *
 * For simplicity's sake, the code for all the different classes is kept in this same file, and
 * database operations are not implemented but only simulated as lists.
 */

case class User(name:String, email:Option[String] = None, password:Option[String] = None)

object ReactiveUserModel {
  def getAll = {
    val userEnumerator = Enumerator.imperative(
      onStart = println("Starting"),
      onComplete = println("Complete"),
      onError = (error:String, input:Input[User]) => println("there was an error: " + error)
    )

    // slowly get stuff from the database
    Akka.future {
      val userList = List(User("oscar"), User("david"), User("john"), User("jane"))
      userList.foreach(u => { println("pushing: " + u.name); userEnumerator.push(u)})

    }

    userEnumerator
  }
}

object Reactive extends Controller {
  val userIteratee = Iteratee.foreach[User](u => println(u))
}

object ReactiveAPI extends Controller {
  // this generates events for all connected clients
  val apiEnumerator = Enumerator.imperative(
    onStart = println("Starting evented API"),
    onComplete = println("That's all folks"),
    onError = (error:String, input:Input[String]) => println("There was an error: " + error)
  )

  def sub = Action {
    val clientSink = Iteratee.consume[String]()
    Ok.stream(apiEnumerator)
  }

  def pub(s:String) = Action {
    Logger.debug("Pushing data to subscribers: " + s)
    apiEnumerator.push(s)
    Ok("Pushed!")
  }
}
