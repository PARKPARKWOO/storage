package org.woo.storage.adapter.out.grpc

import io.grpc.*
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.woo.event.api.InternalApiCallEvent

@Component
class GrpcClientMetricsInterceptor(
    private val kafkaTemplate: KafkaTemplate<String, InternalApiCallEvent>,
) : ClientInterceptor {

    override fun <ReqT, RespT> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel
    ): ClientCall<ReqT, RespT> {
        val startNanos = System.nanoTime()
        val serviceName = method.bareMethodName
        val call = next.newCall(method, callOptions)
        return object : ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(call) {

            override fun start(listener: Listener<RespT>, headers: Metadata) {
                super.start(object : SimpleForwardingClientCallListener<RespT>(listener) {
                    override fun onClose(status: Status, trailers: Metadata) {
                        val durationMs = (System.nanoTime() - startNanos) / 1_000_000
                        val event = InternalApiCallEvent(
                            serviceName = serviceName ?: method.fullMethodName,
                            statusCode   = status.code.value(),
                            timestamp    = System.currentTimeMillis(),
                            errorMessage = status.takeIf { !it.isOk }?.description,
                            durationMs   = durationMs
                        )
                        kafkaTemplate.send(event.topic, event)
                        super.onClose(status, trailers)
                    }
                }, headers)
            }
        }
    }
}