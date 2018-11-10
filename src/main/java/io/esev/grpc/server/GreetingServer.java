package io.esev.grpc.server;

import io.esev.grpc.GreetingServiceGrpc;
import io.esev.grpc.HelloRequest;
import io.esev.grpc.HelloResponse;
import io.esev.grpc.commons.Constant;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerInterceptors;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GreetingServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("[SERVER] Server Greeting Starting!");
        Server server = ServerBuilder
                            .forPort(9191)
                            .addService(ServerInterceptors.intercept(new GreetingServiceImpl(), new JwtServerInterceptor(Constant.JWT_SECRET), new TraceIdServerInterceptor()))
                            .build();


        server.start();
        System.out.println("[SERVER] Server Started");

        server.awaitTermination();
    }

    public static class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

        @Override
        public void greeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

            System.out.println("[SERVICE] Greeting Service Starting!");
            System.out.println(request);

            String userId = Constant.USER_ID_CTX_KEY.get();
            String traceId = Constant.TRACE_ID_CTX_KEY.get();

            System.out.println("[SERVICE] UserId:" + userId);
            System.out.println("[SERVICE] TraceId:" + traceId);

            String greeting = "Hello there, " + request.getName() + " your UserId is: " + userId + " and your Age is: " + request.getAge();

            HelloResponse response = HelloResponse.newBuilder().setGreeting(greeting).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
}
