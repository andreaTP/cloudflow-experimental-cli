package cloudflow
package actions

import buildinfo.BuildInfo

import scala.concurrent.Future

case object Version extends Action {
  def run() = Future.successful(Console.out.println(BuildInfo.version))
}
