package com.kyleu.projectile.models.web

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorRefFactory, OneForOneStrategy, PoisonPill, Props, Status, SupervisorStrategy, Terminated}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.{CompletionStrategy, Materializer, OverflowStrategy}

object WebSocketUtils {
  class FlowActor(props: ActorRef => Props, connId: UUID, outActor: ActorRef) extends Actor {
    val flowActor = context.watch(context.actorOf(props(outActor), "flow-" + connId))

    def receive = {
      case Status.Success(_) | Status.Failure(_) => flowActor.tell(PoisonPill, self)
      case Terminated(_) => context.stop(self)
      case other => flowActor.tell(other, self)
    }

    override def supervisorStrategy = OneForOneStrategy() {
      case _ => SupervisorStrategy.Stop
    }
  }

  def actorRef[In, Out](connId: UUID)(
    props: ActorRef => Props, bufferSize: Int = 16, overflowStrategy: OverflowStrategy = OverflowStrategy.dropNew
  )(implicit factory: ActorRefFactory, mat: Materializer): Flow[In, Out, _] = {
    val cm: PartialFunction[Any, CompletionStrategy] = {
      case _ => CompletionStrategy.immediately
    }
    val fm: PartialFunction[Any, Throwable] = {
      case _ => throw new IllegalStateException("Websocket failure")
    }
    val (outActor, publisher) = Source.actorRef[Out](
      completionMatcher = cm,
      failureMatcher = fm,
      bufferSize = bufferSize,
      overflowStrategy = overflowStrategy
    ).toMat(Sink.asPublisher(false))(Keep.both).run()

    val ref = factory.actorOf(Props(classOf[FlowActor], props, connId, outActor), connId.toString)
    Flow.fromSinkAndSource(
      Sink.actorRef(ref, Status.Success(()), t => Status.Failure(t)),
      Source.fromPublisher(publisher)
    )
  }
}
