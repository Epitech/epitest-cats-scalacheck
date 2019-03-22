package org.scalacheck.cats.instances

import cats._
import cats.implicits._
import org.scalacheck.Gen
import org.scalacheck.rng.Seed
import scala.annotation.tailrec

object GenInstances extends GenInstances

trait GenInstances extends GenInstances1

sealed private[instances] trait GenInstances1 extends GenInstances0 {
  implicit val catsInstancesForScalacheckGen: Monad[Gen]
    with Alternative[Gen]
    with FunctorFilter[Gen] =
    new Monad[Gen] with Alternative[Gen] with FunctorFilter[Gen] {
      // Members declared in cats.Applicative
      override def pure[A](x: A): Gen[A] =
        Gen.const(x)

      // Members declared in cats.FlatMap
      override def flatMap[A, B](fa: Gen[A])(f: A => Gen[B]): Gen[B] =
        fa.flatMap(f)
      override def tailRecM[A, B](a: A)(f: A => Gen[Either[A, B]]): Gen[B] = {

        @tailrec
        def tailRecMR(a0: A, seed: Seed, labs: Set[String])(
            fn: (A, Seed) => Gen.R[Either[A, B]]): Gen.R[B] = {
          val re = fn(a0, seed)
          val nextLabs = labs | re.labels
          re.retrieve match {
            case None => Gen.r(None, re.seed).copy(l = nextLabs)
            case Some(Right(b)) => Gen.r(Some(b), re.seed).copy(l = nextLabs)
            case Some(Left(a1)) => tailRecMR(a1, re.seed, nextLabs)(fn)
          }
        }

        // This is the "Reader-style" appoach to making a stack-safe loop:
        // we put one outer closure around an explicitly tailrec loop
        Gen.gen[B] { (p: Gen.Parameters, seed: Seed) =>
          tailRecMR(a, seed, Set.empty) { (a, seed) =>
            f(a).doApply(p, seed)
          }
        }
      }

      override def combineK[A](x: Gen[A], y: Gen[A]): Gen[A] = Gen.gen { (params, seed) =>
        val xGen = x.doApply(params, seed)
        if (xGen.retrieve.isDefined) xGen
        else y.doApply(params, seed)
      }

      override def empty[A]: Gen[A] = Gen.fail

      override def map2Eval[A, B, Z](fa: Gen[A], fb: Eval[Gen[B]])(f: (A, B) => Z): Eval[Gen[Z]] =
        Eval.later(map2(fa, Gen.lzy(fb.value))(f))

      override def product[A, B](fa: Gen[A], fb: Gen[B]): Gen[(A, B)] = Gen.zip(fa, fb)

      override def functor: Functor[Gen] = this

      override def mapFilter[A, B](fa: Gen[A])(f: A => Option[B]): Gen[B] =
        fa.flatMap { a =>
          f(a) match {
            case Some(b) => pure(b)
            case _ => Gen.fail
          }
        }
    }

  implicit def catsMonoidInstanceForScalacheckGen[A: Monoid]: Monoid[Gen[A]] = new Monoid[Gen[A]] {
    override def empty: Gen[A] = Gen.const(Monoid[A].empty)
    override def combine(x: Gen[A], y: Gen[A]) =
      for {
        xa <- x
        ya <- y
      } yield xa |+| ya
  }

}

sealed private[instances] trait GenInstances0 {
  implicit def catsSemigroupInstanceForScalacheckGen[A: Semigroup]: Semigroup[Gen[A]] =
    new Semigroup[Gen[A]] {
      override def combine(x: Gen[A], y: Gen[A]) =
        for {
          xa <- x
          ya <- y
        } yield xa |+| ya
    }
}
