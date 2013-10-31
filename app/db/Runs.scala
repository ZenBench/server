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

import models._

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

  def prepareBenchs(f: InnerRuns => Boolean = { _ => true }, tests: Future[Map[String, InnerTest]] = extractTests ): Future[Seq[Bench]] = {
    val runs = extractRuns

    for {
      r <- runs
      t <- tests
    } yield {
      r.filter(f).map { run =>
        val typeRef = run.env.map{ e => (e.types, e.ref) }.toMap

        run.metrics.map{ m =>
          for {
            test <- t.get(m.id)
            ref  <- typeRef.get(test.types)
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

  def preparedTests: Future[Seq[Test]] = {
    val tests = extractTests
    for{
      t <- tests
      b <- prepareBenchs( tests = tests )
    } yield {
      b.groupBy[String]( b => b.test ).toSeq.map{ case (key, values) =>
        t.get(key).map { test =>
          val (min, max, sum, count) = values.foldLeft[(Long, Long, Long, Long)]((Long.MaxValue, Long.MinValue, 0, 0)){ case ((mi, ma, sum, count), v) =>
            (
              if( v.mean < mi ) v.mean else mi,
              if( v.mean > ma ) v.mean else ma,
              sum + v.mean,
              count + v.count
            )
          }
          Test(test.id, test.description, test.types, min, sum / values.size, max, count, values.sortWith((t1, t2) => t1.mean > t2.mean))
        }
      }
    }.flatten.sortWith( (t1, t2) => t1.bench.size > t2.bench.size )
  }

  def hosts: Future[Seq[(String, String)]] = extractRuns.map { runs =>
    runs.map( r => (r.id, r.host)).distinct
  }

  def preparedHost(host: String): Future[Host] = prepareBenchs({ i => i.host == host }).map( Host(host, _) )
}