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
package org.jdbi.v3.vavr;

import io.vavr.collection.Set;
import io.vavr.control.Option;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.generic.GenericType;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.core.rule.H2DatabaseRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

public class TestVavrOptionMapperWithDB {

    @Rule
    public H2DatabaseRule dbRule = new H2DatabaseRule().withPlugins();

    @Before
    public void addData() {
        Handle handle = dbRule.openHandle();
        handle.createUpdate("insert into something (id, name) values (1, 'eric')").execute();
        handle.createUpdate("insert into something (id) values (2)").execute();
    }

    @Test
    public void testOptionMapped_shouldSucceed() throws SQLException {
        final Set<Option<String>> result = dbRule.getSharedHandle()
                .registerRowMapper(ConstructorMapper.factory(SomethingWithOption.class))
                .createQuery("select name from something")
                .collectInto(new GenericType<Set<Option<String>>>() {});

        assertThat(result).hasSize(2);
        assertThat(result).contains(Option.none(), Option.of("eric"));
    }

    @Test
    public void testOptionMappedWithinObjectIfPresent_shouldContainValue() throws SQLException {
        final SomethingWithOption result = dbRule.getSharedHandle()
                .registerRowMapper(ConstructorMapper.factory(SomethingWithOption.class))
                .createQuery("select id, name from something where id = 1")
                .mapTo(SomethingWithOption.class)
                .findOnly();

        assertThat(result.getName()).isInstanceOf(Option.class);
        assertThat(result.getName().get()).isEqualToIgnoringCase("eric");
    }

    @Test
    public void testOptionWithinObjectIfMissing_shouldBeNone() throws SQLException {
        final SomethingWithOption result = dbRule.getSharedHandle()
                .registerRowMapper(ConstructorMapper.factory(SomethingWithOption.class))
                .createQuery("select id, name from something where id = 2")
                .mapTo(SomethingWithOption.class)
                .findOnly();

        assertThat(result.getName()).isInstanceOf(Option.class);
        assertThat(result.getName().getOrNull()).isNull();
    }

    public static class SomethingWithOption {
        private int id;
        private Option<String> name;

        public SomethingWithOption(int id, Option<String> name) {
            this.id = id;
            this.name = name;
        }

        public Option<String> getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            SomethingWithOption that = (SomethingWithOption) o;

            if (id != that.id) return false;
            return name != null ? name.equals(that.name) : that.name == null;
        }

        @Override
        public int hashCode() {
            int result = id;
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }


}