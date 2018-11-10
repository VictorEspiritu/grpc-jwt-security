package io.esev.grpc.commons;

import io.grpc.Context;
import io.grpc.Metadata;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

public class Constant {

    public static final String JWT_SECRET = "abcdef1135813";

    public static final Context.Key<String> USER_ID_CTX_KEY = Context.key("userId");
    public static final Context.Key<String> JWT_CTX_KEY = Context.key("jwt");
    public static final Context.Key<String> TRACE_ID_CTX_KEY = Context.key("traceId");
    public static final Context.Key<String> CLIENT_ID_KEY = Context.key("clientId");

    public static final Metadata.Key<String> CLIENT_ID_MD_KEY = Metadata.Key.of("client-id", ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> JWT_MD_KEY = Metadata.Key.of("jwt", ASCII_STRING_MARSHALLER);
    public static final Metadata.Key<String> TRACE_ID_MD_KEY = Metadata.Key.of("traceId", ASCII_STRING_MARSHALLER);

}
