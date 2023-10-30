package com.krickert.search.grpc;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.NettyChannelBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.type.Argument;
import io.micronaut.grpc.channels.GrpcManagedChannelFactory;
import io.micronaut.grpc.channels.GrpcNamedManagedChannelConfiguration;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.kafka.streams.state.RocksDBConfigSetter.LOG;

@Singleton
@Requires(notEnv = Environment.TEST)
public class ConsulGrpcManagedChannelFactory implements AutoCloseable {
    GrpcManagedChannelFactory factory;
    private final ApplicationContext beanContext;
    private final Map<ChannelKey, ManagedChannel> channels = new ConcurrentHashMap<>();


    @Inject
    public ConsulGrpcManagedChannelFactory(ApplicationContext beanContext) {
        this.beanContext = beanContext;
    }

    public ManagedChannel managedChannelFromConsul(String serviceName) {
        Argument<String> argument = Argument.STRING;
        return channels.computeIfAbsent(new ChannelKey(argument, serviceName), channelKey -> {
            final NettyChannelBuilder nettyChannelBuilder = beanContext.createBean(NettyChannelBuilder.class, serviceName);
            ManagedChannel channel = nettyChannelBuilder.build();
            beanContext.findBean(GrpcNamedManagedChannelConfiguration.class, Qualifiers.byName(serviceName))
                    .ifPresent(channelConfig -> {
                        if (channelConfig.isConnectOnStartup()) {
                            LOG.debug("Connecting to the channel: {}", serviceName);
                            if (!connectOnStartup(channel, channelConfig.getConnectionTimeout())) {
                                throw new IllegalStateException("Unable to connect to the channel: " + serviceName);
                            }
                            LOG.debug("Successfully connected to the channel: {}", serviceName);
                        }
                    });
            return channel;
        });
    }

    /**
     * Client key.
     */
    private static final class ChannelKey {

        final Argument<?> identifier;
        final String value;

        public ChannelKey(Argument<?> identifier, String value) {
            this.identifier = identifier;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ChannelKey clientKey = (ChannelKey) o;
            return Objects.equals(identifier, clientKey.identifier) &&
                    Objects.equals(value, clientKey.value);
        }
    }
    private boolean connectOnStartup(ManagedChannel channel, Duration timeout) {
        channel.getState(true); // request connection
        final CountDownLatch readyLatch = new CountDownLatch(1);
        waitForReady(channel, readyLatch);
        try {
            return readyLatch.await(timeout.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private void waitForReady(ManagedChannel channel, CountDownLatch readyLatch) {
        final ConnectivityState state = channel.getState(false);
        if (state == ConnectivityState.READY) {
            readyLatch.countDown();
        } else {
            channel.notifyWhenStateChanged(state, () -> waitForReady(channel, readyLatch));
        }
    }


    @Override
    @PreDestroy
    public void close() {
        for (ManagedChannel channel : channels.values()) {
            if (!channel.isShutdown()) {
                try {
                    channel.shutdown().awaitTermination(1, TimeUnit.SECONDS);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Error shutting down GRPC channel: {}", e.getMessage(), e);
                    }
                }
            }
        }
        channels.clear();
    }

}
