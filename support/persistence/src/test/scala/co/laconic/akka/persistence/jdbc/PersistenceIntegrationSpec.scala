package co.laconic.akka.persistence.jdbc

import akka.actor._
import akka.persistence.{PersistentActor, SnapshotOffer}
import akka.testkit._
import co.laconic.akka.persistence.jdbc.PersistenceIntegrationSpec._
import co.laconic.akka.persistence.support._
import org.scalatest.WordSpecLike

object PersistenceIntegrationSpec {
  // commands
  case class Multiply(value: Int)
  case class Add(value: Int)
  case class Subtract(value: Int)

  case object Fail
  case object TakeSnapshot

  // events
  case class StateUpdated(value: Int)

  object CalculatorActor {
    def props(name: String): Props = Props(new CalculatorActor(name))
  }

  class CalculatorActor(name: String) extends PersistentActor with ActorLogging {

    override def persistenceId: String = s"calculator:$name"

    var state = 0

    override def receiveRecover: Receive = {
      case StateUpdated(v) =>
        log.info("State recovered to {}", v)
        this.state = v
      case SnapshotOffer(_, snapshot: Int) =>
        log.info("State recovered to snapshot with value {}", snapshot)
        state = snapshot
    }

    override def receiveCommand: Receive = {
      case Add(value) => persistAndUpdate(state + value)
      case Multiply(value) => persistAndUpdate(state * value)
      case Subtract(value) => persistAndUpdate(state - value)
      case Fail => throw new Exception("Forced failure!")
      case TakeSnapshot =>
        saveSnapshot(state)
    }

    private def persistAndUpdate(state: Int): Unit = persist(StateUpdated(state)) {
      case StateUpdated(v) =>
        log.info("Persisted event with state {}", v)

        this.state = v
        sender ! state
    }
  }
}

class PersistenceIntegrationSpec extends TestKit(ActorSystem("PersistenceIntegrationSpec"))
  with ImplicitSender
  with WordSpecLike
  with Database {

  "A persistent calculator" should {
    "be able to recover it's state after a restart" in {
      val target = system.actorOf(CalculatorActor.props("1"))

      // mutate the state by performing some operations
      target ! Add(5)
      expectMsg(5)
      target ! Add(5)
      expectMsg(10)
      target ! Multiply(5)
      expectMsg(50)

      // force a failure so that the actor restarts
      target ! Fail

      // state should be recovered
      target ! Subtract(10)
      expectMsg(40)

      // take a snapshot, and force a new failure
      target ! TakeSnapshot
      target ! Fail
      target ! Add(10)
      expectMsg(50)
    }
  }
}
