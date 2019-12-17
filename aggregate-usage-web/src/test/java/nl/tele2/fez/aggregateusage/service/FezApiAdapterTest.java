package nl.tele2.fez.aggregateusage.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.slf4j.MDC;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class FezApiAdapterTest {
    @Mock
    private RestTemplate restTemplate;

    private FezApiAdapter fezApiAdapter;

    @Before
    public void setUp() throws Exception {
        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class), Matchers.<Object>anyVararg()))
                .thenReturn(ResponseEntity.ok("Got it"));

        fezApiAdapter = new FezApiAdapter(restTemplate);

        MDC.put(FezApiAdapter.BUSINESS_PROCESS_ID_HEADER, "Foo-123");
        MDC.put(FezApiAdapter.CONVERSATION_ID_HEADER, "Bar-123");
    }

    @After
    public void tearDown() throws Exception {
        MDC.clear();
    }

    @Test
    public void shouldSetBusinessProcessIdHeaderForPost() {
        fezApiAdapter.post("foo", "Thing");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("foo"), eq(HttpMethod.POST), entityCaptor.capture(), eq(Void.class));

        assertThat(entityCaptor.getValue().getHeaders().get(FezApiAdapter.BUSINESS_PROCESS_ID_HEADER), contains("Foo-123"));
    }

    @Test
    public void shouldGenerateConversationIdHeaderForPost() {
        fezApiAdapter.post("foo", "Thing");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("foo"), eq(HttpMethod.POST), entityCaptor.capture(), eq(Void.class));

        List<String> conversationId = entityCaptor.getValue().getHeaders().get(FezApiAdapter.CONVERSATION_ID_HEADER);
        assertThat(conversationId, hasSize(1));
        assertThat(conversationId.get(0), not(equalTo("Bar-123")));
    }

    @Test
    public void shouldSetBusinessProcessIdHeaderForGet() {
        fezApiAdapter.get("foo", String.class,"param");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("foo"), eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class), eq("param"));

        List<String> bpidHeaders = entityCaptor.getValue().getHeaders().get(FezApiAdapter.BUSINESS_PROCESS_ID_HEADER);
        assertThat(bpidHeaders, hasSize(1));
        assertThat(bpidHeaders.get(0), equalTo("Foo-123"));
    }

    @Test
    public void shouldGenerateConversationIdHeaderForGet() {
        fezApiAdapter.get("foo", String.class,"param");

        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq("foo"), eq(HttpMethod.GET), entityCaptor.capture(), eq(String.class), eq("param"));

        List<String> conversationId = entityCaptor.getValue().getHeaders().get(FezApiAdapter.CONVERSATION_ID_HEADER);
        assertThat(conversationId, hasSize(1));
        assertThat(conversationId.get(0), not(equalTo("Bar-123")));
    }
}