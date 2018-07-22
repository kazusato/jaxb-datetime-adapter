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

import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OffsetDateTimeAdapterTest {

    @Test
    public void testUnmarshalNormal() {
        // test is done with non-JST offset to avoid unintended success with implicit assumption of the +09:00 offset.
        // positive offset
        testUnmarshal("2021-07-05T02:45:37+08:00", 2021, Month.JULY, 5, 2, 45, 37, 0, "+08:00");
        // negative offset with minutes
        testUnmarshal("2021-07-05T02:45:37-04:30", 2021, Month.JULY, 5, 2, 45, 37, 0, "-04:30");
        // three digit millis
        testUnmarshal("2021-07-05T02:45:37.975+08:00", 2021, Month.JULY, 5, 2, 45, 37, 975_000_000, "+08:00");
        // two digit millis
        testUnmarshal("2021-07-05T02:45:37.25+08:00",  2021, Month.JULY, 5, 2, 45, 37, 250_000_000, "+08:00");
        // four digit millis
        testUnmarshal("2021-07-05T02:45:37.9876+08:00", 2021, Month.JULY, 5, 2, 45, 37, 987_600_000, "+08:00");
        // nine digit millis
        testUnmarshal("2021-07-05T02:45:37.987654321+08:00", 2021, Month.JULY, 5, 2, 45, 37, 987_654_321, "+08:00");
        // Zero offset with plus
        testUnmarshal("2021-07-05T02:45:37+00:00", 2021, Month.JULY, 5, 2, 45, 37, 0, "+00:00");
        // Zero offset with minus
        testUnmarshal("2021-07-05T02:45:37-00:00", 2021, Month.JULY, 5, 2, 45, 37, 0, "+00:00");
        // Zero offset with Z
        testUnmarshal("2021-07-05T02:45:37Z", 2021, Month.JULY, 5, 2, 45, 37, 0, "+00:00");
        // Year of 19xx
        testUnmarshal("1997-07-05T02:45:37Z", 1997, Month.JULY, 5, 2, 45, 37, 0, "+00:00");
        // Year before 1000
        testUnmarshal("0997-07-05T02:45:37Z", 997, Month.JULY, 5, 2, 45, 37, 0, "+00:00");
    }

    @Test
    public void testUnmarshalIllegal() {
        // no offset
        testUnmarshalException("2021-07-05T02:45:37", DateTimeParseException.class, null);
        // divided by slash
        testUnmarshalException("2021/07/05T02:45:37+08:00", DateTimeParseException.class, null);
        // omit zero in month
        testUnmarshalException("2021-7-05T02:45:37+08:00", DateTimeParseException.class, null);
        // omit zero in day
        testUnmarshalException("2021-07-5T02:45:37+08:00", DateTimeParseException.class, null);
        // omit zero in hour
        testUnmarshalException("2021-07-05T2:45:37+08:00", DateTimeParseException.class, null);
        // omit zero in minute
        testUnmarshalException("2021-07-05T02:5:37+08:00", DateTimeParseException.class, null);
        // omit zero in second
        testUnmarshalException("2021-07-05T02:05:7+08:00", DateTimeParseException.class, null);
        // ten digit millis
        testUnmarshalException("2021-07-05T02:45:37.9876543212+08:00", DateTimeParseException.class, null);
        // illegal offset format
        testUnmarshalException("2021-07-05T02:45:37+8:00", DateTimeParseException.class, null);
        // illegal character in year
        testUnmarshalException("20A2-07-05T02:45:37+08:00", DateTimeParseException.class, null);
        // illegal month
        testUnmarshalException("2021-13-05T02:45:37+08:00", DateTimeParseException.class, null);
        // illegal day
        testUnmarshalException("2021-02-30T02:45:37+08:00", DateTimeParseException.class, null);
        // illegal hour
        testUnmarshalException("2021-02-28T24:45:37+08:00", DateTimeParseException.class, null);
        // illegal minute
        testUnmarshalException("2021-02-28T02:60:37+08:00", DateTimeParseException.class, null);
        // illegal second - non leap second case
        testUnmarshalException("2000-02-28T02:59:60+08:00", DateTimeParseException.class, null);
        // leap second also throws exception - it is a "legal" time but Java API cannot handle it correctly.
        testUnmarshalException("2015-06-30T23:59:60.999Z", DateTimeParseException.class, null);
        // no "T"
        testUnmarshalException("2021-07-05 02:45:37+08:00", DateTimeParseException.class, null);
        // Three digit year
        testUnmarshalException("997-07-05T02:45:37Z", DateTimeParseException.class, null);
    }

    @Test
    public void testUnmarshalNullEmpty() {
        testUnmarshalException(null, NullPointerException.class, null);
        testUnmarshalException("", DateTimeParseException.class, null);
    }

    @Test
    public void testMarshalNormal() {
        // Zero sub-millis
        testMarshal(OffsetDateTime.of(2018, 7, 3, 9, 1, 3, 987_000_000, ZoneOffset.of("+08:00")),
                "2018-07-03T09:01:03.987+08:00");
        // Zero nano is converted to integer second
        testMarshal(OffsetDateTime.of(2018, 7, 3, 9, 1, 3, 0, ZoneOffset.of("+08:00")),
                "2018-07-03T09:01:03+08:00");
        // Six digit nano (3.0009 seconds is converted to "3.000900")
        testMarshal(OffsetDateTime.of(2018, 7, 3, 9, 1, 3, 900_000, ZoneOffset.of("+08:00")),
                "2018-07-03T09:01:03.000900+08:00");
        // Nine digit nano (3.0000009 seconds is converted to "3.000000900")
        testMarshal(OffsetDateTime.of(2018, 7, 3, 9, 1, 3, 900, ZoneOffset.of("+08:00")),
                "2018-07-03T09:01:03.000000900+08:00");
        // Year before 1000
        testMarshal(OffsetDateTime.of(997, 7, 3, 9, 1, 3, 0, ZoneOffset.of("+08:00")),
                "0997-07-03T09:01:03+08:00");
    }

    @Test
    public void testMarshalNull() {
        String converted = new OffsetDateTimeAdapter().marshal(null);
        assertThat(converted).isNull();
    }

    private void testUnmarshal(String from, int year, Month month, int dayOfMonth, int hour, int minute,
                               int second, int nano, String offset) {
        OffsetDateTime created = new OffsetDateTimeAdapter().unmarshal(from);
        assertThat(created).isNotNull();
        assertThat(created.getYear()).isEqualTo(year);
        assertThat(created.getMonth()).isEqualTo(month);
        assertThat(created.getDayOfMonth()).isEqualTo(dayOfMonth);
        assertThat(created.getHour()).isEqualTo(hour);
        assertThat(created.getMinute()).isEqualTo(minute);
        assertThat(created.getSecond()).isEqualTo(second);
        assertThat(created.getNano()).isEqualTo(nano);
        assertThat(created.getOffset()).isEqualTo(ZoneOffset.of(offset));
    }

    private void testUnmarshalException(String from, Class<? extends Exception> clazz, String partialMessage) {
        assertThatThrownBy(() -> {
            OffsetDateTime created = new OffsetDateTimeAdapter().unmarshal(from);
        }).isInstanceOf(clazz).hasMessageContaining(partialMessage != null ? partialMessage : "" );
    }

    private void testMarshal(OffsetDateTime offsetDateTime, String expectedStr) {
        String converted = new OffsetDateTimeAdapter().marshal(offsetDateTime);
        assertThat(converted).isEqualTo(expectedStr);
    }

}
