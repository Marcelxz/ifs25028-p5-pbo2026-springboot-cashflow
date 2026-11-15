package org.delcom.ifs25028springbootcashflow.support;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.delcom.ifs25028springbootcashflow.adapter.repository.SchemaInitializer;
import org.h2.jdbcx.JdbcDataSource;

/** Pembantu tes: DataSource H2 baru dan DataSource yang selalu gagal. */
public final class TestDb {

    private TestDb() {
    }

    /** H2 in-memory dengan nama unik, skema users + transactions sudah dibuat. */
    public static DataSource newDataSource() {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1");
        SchemaInitializer.initialize(ds);
        return ds;
    }

    /** DataSource yang selalu melempar SQLException, untuk menguji jalur gagal. */
    public static DataSource brokenDataSource() {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class<?>[]{DataSource.class},
                (proxy, method, args) -> {
                    throw new SQLException("koneksi rusak");
                });
    }
}