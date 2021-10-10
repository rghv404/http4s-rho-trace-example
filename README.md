An example repo to show tracing with http4s-natchez and http4s-rho routes.

Requires Jaeger running locally.

Start up Jaeger:

```
docker run -d --name jaeger \
-e COLLECTOR_ZIPKIN_HTTP_PORT=9411 \
 -p 5775:5775/udp \
-p 6831:6831/udp \
-p 6832:6832/udp \
-p 5778:5778 \
-p 16686:16686 \
-p 14268:14268 \
-p 9411:9411 \

jaegertracing/all-in-one:1.8
```

Run this repo: 

* Go to http://localhost:8080/v1/rho/swagger-ui/#/ to see swagger UI
* Do some requests.
* Go to http://localhost:16686 and select `rho-api-example`
and search for traces.

Inspired by https://github.com/tpolecat/natchez-http4s/blob/main/modules/examples/src/main/scala/Example1.scala
and updated to run with rho routes

Currently meant to be canvas thus there's no server logic or backend. 