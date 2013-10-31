package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def home = Action {
    Ok(views.html.home())
  }


  def yeah = Action { Async{
    db.Runs.extractRuns.map( runs =>
      Ok(runs.toString)
    )
  }}

}