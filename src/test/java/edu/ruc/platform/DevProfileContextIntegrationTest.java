package edu.ruc.platform;

import edu.ruc.platform.admin.service.AdminApplicationService;
import edu.ruc.platform.auth.service.AuthApplicationService;
import edu.ruc.platform.platform.service.PlatformApplicationService;
import edu.ruc.platform.student.service.StudentProfileApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:devprofile;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC",
        "spring.flyway.enabled=false"
})
class DevProfileContextIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private AuthApplicationService authApplicationService;

    @Autowired
    private PlatformApplicationService platformApplicationService;

    @Autowired
    private AdminApplicationService adminApplicationService;

    @Autowired
    private StudentProfileApplicationService studentProfileApplicationService;

    @Test
    void devProfileContextLoadsWithRepositoryBackedBeans() {
        assertThat(applicationContext).isNotNull();
        assertThat(dataSource).isNotNull();
        assertThat(authApplicationService.getClass().getSimpleName()).isEqualTo("AuthService");
        assertThat(platformApplicationService.getClass().getSimpleName()).isEqualTo("PlatformService");
        assertThat(adminApplicationService.getClass().getSimpleName()).isEqualTo("AdminService");
        assertThat(studentProfileApplicationService.getClass().getSimpleName()).isEqualTo("StudentProfileService");
    }
}
