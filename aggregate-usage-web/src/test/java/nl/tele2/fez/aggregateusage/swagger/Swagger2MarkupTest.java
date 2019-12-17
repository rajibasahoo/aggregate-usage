package nl.tele2.fez.aggregateusage.swagger;

import nl.tele2.fez.aggregateusage.Application;
import nl.tele2.fez.aggregateusage.service.AggregateUsageFactory;
import nl.tele2.fez.aggregateusage.service.MessagesService;
import nl.tele2.fez.aggregateusage.service.MsisdnsService;
import nl.tele2.fez.aggregateusage.service.NationalBalanceService;
import nl.tele2.fez.aggregateusage.service.RestOfWorldBalanceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static nl.tele2.fez.aggregateusage.util.AuthUtil.getTokenAuthorizedFor;
import static nl.tele2.fez.aggregateusage.TestConstants.CUSTOMER_ID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * Extracting swagger.json to a file for using in generating documentation
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("local")
@AutoConfigureMockMvc
public class Swagger2MarkupTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NationalBalanceService nationalBalanceService;

    @MockBean
    private RestOfWorldBalanceService restOfWorldBalanceService;

    @MockBean
    private MsisdnsService msisdnsService;

    @MockBean
    private AggregateUsageFactory aggregateUsageFactory;

    @MockBean
    private MessagesService messagesService;

    @Test
    public void createSpringfoxSwaggerJson() throws Exception {

        final String outputDir = System.getProperty("io.springfox.staticdocs.outputDir");
        final MvcResult mvcResult = mockMvc.perform(get("/v2/api-docs")
                .header("jwtoken", getTokenAuthorizedFor(CUSTOMER_ID))
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn();

        final MockHttpServletResponse response = mvcResult.getResponse();
        final String swaggerJson = response.getContentAsString();
        Files.createDirectories(Paths.get(outputDir));
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(outputDir, "swagger.json"), StandardCharsets.UTF_8)) {
            writer.write(swaggerJson);
        }
    }

}