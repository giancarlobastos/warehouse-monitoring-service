package com.betterbackendllc.monitor.service;

import com.betterbackendllc.monitor.domain.SensorDataType;
import com.betterbackendllc.monitor.handler.MonitoringUdpHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CentralMonitoringService {

    public static final Logger LOGGER = LoggerFactory.getLogger(CentralMonitoringService.class);

    public static final int SERVICE_PORT = 4000;

    public static void main(String[] args) {
        System.setProperty("java.net.preferIPv4Stack", "true");
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            ChannelFuture channelFuture = bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new MonitoringUdpHandler(Map.of(
                            SensorDataType.TEMPERATURE, 35,
                            SensorDataType.HUMIDITY, 50)))
                    .bind(SERVICE_PORT)
                    .sync();

            LOGGER.info("CentralMonitoringService started!");

            channelFuture
                    .channel()
                    .closeFuture()
                    .await();

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        } finally {
            group.shutdownGracefully();
        }
    }
}
