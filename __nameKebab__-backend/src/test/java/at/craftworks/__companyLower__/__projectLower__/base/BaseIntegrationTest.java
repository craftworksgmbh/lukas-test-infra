package at.craftworks.__companyLower__.__projectLower__.base;

import at.craftworks.__companyLower__.__projectLower__.config.JacksonConfiguration;
import io.restassured.config.LogConfig;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import io.restassured.module.mockmvc.config.RestAssuredMockMvcConfig;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import lombok.extern.java.Log;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManager;

@Log
@ActiveProfiles({"test", "testdata"})
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public abstract class BaseIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private JacksonConfiguration jacksonConfiguration;
    //@Autowired
    //private JwtTokenService jwtTokenService;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private EntityManager entityManager;

    @Before
    public void beforeBase() throws AuthenticationServiceException {
        RestAssuredMockMvc.mockMvc(mockMvc);

        ObjectMapperConfig objectMapperConfig = new ObjectMapperConfig()
                .jackson2ObjectMapperFactory(
                        (aClass, s) -> jacksonConfiguration.jackson2ObjectMapperBuilder().build()
                );
        RestAssuredMockMvc.config = RestAssuredMockMvcConfig.newConfig()
                .logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails())
                .objectMapperConfig(objectMapperConfig);
    }

    protected MockMvcRequestSpecification request() {
        return RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    /*
    protected MockMvcRequestSpecification requestAs(String username) {
        String jwt = generateJwtForUser(username);

        return request()
                .header("Authorization", "Bearer " + jwt);
    }

    private String generateJwtForUser(String username) {
        AuthUser authUser = (AuthUser) userDetailsService.loadUserByUsername(username);

        return jwtTokenService.generateAuthToken(
                authUser,
                Instant.now()
        );
    }

    protected AuthenticationTokenDTO login(String username, String password) {
        PasswordGrantTypeDTO cred = new PasswordGrantTypeDTO();
        cred.setUsername(username);
        cred.setPassword(password);
        return request()
                .body(cred)
                .post("/api/v1/auth/token")
                .then().statusCode(HttpStatus.SC_OK)
                .extract().response().as(AuthenticationTokenDTO.class);
    }
    */

    /**
     * Executes sql. Useful in transactional tests if you want to execute sql before transaction commit.
     */
    protected void databaseFlush() {
        entityManager.flush();
    }
}


