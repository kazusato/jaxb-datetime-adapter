/*
 * Copyright 2018 Kazuhiko Sato
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kazusato.jaxb.adapter;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocalTimeAdapterTest {

    @Test
    public void testUnmarshalNormal() {
        testUnmarshal("09:00", 9, 0, 0, 0);
        testUnmarshal("09:05:03", 9, 5, 3, 0);
        testUnmarshal("09:05:03.9", 9, 5, 3, 900_000_000);
        testUnmarshal("09:05:03.987", 9, 5, 3, 987_000_000);
        testUnmarshal("09:05:03.987654321", 9, 5, 3, 987_654_321);

    }

    @Test
    public void testUnmarshalIllegal() {
        // with "T"
        testUnmarshalException("T09:00", DateTimeParseException.class, null);
        // with date
        testUnmarshalException("2021-07-05T02:45:37", DateTimeParseException.class, null);
        // hour only
        testUnmarshalException("09", DateTimeParseException.class, null);
        // ten digit millis
        testUnmarshalException("09:05:03.9876543212",  DateTimeParseException.class, null);
        // with offset
        testUnmarshalException("09:00+08:00", DateTimeParseException.class, null);
        // illegal hour
        testUnmarshalException("24:00", DateTimeParseException.class, null);
        // illegal minute
        testUnmarshalException("23:60", DateTimeParseException.class, null);
        // illegal second - Java API cannot handle a leap second correctly.
        testUnmarshalException("23:59.60", DateTimeParseException.class, null);
        // illegal character in hour
        testUnmarshalException("2A:59", DateTimeParseException.class, null);
    }

    @Test
    public void testUnmarshalNullEmpty() {
        testUnmarshalException(null, NullPointerException.class, null);
        testUnmarshalException("", DateTimeParseException.class, null);
    }

    @Test
    public void testMarshalNormal() {
        testMarshal(LocalTime.of(7, 1), "07:01");
        testMarshal(LocalTime.of(23, 1, 5), "23:01:05");
        testMarshal(LocalTime.of(7, 1, 5, 987_000_000), "07:01:05.987");
        testMarshal(LocalTime.of(7, 1, 5, 987_600_000), "07:01:05.987600");
        testMarshal(LocalTime.of(7, 1, 5, 987_654_300), "07:01:05.987654300");
    }

    @Test
    public void testMarshalNull() {
        String converted = new LocalTimeAdapter().marshal(null);
        assertThat(converted).isNull();
    }

    private void testUnmarshal(String from, int hour, int minute, int second, int nano) {
        LocalTime created = new LocalTimeAdapter().unmarshal(from);
        assertThat(created).isNotNull();
        assertThat(created.getHour()).isEqualTo(hour);
        assertThat(created.getMinute()).isEqualTo(minute);
        assertThat(created.getSecond()).isEqualTo(second);
        assertThat(created.getNano()).isEqualTo(nano);
    }

    private void testUnmarshalException(String from, Class<? extends Exception> clazz, String partialMessage) {
        assertThatThrownBy(() -> {
            LocalTime created = new LocalTimeAdapter().unmarshal(from);
        }).isInstanceOf(clazz).hasMessageContaining(partialMessage != null ? partialMessage : "" );
    }

    private void testMarshal(LocalTime localTime, String expectedStr) {
        String converted = new LocalTimeAdapter().marshal(localTime);
        assertThat(converted).isEqualTo(expectedStr);
    }

}
