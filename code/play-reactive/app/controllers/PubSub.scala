package controllers

import play.api.mvc.{Action, Controller}
import play.api.libs.iteratee._
import akka.actor.{Props, Actor}
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.Play.current

/**
 * Example of publish-subscribe functionality using Play's reactive non-blocking
 * enumerators and an actor to keep track of the subscribers (because we cannot use the same
 * enumerator to feed into more than one iteratee)
 */

/**
 * This object handles the publish and subscribe functionality.
 *
 * The PubSubActor class is a very simple actor that handles new subscribers as well as
 * publishing messages into the "bus"
 */
case class Message(msg: String)
case class NewSubscriber(subscriber: PushEnumerator[String])

object PubSub {
  var subscribers = List[PushEnumerator[String]]()

  class PubSubActor extends Actor {
    def receive = {
      case NewSubscriber(subscriberChannel) => subscribers = subscribers ++ List(subscriberChannel)
      case Message(msg) => {
        Logger.debug("Publishing message: " + msg)
        subscribers.foreach(channel => channel.push(msg))
      }
    }
  }

  def sub(e:PushEnumerator[String]) = {
    actor ! NewSubscriber(e)
    e
  }

  def pub(msg:String) = actor ! Message(msg)

  val actor = Akka.system.actorOf(Props[PubSubActor])
}

object PubSubController extends Controller {
  // this generates events for all connected clients
  val consoleLogger = Iteratee.foreach[String](i => Logger.debug("message: " + i))

  def makeEnumerator = Enumerator.imperative(
    onStart = Logger.info("Starting Enumerator"),
    onComplete = Logger.info("That's all folks"),
    onError = (error:String, input:Input[String]) => println("There was an error: " + error)
  )

  def sub(topic:String) = Action {
    val enumerator = makeEnumerator
    PubSub.sub(enumerator)

    // the publish-subscribe actor will keep pushing data through the source enumerator, but individual
    // instances of the enumerator can of course apply their own transformations. In this example, we
    // will filter our messages that don't contain the given topic
    Logger.debug("topic = " + topic)
    Ok.stream(enumerator &> Enumeratee.filter { _ == topic })
  }

  def pub(s:String) = Action {
    Logger.debug("Pushing data to subscribers: " + s)
    PubSub.pub(s)
    Ok("Pushed!")
  }
}
