package com.betterbackendllc.monitor.service;

import com.betterbackendllc.monitor.domain.SensorDataType;
import com.betterbackendllc.monitor.handler.SensorUdpHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WarehouseService {

    public static final Logger LOGGER = LoggerFactory.getLogger(WarehouseService.class);

    public static final int TEMPERATURE_SENSOR_PORT = 3344;
    public static final int HUMIDITY_SENSOR_PORT = 3355;

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap temperatureBootstrap = new Bootstrap();
            ChannelFuture temperatureChannelFuture = temperatureBootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SensorUdpHandler(SensorDataType.TEMPERATURE))
                    .bind(TEMPERATURE_SENSOR_PORT)
                    .sync();

            Bootstrap humidityBootstrap = new Bootstrap();
            ChannelFuture humidityChannelFuture = humidityBootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new SensorUdpHandler(SensorDataType.HUMIDITY))
                    .bind(HUMIDITY_SENSOR_PORT)
                    .sync();

            LOGGER.info("WarehouseService started!");

            temperatureChannelFuture
                    .channel()
                    .closeFuture()
                    .await();

            humidityChannelFuture
                    .channel()
                    .closeFuture()
                    .await();
        } catch (Exception e) {
            LOGGER.error("Error starting WarehouseService", e);
        } finally {
            group.shutdownGracefully();
        }
    }
}