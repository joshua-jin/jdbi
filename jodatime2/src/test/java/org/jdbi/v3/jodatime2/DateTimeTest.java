/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jdbi.v3.jodatime2;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.rule.H2DatabaseRule;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DateTimeTest {
    @Rule
    public H2DatabaseRule dbRule = new H2DatabaseRule().withPlugin(new JodaTimePlugin());

    @Test
    public void dateTime() {
        Handle h = dbRule.getSharedHandle();
        h.execute("create table stuff(ts timestamp)");

        DateTime dt = new DateTime();
        h.execute("insert into stuff(ts) values (?)", dt);

        assertThat(h.createQuery("select ts from stuff").mapTo(DateTime.class).one()).isEqualTo(dt);
    }
}
