package service;

import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import com.google.protobuf.Timestamp;
import example.myapp.helloworld.grpc.GreeterService;
import example.myapp.helloworld.grpc.HelloReply;
import example.myapp.helloworld.grpc.HelloRequest;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class GreeterServiceImpl implements GreeterService {

  private final ActorSystem<?> system;

  public GreeterServiceImpl(ActorSystem<?> system) {
    this.system = system;
  }

  @Override
  public CompletionStage<HelloReply> sayHello(HelloRequest in) {
    Instant now = Instant.now();
    System.out.println("sayHello to " + in.getName());
    HelloReply reply =
        HelloReply.newBuilder()
            .setMessage("Hello " + in.getName())
            .setTimestamp(
                Timestamp.newBuilder()
                    .setSeconds(now.getEpochSecond())
                    .setNanos(now.getNano())
                    .build())
            .build();
    return CompletableFuture.completedFuture(reply);
  }

  @Override
  public CompletionStage<HelloReply> itKeepsTalking(Source<HelloRequest, NotUsed> in) {
    Instant now = Instant.now();
    System.out.println("sayHello to in stream");
    return in.runWith(Sink.seq(), system)
        .thenApply(
            elements -> {
              String elementStr = elements.stream().map(HelloRequest::getName).toList().toString();
              return HelloReply.newBuilder()
                  .setMessage(elementStr)
                  .setTimestamp(
                      Timestamp.newBuilder()
                          .setSeconds(now.getEpochSecond())
                          .setNanos(now.getNano())
                          .build())
                  .build();
            });
  }

  @Override
  public Source<HelloReply, NotUsed> itKeepsReplying(HelloRequest in) {
    System.out.println("sayHello to " + in.getName() + " with stream of chars");
    List<String> characters = in.getName().chars().mapToObj(c -> String.valueOf((char) c)).toList();
    return Source.from(characters)
        .map(character -> HelloReply.newBuilder().setMessage(character).build());
  }

  @Override
  public Source<HelloReply, NotUsed> streamHellos(Source<HelloRequest, NotUsed> in) {
    return in.map(request -> HelloReply.newBuilder().setMessage(request.getName()).build());
  }
}
