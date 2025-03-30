package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(controllers = HomepageController.class)
@Import(TestConfig.class)
public class HomepageControllerTests extends ControllerTestCase {

    @MockBean
    UserRepository userRepository;

    @Test
    public void homepage_returns_successfully() throws Exception {
        String expectedResponse = """
                <p>This is the homepage for team01 which is simply a backend with no frontend.</p>
                <p><ul>
                  <li><a href="/oauth2/authorization/google">To sign in, click here</a></li>
                  <li><a href="/logout">To sign out, click here</a></li>
                  <li><a href="/swagger-ui/index.html">Click here to go to Swagger</a></li>
                </ul></p>
                """;
        MvcResult response = mockMvc.perform(get("/")).andExpect(status().isOk()).andReturn();
        assertEquals(expectedResponse, response.getResponse().getContentAsString());
    }
}
