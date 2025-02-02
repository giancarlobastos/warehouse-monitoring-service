package com.betterbackendllc.monitor.handler;

import com.betterbackendllc.monitor.domain.SensorDataType;
import com.betterbackendllc.monitor.service.CentralMonitoringService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;

public class SensorUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    public static final Logger LOGGER = LoggerFactory.getLogger(SensorUdpHandler.class);

    private static final String LOCALHOST = "127.0.0.1";
    private static final String DOCKER_HOST = "central_monitoring";

    private static final String MONITORING_HOST =
            System.getenv("DOCKER_ENV") != null ? DOCKER_HOST : LOCALHOST;

    private final SensorDataType dataType;
    private final Channel centralMonitoringServiceChannel;

    public SensorUdpHandler(SensorDataType dataType) {
        this.dataType = dataType;
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();

        centralMonitoringServiceChannel = bootstrap.group(group)
                .channel(NioDatagramChannel.class)
                .handler(new SimpleChannelInboundHandler<DatagramPacket>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket o) {
                    }
                }).bind(0)
                .addListener((ChannelFutureListener) listener -> {
                    if (!listener.isSuccess()) {
                        throw new RuntimeException(String.format("Error creating UDP channel to Central Monitoring Service for %s", dataType));
                    }
                }).channel();
    }

    @Override
    @SneakyThrows
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        String message = generateMessage(packet);
        LOGGER.debug(message);

        centralMonitoringServiceChannel.writeAndFlush(
                new DatagramPacket(Unpooled.copiedBuffer(message, CharsetUtil.UTF_8),
                        new InetSocketAddress(MONITORING_HOST, CentralMonitoringService.SERVICE_PORT)));
    }

    private String generateMessage(DatagramPacket packet) {
        return String.format("type=%s;%s", dataType, deleteWhitespace(packet.content().toString(CharsetUtil.UTF_8).trim()));
    }
}
