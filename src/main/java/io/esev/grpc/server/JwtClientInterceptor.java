package io.esev.grpc.server;

import io.esev.grpc.commons.Constant;
import io.grpc.*;

public class JwtClientInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> methodDescriptor, CallOptions callOptions, Channel channel) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(channel.newCall(methodDescriptor, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                System.out.println("[INTERCEPTOR] JWT Header");

                headers.put(Constant.JWT_MD_KEY, Constant.JWT_CTX_KEY.get());
                super.start(responseListener, headers);
            }
        };
    }
}
