package com.yiran.agent.web;

import com.yiran.LoadBalance;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.util.concurrent.TimeUnit;

public class HttpChannelInitService extends ChannelInitializer<SocketChannel> {
    private LoadBalance loadBalance;

    public HttpChannelInitService (LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    protected void initChannel(SocketChannel sc)
            throws Exception {

        sc.pipeline().addLast(new HttpResponseEncoder());

        sc.pipeline().addLast(new HttpAdvanceRequestDecoder());

        sc.pipeline().addLast(new HttpChannelHandler(loadBalance));
    }

}
