Play's basic asynchronous capabilities.

How to run
==========
Start the play application and use the following routes:

* /start - will open 100 iframes in the same page, and will keep all of them waiting until a promise is asynchronously redeemed. Shamelessly taken from one of the demos that the Play/Zenexity guys have done in their talks, because it really shows Play's asynchronous capabilities at its best. For double the fun, take a look at the amount of threads used with the VisualVM. After you've opened the page with the iframes, point your browser to http://localhost:9000/trigger/hello to see how all iframes receive "hello" as their content, naturally asynchronously.

* /repos/<github-huser> as an example of how to asynchronously call a web service and return some output to the browser. Try with http://localhost:9000/repos/oscarrenalias, for example