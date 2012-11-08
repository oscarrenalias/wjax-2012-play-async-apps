Some small demos of Play's reactive IO capabilities.

How to run
==========
The first demo is a very simple HTTP-based publish-subscribe API using enumerators and iteratees. After starting the application, use for example curl to subscribe to a topic with a client:

`curl -N http://localhost:9000/api/sub/testtopic`

We'll use -N so that curl does not buffer the output and shows it right away in the console.

You can open more than one connection with curl with the same topic or different ones.

In another console, you can now push messages to subscribers:

`curl -N http://locahost:9000/api/pub/testtopic`

Almost immediately, you should see the "testtopic" message returns to the clients that subscribed to that topic.

There are other assorted little examples in the code but this is probably the most interesting and useful one.