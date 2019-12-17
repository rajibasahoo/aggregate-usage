package nl.tele2.fez.aggregateusage.util;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

@UtilityClass
public class AuthUtil {

    private static final Configuration configuration =
            Configuration.builder().jsonProvider(new JacksonJsonNodeJsonProvider()).mappingProvider(new JacksonMappingProvider()).build();

    private static final URI JWT_BODY = URI.create("security/jwt-body.json");
    private static final URI JWT_HEADER = URI.create("security/jwt-header.json");

    public static String getTokenAuthorizedFor(String customerId) {
        String jwtHeader = JsonPath.using(configuration)
                .parse(parseJson(JWT_HEADER)).jsonString();

        String jwtBody = JsonPath.using(configuration)
                .parse(parseJson(JWT_BODY))
                .set("$.role", "SLG").jsonString();

        if (StringUtils.isNotEmpty(customerId)) {
            jwtBody = JsonPath.using(configuration)
                    .parse(jwtBody)
                    .set("$.siebel_id", customerId).jsonString();
        }
        return Base64.getEncoder().encodeToString(jwtHeader.getBytes()) +
                "." +
                Base64.getEncoder().encodeToString(jwtBody.getBytes()) +
                "." +
                Base64.getEncoder().encodeToString(
                        "We_don't_care_now_about_signature".getBytes()
                );
    }

    private String parseJson(URI jsonUri) {
        try {
            return new String(Files.readAllBytes(Paths.get(AuthUtil.class.getClassLoader().getResource(jsonUri.toString()).toURI())));
        } catch (final IOException | URISyntaxException e) {
            return "";
        }
    }

}
