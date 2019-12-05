package example

import zio._

object Hello extends App {

  import distage.{DIKey, GCMode, ModuleDef, Injector, ProviderMagnet, Tag}
  import izumi.distage.constructors.TraitConstructor
  import zio.console.{Console, putStrLn}
  import zio.{UIO, URIO, URManaged, ZIO, Ref, Task}

  trait Hello {
    def hello: UIO[String]
  }

  trait World {
    def world: UIO[String]
  }

  // Environment forwarders that allow
  // using service functions from everywhere

  val hello: URIO[ {def hello: Hello}, String] = ZIO.accessM(_.hello.hello)

  val world: URIO[ {def world: World}, String] = ZIO.accessM(_.world.world)

  // service implementations

  val makeHello = {
    (for {
      _ <- putStrLn("Creating Enterprise Hellower...")
      hello = new Hello {
        val hello = UIO("Hello")
      }
    } yield hello).toManaged { _ =>
      putStrLn("Shutting down Enterprise Hellower")
    }
  }

  val makeWorld = {
    for {
      counter <- Ref.make(0)
    } yield new World {
      val world = counter.get.map(c => if (c < 1) "World" else "THE World")
    }
  }

  // the main function

  val turboFunctionalHelloWorld = {
    for {
      hello <- hello
      world <- world
      _ <- putStrLn(s"$hello $world")
    } yield ()
  }

  // a generic function that creates an `R` trait where all fields are populated from the object graph

  def provideCake[R: TraitConstructor, A: Tag](fn: R => A): ProviderMagnet[A] = {
    TraitConstructor[R].provider.map(fn)
  }

  val definition = new ModuleDef {
    make[Hello].fromResource(provideCake(makeHello.provide(_)))
    make[World].fromEffect(makeWorld)
    make[Console.Service[Any]].fromValue(Console.Live.console)
    make[UIO[Unit]].from(provideCake(turboFunctionalHelloWorld.provide))
  }

  val main = Injector()
    .produceF[Task](definition, GCMode(DIKey.get[UIO[Unit]]))
    .use(_.get[UIO[Unit]])

  def run(args: List[String]) =
    main
      .foldM(
        e => putStrLn(e.getMessage).as(1),
        _ => IO.succeed(0)
      )
}
