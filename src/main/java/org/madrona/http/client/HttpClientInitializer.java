package org.madrona.http.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpContentDecompressor;


public class HttpClientInitializer extends ChannelInitializer<SocketChannel> {



    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpContentDecompressor());
        pipeline.addLast(new HttpResponseHandler());

        /**
         --(ByteBuf)--> HttpRequestDecoder --(HttpObject)-->
         HttpObjectAggregator --(FullHttpRequest)--> RequestDecoder* --(Request*)-->
         FormPayloadDecoder* --(FullDecodedRequest*)--> DefaultHandler* --(Response*)-->
         DefaultExceptionHandler*
         */
    }

}