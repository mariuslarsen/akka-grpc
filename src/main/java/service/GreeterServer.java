package service;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import akka.grpc.javadsl.ServerReflection;
import akka.grpc.javadsl.ServiceHandler;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.japi.function.Function;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.GreeterServiceHandlerFactory;
import java.util.Collections;
import java.util.concurrent.CompletionStage;

public class GreeterServer {

  public static void main(String[] args) {
    Config config =
        ConfigFactory.parseString("akka.http.server.preview.enable-http2 = on")
            .withFallback(ConfigFactory.defaultApplication());

    ActorSystem<?> actorSystem = ActorSystem.create(Behaviors.empty(), "HelloWorld", config);
    run(actorSystem)
        .thenAccept(
            serverBinding ->
                System.out.println("gRPC server bound to: " + serverBinding.localAddress()));
  }

  public static CompletionStage<ServerBinding> run(ActorSystem<?> system) {
    // ServerReflection is added in order to use gRPCurl
    Function<HttpRequest, CompletionStage<HttpResponse>> service =
        ServiceHandler.concatOrNotFound(
            GreeterServiceHandlerFactory.create(new GreeterServiceImpl(system), system),
            ServerReflection.create(Collections.singletonList(GreeterService.description), system));

    return Http.get(system).newServerAt("127.0.0.1", 8090).bind(service);
  }
}
