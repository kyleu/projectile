# Websocket

@@@ note { title=Prerequisites }

This page assumes you have checked out a local copy of [estimate.poker](https://github.com/KyleU/estimate) and switched to the `08-scala-js` branch

@@@


## Websocket Library

Edit `Server.scala`, and change it to depend on @ref[`projectile-lib-websocket`](../../libraries/websocket.md):

`project/Dependencies.scala`:

```val all = Seq("websocket", "slick").map(s => "com.kyleu" %% s"projectile-lib-$s" % version)```


## Network messages

Like most real-time web applications, we'll be using a websocket, passing JSON or binary serialized data bi-directionally.
There are eight different client messages, and ten different server messages. This is our first planning-poker-specific code

- [`shared/src/main/scala/models/message/ClientMessage.scala`](https://github.com/KyleU/estimate/blob/09-websocket/shared/src/main/scala/models/message/ClientMessage.scala)
- [`shared/src/main/scala/models/message/ServerMessage.scala`](https://github.com/KyleU/estimate/blob/09-websocket/shared/src/main/scala/models/message/ServerMessage.scala)


## Controllers, routes, and views

TODO


## Explore the code

https://github.com/KyleU/estimate/tree/09-websocket

See this branch's Pull Request for detailed comments on the generated files

https://github.com/KyleU/estimate/pull/9


## Next steps

Ok, we're all wired up - let's finish this thing and @ref[build a real app](10-planning-poker.md)!
