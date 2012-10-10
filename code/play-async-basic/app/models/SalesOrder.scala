package models

object OrderStatus extends Enumeration {
  type OrderStatus = Value
  val New, Cancelled, Fulfilled = Value
}

import OrderStatus._
import play.api.libs.json.Writes
import play.api.libs.json.JsArray
import play.api.libs.json.JsObject
import play.api.libs.json.JsString
import play.api.libs.json.JsNumber

case class SalesOrder(id:String, user:String, status:OrderStatus, items:SalesOrder.ItemType)

object SalesOrder{
  type ItemType = Map[Int,(Int,String)]

  def findAll = {
    (1 to 500).map { id =>
      SalesOrder(id.toString, "random-user", OrderStatus.New, Map(1 -> (10, "item 1"), 2 -> (2, "item 2")))
    }.toList
  }

  implicit object SalesOrderJsonWriter extends Writes[SalesOrder] {
    def writes(o: SalesOrder) = JsObject(List(
      "id" -> JsString(o.id),
      "user" -> JsString(o.user),
      "status" -> JsString(o.status.toString),
      "items" -> JsArray(
        o.items.map(item => item match { case (id, (amount, desc)) =>
          JsObject(List("id" -> JsNumber(id), "amount" -> JsNumber(amount), "description" -> JsString(desc)))
        }).toSeq)
    )
    )
  }
}