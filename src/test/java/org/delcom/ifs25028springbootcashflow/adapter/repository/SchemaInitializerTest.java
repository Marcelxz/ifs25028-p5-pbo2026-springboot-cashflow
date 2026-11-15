package org.delcom.ifs25028springbootcashflow.adapter.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import org.delcom.ifs25028springbootcashflow.support.TestDb;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

class SchemaInitializerTest {

    @Test
    void membuatTabelDanBolehDiulang() throws Exception {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");

        SchemaInitializer.initialize(ds);
        SchemaInitializer.initialize(ds); // CREATE TABLE IF NOT EXISTS: aman diulang

        try (Connection c = ds.getConnection(); Statement st = c.createStatement()) {
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
            try (ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM transactions")) {
                assertTrue(rs.next());
                assertEquals(0, rs.getInt(1));
            }
        }
    }

    @Test
    void gagalBilaKoneksiRusak() {
        assertThrows(IllegalStateException.class, () -> SchemaInitializer.initialize(TestDb.brokenDataSource()));
    }
}