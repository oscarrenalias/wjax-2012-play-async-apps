package controllers

import play.api.libs.concurrent.Promise
import play.api.mvc.{Controller, Action, AsyncResult}

/**
 * This example has been presented very often by Sadek Drobi (Zenexity's CTO) but I
 * think it's a very good one so we will show it here again
 */
object PromiseExample extends Controller {

  // Global promise that all actions will wait for
  val promise = Promise[String]()

  // action that waits until the promise has been redeemed before sending a response to clients
  def waitForPromise = Action {
    new AsyncResult(promise.map(s => Ok(s)))
  }

  // renders our iframes
  def index = Action {
    Ok(views.html.frames())
  }

  // redeems the promise by providing a value
  def redeemPromise(s:String) = Action {
    promise.redeem(s)
    Ok("Done!")
  }
}
