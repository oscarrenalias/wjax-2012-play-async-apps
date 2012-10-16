package controllers

import play.api.libs.ws.WS
import play.api.mvc._
import play.api.libs.json._
import play.api.Logger
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.concurrent.Promise

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

  /**
   * Execute multiple calls in parallel
   */
  def repoBranches(user: String) = Action {
    Async {
      WS.url("https://api.github.com/users/" + user + "/repos").get().map { response =>
        val responses = (response.json \\ "name").map { repo =>
          // return the Future
          WS.url("https://api.github.com/repos/" + user + "/" + repo.as[String] + "/branches").get()
        }

        /*val results = requests.map { req =>
          req.map { resp =>
            JsObject(List("repo" -> JsString("TODO"), "branches" -> JsArray((resp.json \\ "name"))))
          }
        }*/

        //def conv(p:Promise[play.api.libs.ws.Response]) = p.map { resp => resp.json \\ "name" }
        Logger.debug(responses.map(promise => {
          promise.map(response => response.json \\ "name")
        }).mkString("-"))

        //Ok(Json.toJson(results))
        Ok(Json.toJson(JsObject(List("ok" -> JsString("ok")))))
      }
    }
  }
}
