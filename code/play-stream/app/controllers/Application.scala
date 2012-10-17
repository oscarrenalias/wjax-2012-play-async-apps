package controllers

import play.api._
import libs.Comet.CometMessage
import libs.concurrent.{Akka, Promise}
import libs.EventSource
import libs.iteratee.{Enumeratee, Enumerator}
import play.api.mvc._
import akka.util.duration._
import play.api.Play.current
import play.api.libs.json.Json._

sealed trait Status
case class Update(message: String, user: String) extends Status
object Update {
  def random = Update("this is some random text", "Test user")
}

object Application extends Controller {

  object Streams {
    // Generates random status updates every 5 seconds (TODO: can we randomize the time?)
    val updateGenerator = Enumerator.fromCallback { () =>
      Promise.timeout(Some(Update.random), 5000 milliseconds)
    }
  }
  
  def index = Action {
    Ok(views.html.stream())
  }

  def plainStream = Action {
    Async {
      Akka.future {
        // maps update events to json objects that we can deliver to the client
        val jsonConverter = Enumeratee.map[Update] { status =>
          toJson(Map("message" -> status.message, "user" -> status.user))
        }

        implicit val encoder = CometMessage.jsonMessages

        // in this stream we are sending the random event as-is in plain text
        Ok.feed(Streams.updateGenerator &> jsonConverter)
      }
    }
  }

  def stream = Action {
    Async {
      Akka.future {
        // maps update events to json objects that we can deliver to the client
        val jsonConverter = Enumeratee.map[Update] { status =>
          toJson(Map("message" -> status.message, "user" -> status.user))
        }

        implicit val encoder = CometMessage.jsonMessages
        // Here we compose our processing flow with EventSource, which ensures that our data
        // is correctly formatted according to the specs of the Javascript EventSource API
        Ok.feed(Streams.updateGenerator &> jsonConverter ><> EventSource()).as("text/event-stream")
      }
    }
  }
}