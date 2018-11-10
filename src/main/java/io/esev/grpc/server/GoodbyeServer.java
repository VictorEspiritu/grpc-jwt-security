package io.esev.grpc.server;

import io.esev.grpc.GoodbyeRequest;
import io.esev.grpc.GoodbyeResponse;
import io.esev.grpc.GoodbyeServiceGrpc;
import io.esev.grpc.GreetingServiceGrpc;
import io.esev.grpc.HelloRequest;
import io.esev.grpc.HelloResponse;
import io.esev.grpc.commons.Constant;
import io.grpc.*;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class GoodbyeServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        System.out.println("[SERVER] Server Goodbye Starting!");
        //Connect to Greeting Server
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 9191)
                                        .usePlaintext(true)
                                        .intercept(new JwtClientInterceptor())
                                        .intercept(new TraceIdClientInterceptor())
                                        .build();

        GreetingServiceGrpc.GreetingServiceBlockingStub greetingStub = GreetingServiceGrpc.newBlockingStub(channel);


        //Expose a Server
        Server goodByeServer = ServerBuilder
                                    .forPort(9292)
                                    .addService(ServerInterceptors.intercept(new GoodbyeServiceImpl(greetingStub), new JwtServerInterceptor(Constant.JWT_SECRET), new TraceIdServerInterceptor()))
                                    .build();
        goodByeServer.start();
        System.out.println("[SERVER] Server Goodbye Started!");

        goodByeServer.awaitTermination();
    }

    public static class GreetingServiceImpl extends GreetingServiceGrpc.GreetingServiceImplBase {

        private final ManagedChannel channel;
        private final GoodbyeServiceGrpc.GoodbyeServiceBlockingStub goodByeStub;


        public GreetingServiceImpl(ManagedChannel channel) {
            this.channel = channel;
            this.goodByeStub = GoodbyeServiceGrpc.newBlockingStub(channel);
        }

        @Override
        public void greeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {
            System.out.println("[SERVICE] Greeting2 Service Starting!");
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

        @Override
        public void secondGreeting(HelloRequest request, StreamObserver<HelloResponse> responseObserver) {

            System.out.println("[SERVICE] Second Greeting Service Starting!");
            System.out.println(request);

            GoodbyeResponse response = goodByeStub.goodbye(GoodbyeRequest.newBuilder().setName(request.getName()).build());
            System.out.println("[SERVICE] Say GoodBye");
            System.out.println(response);

            greeting(request, responseObserver);
        }
    }

    public  static class GoodbyeServiceImpl extends GoodbyeServiceGrpc.GoodbyeServiceImplBase {

        private final GreetingServiceGrpc.GreetingServiceBlockingStub greetingStub;


        public GoodbyeServiceImpl(GreetingServiceGrpc.GreetingServiceBlockingStub greetingStub) {
            this.greetingStub = greetingStub;
        }

        @Override
        public void goodbye(GoodbyeRequest request, StreamObserver<GoodbyeResponse> responseObserver) {

            System.out.println("[SERVICE] GoodBye Service Starting!");
            System.out.println(request);

            String userId = Constant.USER_ID_CTX_KEY.get();
            String traceId = Constant.TRACE_ID_CTX_KEY.get();

            System.out.println("[SERVICE] UserId:" + userId);
            System.out.println("[SERVICE] TraceId:" + traceId);

            System.out.println("[SERVICE] Use GreetingService from GoodbyeService");
            HelloResponse response = this.greetingStub.greeting(HelloRequest.newBuilder().setName(request.getName()).setAge(20L).build());
            System.out.println("[SERVICE] Response");
            System.out.println(response);

            String farewell= "Goodbye, " + request.getName() + " your UserId is: " + userId;

            responseObserver.onNext(GoodbyeResponse.newBuilder().setFarewell(farewell).build());
            responseObserver.onCompleted();

        }
    }
}
