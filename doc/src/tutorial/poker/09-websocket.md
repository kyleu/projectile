# Planning Poker

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate) and switched to the `08-scala-js` branch

@@@


TODO: Websocket


## Network messages

Like all real-time web applications, we'll be using a websocket, passing JSON or binary serialized data bi-directionally.
There are eight different client messages, and ten different server messages:

`shared/src/main/scala/models/message/ClientMessage.scala`

```scala
package models.message

import java.util.UUID

import com.kyleu.projectile.util.BinarySerializers._
import com.kyleu.projectile.util.JsonSerializers._
import models.poll.PollStatusType

sealed trait ClientMessage

object ClientMessage {
  implicit val jsonEncoder: Encoder[ClientMessage] = deriveEncoder
  implicit val jsonDecoder: Decoder[ClientMessage] = deriveDecoder
  implicit val pickler: Pickler[ClientMessage] = generatePickler

  final case class Ping(ts: Long) extends ClientMessage

  final case class JoinSession(id: UUID) extends ClientMessage

  sealed trait SessionRequest

  final case class UpdateSession(name: String, choices: Seq[String]) extends ClientMessage with SessionRequest
  final case class UpdateProfile(name: String) extends ClientMessage with SessionRequest
  final case class AddPoll(str: String) extends ClientMessage with SessionRequest
  final case class UpdatePoll(poll: UUID, title: Option[String]) extends ClientMessage with SessionRequest
  final case class SetPollStatus(poll: UUID, status: PollStatusType) extends ClientMessage with SessionRequest
  final case class SubmitVote(poll: UUID, vote: String) extends ClientMessage with SessionRequest
}
```

`shared/src/main/scala/models/message/ClientMessage.scala`

```scala
package models.message

import java.util.UUID

import com.kyleu.projectile.util.BinarySerializers._
import com.kyleu.projectile.util.JsonSerializers._
import models.poll.{PollRow, VoteRow}
import models.session.{MemberRow, SessionRow}

sealed trait ServerMessage

object ServerMessage {
  implicit val jsonEncoder: Encoder[ServerMessage] = deriveEncoder
  implicit val jsonDecoder: Decoder[ServerMessage] = deriveDecoder
  implicit val pickler: Pickler[ServerMessage] = generatePickler

  // System
  final case class VersionResponse(version: String) extends ServerMessage
  final case class Pong(ts: Long, serverTime: Long) extends ServerMessage
  final case class Disconnected(reason: String) extends ServerMessage

  // Session
  final case class SessionNotFound(id: UUID) extends ServerMessage
  final case class SessionJoined(
      session: SessionRow, members: Seq[MemberRow], connected: Seq[UUID], polls: Seq[PollRow], votes: Seq[VoteRow]
  ) extends ServerMessage
  final case class SessionUpdate(session: SessionRow) extends ServerMessage

  // Members
  final case class MemberStatusUpdate(participant: UUID, connected: Boolean) extends ServerMessage
  final case class MemberUpdate(member: MemberRow) extends ServerMessage

  // Polls
  final case class PollUpdate(poll: PollRow) extends ServerMessage
  final case class VoteUpdate(vote: VoteRow) extends ServerMessage
}
```


## Controllers, routes, and views

TODO


## Explore the code

https://github.com/KyleU/estimate/tree/09-websocket

See this branch's Pull Request for detailed comments on the generated files

https://github.com/KyleU/estimate/pull/9


## Next steps

Ok, we're all wired up - let's finish this thing and @ref[build a real app](10-planning-poker.md)!
