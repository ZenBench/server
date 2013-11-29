package utils

import play.api.Play

package object Filters {
  def short_cpu_name(cpuName: String) = {
    val exclusions = Play.current.configuration.getStringList("cpu.exclude").get
    cpuName.split(" ").filter(
      s => !exclusions.contains(s.trim) && !s.trim.isEmpty).reduce(
      (s1, s2) => s1 + " " + (s2))
  }

}
