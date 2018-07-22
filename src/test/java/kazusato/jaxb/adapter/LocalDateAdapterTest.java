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

import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeParseException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LocalDateAdapterTest {

    @Test
    public void testUnmarshalNormal() {
        testUnmarshal("2021-07-21", 2021, Month.JULY, 21);
        testUnmarshal("1997-07-21", 1997, Month.JULY, 21);
        testUnmarshal("0997-07-21", 997, Month.JULY, 21);
    }

    @Test
    public void testUnmarshalIllegal() {
        // with time
        testUnmarshalException("2021-07-21T09:00", DateTimeParseException.class, null);
        // divided by slash
        testUnmarshalException("2021/07/21", DateTimeParseException.class, null);
        // no day
        testUnmarshalException("2021-07", DateTimeParseException.class, null);
        // omit zero in month
        testUnmarshalException("2021-7-21", DateTimeParseException.class, null);
        // omit zero in day
        testUnmarshalException("2021-07-1", DateTimeParseException.class, null);
        // three digit year
        testUnmarshalException("997-07-21", DateTimeParseException.class, null);
        // illegal month
        testUnmarshalException("2021-13-21", DateTimeParseException.class, null);
        // illegal day
        testUnmarshalException("2021-02-29", DateTimeParseException.class, null);
        // illegal character in year
        testUnmarshalException("202A-02-28", DateTimeParseException.class, null);
    }

    @Test
    public void testUnmarshalNullEmpty() {
        testUnmarshalException(null, NullPointerException.class, null);
        testUnmarshalException("", DateTimeParseException.class, null);
    }

    @Test
    public void testMarshalNormal() {
        testMarshal(LocalDate.of(2021, 7, 5), "2021-07-05");
        testMarshal(LocalDate.of(997, 7, 5), "0997-07-05");
    }

    @Test
    public void testMarshalNull() {
        String converted = new LocalDateAdapter().marshal(null);
        assertThat(converted).isNull();
    }

    private void testUnmarshal(String from, int year, Month month, int dayOfMonth) {
        LocalDate created = new LocalDateAdapter().unmarshal(from);
        assertThat(created).isNotNull();
        assertThat(created.getYear()).isEqualTo(year);
        assertThat(created.getMonth()).isEqualTo(month);
        assertThat(created.getDayOfMonth()).isEqualTo(dayOfMonth);
    }

    private void testUnmarshalException(String from, Class<? extends Exception> clazz, String partialMessage) {
        assertThatThrownBy(() -> {
            LocalDate created = new LocalDateAdapter().unmarshal(from);
        }).isInstanceOf(clazz).hasMessageContaining(partialMessage != null ? partialMessage : "" );
    }

    private void testMarshal(LocalDate localDate, String expectedStr) {
        String converted = new LocalDateAdapter().marshal(localDate);
        assertThat(converted).isEqualTo(expectedStr);
    }

}
