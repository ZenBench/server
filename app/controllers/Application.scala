package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }


  def yeah = Action { Async{
    db.Runs.extractRuns.map( runs =>
      Ok(runs.toString)
    )
  }}

}