package org.novelfs.taglessmetrics.kamon

import _root_.kamon.Kamon
import _root_.kamon.testkit.MetricInspection
import cats.effect.IO
import cats.implicits._
import org.novelfs.taglessmetrics.kamon.instances.all._
import org.novelfs.taglessmetrics.{DecrementMetric, IncrementMetric}
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}

import scala.util.Random

class RangeSamplerSpec extends FlatSpec with Matchers with MetricInspection with GeneratorDrivenPropertyChecks  {
  val positiveInt = Gen.posNum[Int]

  implicit val ioTimer = IO.timer(scala.concurrent.ExecutionContext.global)

  "increment" should "increment the metric the number of times it is called" in {
    forAll(positiveInt) { expectedRangeSamplerValue: Int =>
      val suffix = Random.alphanumeric.take(10).mkString
      val kamonRangeSampler = Kamon.rangeSampler("test-range-sampler-" + suffix)
      val testRangeSampler = RangeSampler("test-range-sampler-" + suffix)
      val incrMetric = IncrementMetric[IO, RangeSampler].increment(testRangeSampler)
      List.fill(expectedRangeSamplerValue)(incrMetric).sequence_.unsafeRunSync()
      val actualRangeSamplerValue = kamonRangeSampler.distribution().max
      actualRangeSamplerValue shouldBe expectedRangeSamplerValue
    }
  }

  "incrementTimes" should "increment the metric by n" in {
    forAll(positiveInt) { expectedRangeSamplerValue: Int =>
      val suffix = Random.alphanumeric.take(10).mkString
      val kamonRangeSampler = Kamon.rangeSampler("test-range-sampler-" + suffix)
      val testRangeSampler = RangeSampler("test-range-sampler-" + suffix)
      IncrementMetric[IO, RangeSampler].incrementTimes(expectedRangeSamplerValue.toLong)(testRangeSampler).unsafeRunSync()
      val actualRangeSamplerValue = kamonRangeSampler.distribution().max
      actualRangeSamplerValue shouldBe expectedRangeSamplerValue
    }
  }
}
