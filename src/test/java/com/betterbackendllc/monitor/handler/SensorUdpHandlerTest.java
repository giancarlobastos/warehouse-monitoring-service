package com.betterbackendllc.monitor.handler;

import com.betterbackendllc.monitor.domain.SensorDataType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SensorUdpHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext mockCtx;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatagramPacket mockPacket;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Channel mockChannel;

    @Test
    @SneakyThrows
    public void shouldPrependTemperatureTypeWhenTemperatureMessage() {
        String udpMessage = "sensor_id=12345;value=40";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(udpMessage);

        SensorUdpHandler handler = new SensorUdpHandler(SensorDataType.TEMPERATURE);
        Field channelField = SensorUdpHandler.class.getDeclaredField("centralMonitoringServiceChannel");
        channelField.setAccessible(true);
        channelField.set(handler, mockChannel);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockChannel).writeAndFlush(argThat((DatagramPacket packet) -> {
            String content = packet.content().toString(CharsetUtil.UTF_8);
            return content.contains("type=TEMPERATURE;sensor_id=12345;value=40");
        }));
    }

    @Test
    @SneakyThrows
    public void shouldPrependHumidityTypeWhenHumidityMessage() {
        String udpMessage = "sensor_id=12345;value=40";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(udpMessage);

        SensorUdpHandler handler = new SensorUdpHandler(SensorDataType.HUMIDITY);
        Field channelField = SensorUdpHandler.class.getDeclaredField("centralMonitoringServiceChannel");
        channelField.setAccessible(true);
        channelField.set(handler, mockChannel);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockChannel).writeAndFlush(argThat((DatagramPacket packet) -> {
            String content = packet.content().toString(CharsetUtil.UTF_8);
            return content.contains("type=HUMIDITY;sensor_id=12345;value=40");
        }));
    }
}