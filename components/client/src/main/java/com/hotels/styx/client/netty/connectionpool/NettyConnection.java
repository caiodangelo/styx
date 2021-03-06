/**
 * Copyright (C) 2013-2017 Expedia Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hotels.styx.client.netty.connectionpool;

import com.eaio.uuid.UUID;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.net.HostAndPort;
import com.hotels.styx.api.Announcer;
import com.hotels.styx.api.client.Connection;
import com.hotels.styx.api.client.Origin;
import com.hotels.styx.api.netty.exceptions.TransportException;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import rx.Observable;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A connection using a netty channel.
 */
public class NettyConnection implements Connection, TimeToFirstByteListener {
    public static final AttributeKey<Object> CLOSED_BY_STYX = AttributeKey.newInstance("CLOSED_BY_STYX");

    private final Origin origin;
    private final Object id;
    private final Channel channel;

    private volatile TransportException lastException;
    private volatile long timeToFirstByteMs;
    private final Announcer<Listener> listeners = Announcer.to(Listener.class);

    /**
     * Constructs an instance with an arbitrary UUID.
     *
     * @param origin  the origin connected to
     * @param channel the netty channel used
     */
    @VisibleForTesting
    public NettyConnection(Origin origin, Channel channel) {
        this(new UUID(), origin, channel);
    }

    /**
     * Constructs an instance.
     *
     * @param id      an object to use as an ID
     * @param origin  the origin connected to
     * @param channel the netty channel used
     */
    public NettyConnection(Object id, Origin origin, Channel channel) {
        this.id = id;
        this.origin = checkNotNull(origin);
        this.channel = checkNotNull(channel);
        this.channel.pipeline().addLast(new TimeToFirstByteHandler(this));
        this.channel.closeFuture().addListener(future ->
                listeners.announce().connectionClosed(NettyConnection.this));
    }

    @Override
    public <R> Observable<R> execute(Supplier<Observable<R>> operation) {
        return operation.get()
                .doOnError(throwable -> {
                    if (throwable instanceof TransportException) {
                        lastException = (TransportException) throwable;
                    }
                });
    }

    /**
     * The netty channel associated with this connection.
     *
     * @return netty channel
     */
    public Channel channel() {
        return channel;
    }

    private HostAndPort getHost() {
        return this.origin.host();
    }

    @Override
    public boolean isConnected() {
        return channel.isActive();
    }

    @Override
    public Origin getOrigin() {
        return this.origin;
    }

    @Override
    public long getTimeToFirstByteMillis() {
        return this.timeToFirstByteMs;
    }

    @Override
    public void addConnectionListener(Listener listener) {
        this.listeners.addListener(listener);
    }

    @Override
    public void close() {
        if (channel.isOpen()) {
            channel.attr(CLOSED_BY_STYX).set(true);
            channel.close();
        }
    }

    @Override
    public void notifyTimeToFirstByte(long timeToFirstByte, TimeUnit timeUnit) {
        this.timeToFirstByteMs = timeUnit.toMillis(timeToFirstByte);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        NettyConnection other = (NettyConnection) obj;
        return Objects.equal(this.id, other.id);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("host", getHost())
                .add("channel", toString(channel))
                .toString();
    }

    private static String toString(Channel channel) {
        return toStringHelper(channel)
                .add("active", channel.isActive())
                .add("open", channel.isOpen())
                .add("registered", channel.isRegistered())
                .add("writable", channel.isWritable())
                .add("localAddress", channel.localAddress())
                .add("clientAddress", channel.remoteAddress())
                .add("hashCode", channel.hashCode())
                .toString();
    }
}
