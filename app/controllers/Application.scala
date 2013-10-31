package controllers

import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def home = Action.async {
    db.Runs.preparedTests.map{ runs => Ok(views.html.home(runs)) }
  }


  def runs = Action.async {
    db.Runs.extractRuns.map( runs =>
      Ok(runs.toString)
    )
  }

  def tests = Action.async {
    db.Runs.extractRuns.map( runs =>
      Ok(runs.toString)
    )
  }

  def benchs = Action.async {
    db.Runs.prepareBenchs().map( runs =>
      Ok(runs.toString)
    )
  }

  def hosts = Action.async {
    db.Runs.hosts.map( runs => Ok(runs.toString) )
  }

  def preparedTests = Action.async {
    db.Runs.preparedTests.map( runs =>
      Ok(runs.toString)
    )
  }

  def preparedHost(host: String) = Action.async {
    db.Runs.preparedHost(host).map( host => Ok(host.toString) )
  }
}