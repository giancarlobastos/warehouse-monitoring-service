package com.betterbackendllc.monitor.handler;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.betterbackendllc.monitor.domain.SensorDataType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MonitoringUdpHandlerTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext mockCtx;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DatagramPacket mockPacket;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Appender<ILoggingEvent> mockAppender;

    private MonitoringUdpHandler handler;

    @BeforeEach
    void beforeEach() {
        handler = new MonitoringUdpHandler(Map.of(
                SensorDataType.TEMPERATURE, 35,
                SensorDataType.HUMIDITY, 50));

        Logger spyLogger = (Logger) LoggerFactory.getLogger(MonitoringUdpHandler.class);
        spyLogger.addAppender(mockAppender);
    }

    @Test
    public void shouldLogTemperatureAlarmWhenValueIsBiggerThanThreshold() {
        String message = "type=TEMPERATURE;sensor_id=12345;value=36";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(message);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockAppender).doAppend(argThat(event ->
                event.getFormattedMessage().contains("ALARM - TEMPERATURE, sensorId = 12345, value = 36")
        ));
    }

    @Test
    public void shouldNotLogTemperatureAlarmWhenValueIsLessThanOrEqualThreshold() {
        String message = "type=TEMPERATURE;sensor_id=12345;value=35";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(message);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockAppender, never()).doAppend(any());
    }

    @Test
    public void shouldLogHumidityAlarmWhenValueIsBiggerThanThreshold() {
        String message = "type=HUMIDITY;sensor_id=12345;value=51";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(message);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockAppender).doAppend(argThat(event ->
                event.getFormattedMessage().contains("ALARM - HUMIDITY, sensorId = 12345, value = 51")
        ));
    }

    @Test
    public void shouldNotLogHumidityAlarmWhenValueIsLessThanOrEqualThreshold() {
        String message = "type=HUMIDITY;sensor_id=12345;value=50";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(message);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockAppender, never()).doAppend(any());
    }

    @Test
    void shouldLogInvalidDataWhenMessageIsInvalid() {
        String message = "INVALID DATA";
        when(mockPacket.content().toString(CharsetUtil.UTF_8)).thenReturn(message);

        handler.channelRead0(mockCtx, mockPacket);

        verify(mockAppender).doAppend(argThat(event ->
                event.getFormattedMessage().contains("Invalid sensor data: INVALID DATA")
        ));
    }
}