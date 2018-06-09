package pl.setblack.fibo

import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter
import org.springframework.web.reactive.function.BodyInserters.fromObject
import org.springframework.web.reactive.function.BodyInserters.fromPublisher
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import reactor.core.publisher.Mono
import reactor.ipc.netty.http.server.HttpServer
import java.util.function.Function

class FiboServer {
    fun start() {
        val route = router {
            GET("/fib/{n}")
            { request ->
                val n = Integer.parseInt(request.pathVariable("n"))
                if (n < 2) {
                    ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(fromObject<String>(n.toString()))
                } else {
                    val n_1 = WebClient.create("http://localhost:8080").get().uri("/fib/{n}", n - 1)
                            .accept(MediaType.TEXT_HTML).exchange()
                            .flatMap { resp -> resp.bodyToMono(String::class.java) }
                            .map<Int>{ Integer.parseInt(it) }
                    val n_2 = WebClient.create("http://localhost:8080").get().uri("/fib/{n}", n - 2)
                            .accept(MediaType.TEXT_HTML).exchange().flatMap { resp -> resp.bodyToMono(String::class.java) }
                            .map<Int> { Integer.parseInt(it) }

                    val result = n_1
                            .flatMap { a -> n_2.map { b -> a!! + b!! } }
                            .map<String>( { it.toString() })
                    ServerResponse.ok().contentType(MediaType.TEXT_HTML).body(fromPublisher<String, Mono<String>>(result, String::class.java))
                }
            }
        }

        val httpHandler = RouterFunctions.toHttpHandler(route)
        val adapter = ReactorHttpHandlerAdapter(httpHandler)
        val server = HttpServer.create("localhost", 8080)
        server.startAndAwait(adapter)
    }
}


fun main(args: Array<String>) {
    println("Hello, world!")
    FiboServer().start()

}