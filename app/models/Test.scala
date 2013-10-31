package models

case class Test(id: String, name: String, types: String, min: Long, mean: Long, max: Long, count: Long, bench: Seq[Bench])