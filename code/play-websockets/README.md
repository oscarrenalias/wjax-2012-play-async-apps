This application demonstrates how to use Websockets in Play.

The application provides two websocket endpoints: a simple echo service as well as a service that returns the server time every 5 seconds over websockets.

How to run
==========
Start the application with ```play run``` and then visit http://localhost:9000.

In the "Websocket endpoint" textbox, connect to these two endpoints:

* Use ws://localhost:9000/echo to test an 'echo' service. Simply write content in the "Send content" box and it will appear on the right hand side of the page under "WebSocket responses"

* Use ws://localhost:9000/time for the time service; the server will report the time every 5 seconds via the WebSocket channel and it will appear on the right hand side of the page.