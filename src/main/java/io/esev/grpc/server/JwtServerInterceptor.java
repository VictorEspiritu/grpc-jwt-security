package io.esev.grpc.server;

import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.JWTVerifyException;
import io.esev.grpc.commons.Constant;
import io.grpc.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Map;

public class JwtServerInterceptor implements ServerInterceptor {

    private final String secret;
    private final JWTVerifier verifier;

    public JwtServerInterceptor(String secret) {
        this.secret = secret;
        this.verifier = new JWTVerifier(secret);
    }

    private static final ServerCall.Listener NOOP_LISTENER = new ServerCall.Listener() {
    };

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        String jwt = metadata.get(Constant.JWT_MD_KEY);
        if(jwt == null) {
            System.out.println("[INTERCEPTOR] JWT Null");
            serverCall.close(Status.UNAUTHENTICATED.withDescription("JWT Token is missing from Metadata"), metadata);
            return NOOP_LISTENER;
        }
        Context ctx;

        try {
            System.out.println("[INTERCEPTOR] Verified JWT Token");
            Map<String, Object> verified = verifier.verify(jwt);

            System.out.println("[INTERCEPTOR] Inject Context  JWT");
            ctx = Context.current()
                        .withValue(Constant.USER_ID_CTX_KEY, verified.getOrDefault("sub", "anonymous").toString())
                        .withValue(Constant.JWT_CTX_KEY, jwt);

        } catch (NoSuchAlgorithmException | InvalidKeyException | IOException | SignatureException | JWTVerifyException e) {
            System.out.println("[INTERCEPTOR] Verification Failed JWT - Unauthenticated!");
            serverCall.close(Status.UNAUTHENTICATED.withDescription(e.getMessage()).withCause(e), metadata);
            return NOOP_LISTENER;
        }

        return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
    }

}
