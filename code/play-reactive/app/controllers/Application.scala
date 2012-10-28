package controllers

import play.api._
import libs.Comet.CometMessage
import libs.concurrent.{Akka, Promise}
import libs.EventSource
import libs.iteratee.{Iteratee, Enumeratee, Enumerator}
import play.api.mvc._
import akka.util.duration._
import play.api.Play.current
import play.api.libs.json.Json._

sealed trait Status
case class Update(message: String, user: String) extends Status
object Update {
  def random = Update("this is some random text", "Test user")
}

// most of these are as defined in the official documentation for Iteratees here:
// http://www.playframework.org/documentation/2.0.4/Iteratees
object MyIteratees {
  // sums up the input
  val inputLength = Iteratee.fold[Array[Byte], Int](0) { (length, bytes) => length + bytes.size }
}

// sample enumerators (generators of data for iteratees)
object MyEnumerators {
  // generates strings
  val stringEnumerator = Enumerator("one", "two", "three", "four")
  // converts strings to Array[Byte] so that we can push them into the inputLength iteratee
  val toByteArray = Enumeratee.map[String] { s => s.getBytes }
}

object Combinations {
  // put together Iteratees, Enumerators and Enumeratees
  import MyEnumerators._
  import MyIteratees._

  // let's hook up these puppies
  val countInput = stringEnumerator &> toByteArray |>> inputLength
}

/**
 * How to combine iterators and extract their result
 *
 * val r1 = stringEnumerator(inputLength)  // returns Promise[Iteratee[String, Int]]
 * val r2 = r1.flatMap(i => i.run)  // run the iteratee, returns Promise[Int]
 * val resultFromIteratee = r2.value.get  // keep the value
 * r2.onRedeem(s => println(s))   // or operate on it without extracting it
 *
 * The first two steps can be combined so that we get access to the Promise[Int] right away
 * using Iteratee.flatten:
 *
 * val promiseOfInt = Iteratee.flatten(stringEnumerator(inputLength)).run // returns Promise[Int]
 * val result = promiseOfInt.value.get
 */
object Application extends Controller {

  object Streams {
    // Generates random status updates every 5 seconds (TODO: can we randomize the time?)
    Logger.debug("Initializing the random stream generator")
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