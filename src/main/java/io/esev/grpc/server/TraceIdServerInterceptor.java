package io.esev.grpc.server;

import io.esev.grpc.commons.Constant;
import io.grpc.*;


/**
 * Created by @Esev
 */
public class TraceIdServerInterceptor implements ServerInterceptor {
    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {

        System.out.println("[INTERCEPTOR] TraceId Inject");
        String traceId = metadata.get(Constant.TRACE_ID_MD_KEY);
        Context ctx = Context.current().withValue(Constant.TRACE_ID_CTX_KEY, traceId);

        return Contexts.interceptCall(ctx, serverCall, metadata, serverCallHandler);
    }
}
