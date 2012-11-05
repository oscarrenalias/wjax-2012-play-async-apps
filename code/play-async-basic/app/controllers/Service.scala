package controllers

import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.json._
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.concurrent.{Akka, Promise}

object Service extends Controller {

  /**
   * Call a RESTful service asynchronously and return the results whenever they're ready
   */
  def userRepos(user: String) = Action {
    Async {
      WS.url("https://api.github.com/users/" + user + "/repos").get().map { response =>
        val repoList = (response.json \\ "html_url").map { repo =>
          JsObject(List("repo" -> repo))
        }

        Ok(Json.toJson(repoList))
      }
    }
  }
}
