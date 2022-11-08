package com.krickert.search.model.util;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Test;

import java.sql.Time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
class ProtobufUtilsTest {

    @Test
    void now() throws InterruptedException {
        Timestamp now = ProtobufUtils.now();
        assertThat(now).isInstanceOf(Timestamp.class);
        Thread.sleep(100);
        assertThat(ProtobufUtils.now().getSeconds() > now.getSeconds()).isTrue();
    }

    @Test
    void stamp() {
        long time = System.currentTimeMillis() / 1000;
        Timestamp stamp = ProtobufUtils.stamp(time);
        assertThat(stamp.getSeconds()).isEqualTo(time);
        assertThat(stamp.getNanos()).isEqualTo(0);
    }
}