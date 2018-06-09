package com.yiran.agent.web;

import com.yiran.LoadBalance;
import com.yiran.agent.AgentClient;
import com.yiran.agent.AgentClientManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Map;

public class HttpChannelHandler extends SimpleChannelInboundHandler<Object> {
    private static Logger logger = LoggerFactory.getLogger(HttpChannelHandler.class);

    private LoadBalance loadBalance;
    private ByteBuf contentBuf = PooledByteBufAllocator.DEFAULT.buffer(2048);
    private ByteBuf parseTempBuf = PooledByteBufAllocator.DEFAULT.buffer(2048);
    private int contentLength = 0;

    private HttpRequest request = null;

    public HttpChannelHandler (LoadBalance loadBalance) {
        this.loadBalance = loadBalance;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if (msg instanceof HttpRequest) {
            contentLength = HttpUtil.getContentLength((HttpMessage) msg, 0);
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            if (buf.isReadable()) {
                contentBuf.writeBytes(buf);
            }
            if (msg instanceof LastHttpContent) {
                try{
                    FormDataParser formDataParser = new FormDataParser(parseTempBuf, 2048);
                    String serviceName = formDataParser.parseInterface(contentBuf);
                    if(serviceName == null) {
                        logger.error("Failed to parse form data!{}.", contentBuf.toString(Charset.forName("utf-8")));
                        ctx.close();
                        return;
                    }
                    /*开始调用服务*/

                    //logger.info("serviceName:{}", serviceName);

                    /*选出最优客户端*/
                    AgentClient agentClient = loadBalance.findOptimalAgentClient(serviceName);
                    /*调用服务*/
                    agentClient.request(ctx.channel(), contentBuf);
                    parseTempBuf.release();
                    //formDataParser.release();
                    //contentBuf.release();

                } catch (Exception e) {
                    logger.error("", e);
                }
            }
        }
    }
    /**
     * 设置HTTP返回头信息
     */
    private void setHeaders(FullHttpResponse response) {
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
//        if (HttpUtil.isKeepAlive(request)) {
//            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
//        }
    }


    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();//刷新后才将数据发出到SocketChannel
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
