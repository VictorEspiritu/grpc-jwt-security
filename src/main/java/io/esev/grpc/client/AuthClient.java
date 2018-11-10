package io.esev.grpc.client;

import com.auth0.jwt.JWTSigner;
import io.esev.grpc.*;
import io.esev.grpc.GoodbyeRequest;
import io.esev.grpc.GoodbyeResponse;
import io.esev.grpc.GoodbyeServiceGrpc;
import io.esev.grpc.GreetingServiceGrpc;
import io.esev.grpc.HelloRequest;
import io.esev.grpc.HelloResponse;
import io.esev.grpc.Sentiment;
import io.esev.grpc.commons.Constant;
import io.esev.grpc.server.TraceIdClientInterceptor;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.HashMap;

public class AuthClient {

    public static void main(String[] args) {
        System.out.println("[CLIENT] AuthClient Starting..!");
        String jwt = createJwt(Constant.JWT_SECRET, "authClient", "esev26");

        System.out.println("[CLIENT] JWT Created");
        System.out.println(jwt);

        JwtCallCredentials jwtCallCredentials = new JwtCallCredentials(jwt);

        System.out.println("[CLIENT] Create Channel to Greeting Server");
        ManagedChannel greetingChannel = ManagedChannelBuilder
                                                .forAddress("localhost", 9191)
                                                .usePlaintext(true)
                                                .intercept(new TraceIdClientInterceptor())
                                                .build();

        System.out.println("[CLIENT] Create Channel to GoodBye Server");
        ManagedChannel goodbyeChannel = ManagedChannelBuilder
                                                .forAddress("localhost", 9292)
                                                .usePlaintext(true)
                                                .intercept(new TraceIdClientInterceptor())
                                                .build();

        System.out.println("[CLIENT] Create Stub to Greeting Server");
        Context.current().withValue(Constant.TRACE_ID_CTX_KEY, "1").run(() -> {
            GreetingServiceGrpc.GreetingServiceBlockingStub greetingStub = GreetingServiceGrpc.newBlockingStub(greetingChannel).withCallCredentials(jwtCallCredentials);
            System.out.println("[CLIENT] Response of Execute Method Remote Greeting");

            HelloResponse helloResponse = greetingStub.greeting(HelloRequest.newBuilder().setName("Victor").setAge(28L).setSentiment(Sentiment.HAPPY).build());
            System.out.println("[CLIENT] Response of Greeting Server");
            System.out.println(helloResponse);
        });

        System.out.println("[CLIENT] Create Stub to Goodbye Server");
        Context.current().withValue(Constant.TRACE_ID_CTX_KEY, "2").run(() -> {
            GoodbyeServiceGrpc.GoodbyeServiceBlockingStub goodByeStub = GoodbyeServiceGrpc.newBlockingStub(goodbyeChannel).withCallCredentials(jwtCallCredentials);
            GoodbyeResponse goodbyeResponse = goodByeStub.goodbye(GoodbyeRequest.newBuilder().setName("Emeric").setReason("Because I am sleepy...!").build());
            System.out.println("[CLIENT] Response of GoodBye Server");
            System.out.println(goodbyeResponse);
        });

    }

    private static String createJwt(String secret, String issuer, String subject) {

        final long iat = System.currentTimeMillis() / 500;
        final long exp = iat + 60L;

        final JWTSigner signer = new JWTSigner(secret);
        final HashMap<String, Object> claims = new HashMap<>();

        claims.put("iss", issuer);
        claims.put("exp", exp);
        claims.put("iat", iat);
        claims.put("sub", subject);

        return signer.sign(claims);
    }
}
