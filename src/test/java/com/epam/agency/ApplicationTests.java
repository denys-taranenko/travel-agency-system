package com.epam.agency;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@TestPropertySource(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
@ActiveProfiles("test")
@Sql(scripts = {"/test-schema.sql", "/test-data.sql"})
class ApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    public void testDatabaseInitialization() {
        List<String> voucherColumns = jdbcTemplate.queryForList("SELECT column_name FROM information_schema.columns " +
                        "WHERE table_name = 'VOUCHERS'", String.class);

        assertThat(voucherColumns).containsExactlyInAnyOrder(
                "ID", "TITLE", "DESCRIPTION", "PRICE", "TOUR_TYPE",
                "TRANSFER_TYPE", "HOTEL_TYPE", "STATUS", "ARRIVAL_DATE",
                "EVICTION_DATE", "HOT_STATUS", "DELETED_AT");

        Integer voucherCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM vouchers", Integer.class);
        assertThat(voucherCount).isEqualTo(1);

        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        assertThat(userCount).isEqualTo(3);
    }

    @Test
    public void testVoucherData() {
        String title = jdbcTemplate.queryForObject("SELECT title FROM vouchers WHERE id = ?", String.class,
                UUID.fromString("660e8400-e29b-41d4-a716-446655440000"));
        assertThat(title).isEqualTo("Thermal Spa Retreat");
    }
}
