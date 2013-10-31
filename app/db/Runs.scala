package db


import play.api.libs.json._
import play.api.libs.functional.syntax._

// Reactive JSONCollection

import reactivemongo.api._
import play.modules.reactivemongo.ReactiveMongoPlugin
import play.modules.reactivemongo.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.Play.current

import scala.concurrent.Future


case class Bench(test: String, types: String, ref: String, mean: Long, count: Long)

case class Test(id: String, name: String, types: String, bench: Seq[Bench])
case class Host(id: String, bench: Seq[Bench])

object Runs {
  lazy val collectionRuns = ReactiveMongoPlugin.db.collection[JSONCollection]("runs")
  lazy val collectionTests = ReactiveMongoPlugin.db.collection[JSONCollection]("tests")

  case class Env(types: String, ref: String)
  case class Run(id: String, value: String)
  case class InnerRuns(id: String, host: String, env: Seq[Env], metrics: Seq[Run])
  case class InnerTest(id: String, description: String, types: String)

  def extractRuns: Future[Seq[InnerRuns]] = {
    implicit val envReads = Json.reads[Env]
    implicit val runReads = Json.reads[Run]
    implicit val runsReads = Json.reads[InnerRuns]

    collectionRuns.find(Json.obj()).cursor[JsObject].collect[Seq]().map { runs =>
      runs.map { json =>
        Json.fromJson[InnerRuns](json).asOpt
      }.flatten
    }
  }

  def extractTests: Future[Map[String, InnerTest]] = {
    implicit val runsReads = Json.reads[InnerTest]

    collectionTests.find(Json.obj()).cursor[JsObject].collect[Seq]().map { runs =>
      runs.map { json =>
        Json.fromJson[InnerTest](json).asOpt.map{ t => (t.id, t) }
      }.flatten.toMap
    }
  }

  def prepareRuns(f: InnerRuns => Boolean = { _ => true } ): Future[Seq[Bench]] = {
    val runs = extractRuns
    val tests = extractTests

    for {
      r <- runs
      t <- tests
    } yield {
      r.filter(f).map { run =>
        val typeRef = run.env.map{ e => (e.types, e.ref) }.toMap
        run.metrics.map{ m =>
          for {
            test <- t.get(m.id)
            ref  <- typeRef.get(test.id)
          } yield Bench(test.id, test.types, ref, m.value.toLong, 1)
        }
      }.flatten
       .flatten
       .groupBy[(String, String, String)]{ b => (b.test, b.types, b.ref)}
       .toSeq.map{ case (k, values) =>
          val sum = values.foldLeft[Long](0){ (acc, v) => acc + v.mean }
          Bench(k._1, k._2, k._3, sum / values.size, values.size)
       }
    }
  }
}