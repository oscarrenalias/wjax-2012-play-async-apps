package controllers

import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.json.{Json, JsNumber, JsString, JsObject}

object Service extends Controller {

  def userRepos(user: String) = Action {
    Async {
      WS.url("https://api.github.com/users/" + user + "/repos").get().map { response =>
        val repoList = (response.json \\ "html_url").map{ repo =>
          JsObject(List("repo" -> repo))
        }

        Ok(Json.toJson(repoList))
      }
    }
  }
}
