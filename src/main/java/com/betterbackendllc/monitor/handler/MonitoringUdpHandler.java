package com.betterbackendllc.monitor.handler;

import com.betterbackendllc.monitor.domain.SensorDataType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.join;

@RequiredArgsConstructor
public class MonitoringUdpHandler extends SimpleChannelInboundHandler<DatagramPacket> {
    public static final Logger LOGGER = LoggerFactory.getLogger(MonitoringUdpHandler.class);

    private static final Pattern SENSOR_PATTERN =
            Pattern.compile(format("type=(%s);sensor_id=(\\w+);value=(\\d+)", join(SensorDataType.values(), "|")));

    private final Map<SensorDataType, Integer> thresholds;

    @Override
    @SneakyThrows
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket packet) {
        String message = packet.content().toString(CharsetUtil.UTF_8).trim();
        LOGGER.debug(message);

        handleMessage(message);
    }

    private void handleMessage(String message) {
        Matcher matcher = SENSOR_PATTERN.matcher(message);

        if (matcher.matches()) {
            SensorDataType type = SensorDataType.valueOf(matcher.group(1));
            String sensorId = matcher.group(2);
            int value = Integer.parseInt(matcher.group(3));

            if (value > thresholds.getOrDefault(type, 0)) {
                LOGGER.warn("ALARM - {}, sensorId = {}, value = {}", type, sensorId, value);
            }
        } else {
            LOGGER.warn("Invalid sensor data: {}", message);
        }
    }
}
