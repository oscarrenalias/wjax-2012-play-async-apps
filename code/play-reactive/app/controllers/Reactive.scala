package controllers

import play.api.libs.iteratee._
import play.api.libs.concurrent.Akka
import play.api.mvc.{Action, Controller}
import play.api.Play.current
import play.api.Logger
import akka.actor.{Actor, Props}
import play.api.libs.json.{JsString, Json, JsObject, Writes}

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
    val userList = List(User("oscar"), User("david"), User("john"), User("jane"))

    val userEnumerator = Enumerator.pushee[User] (
      onStart = pushee => {
        userList.foreach { user => pushee.push(user) }
        pushee.close()
      },
      onError = (error:String, input:Input[User]) => println("there was an error: " + error)
    )

    userEnumerator
  }
}

object Reactive extends Controller {
  implicit object UserJsonWriter extends Writes[User] {
    override def writes(u:User) = JsObject(List("user" -> JsString(u.name), "email" -> JsString(u.email.getOrElse(""))))
  }

  val asJson = Enumeratee.map[User] { u => Json.toJson(u) }

  def index = Action {
    Ok(views.html.users.render())
  }

  def get = Action {
    Ok.stream(ReactiveUserModel.getAll &> asJson)
  }
}
