package io.sailex.ai.npc.client.llm;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.OllamaChatRequest;
import io.github.ollama4j.models.chat.OllamaChatResult;
import io.github.ollama4j.models.embeddings.OllamaEmbedResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OllamaClientTest {

    private OllamaClient ollamaClient;
    private OllamaAPI ollamaAPI;

    @BeforeEach
    public void setUp() {
        ollamaAPI = mock(OllamaAPI.class);
        ollamaClient = new OllamaClient("gemma2", "http://localhost:11343");
        ollamaClient.setOllamaAPI(ollamaAPI);
    }

    @Test
    void testGenerateResponse() throws OllamaBaseException, IOException, InterruptedException {
        String userPrompt = "hello, how are you?";
        String systemPrompt = "some world context";
        String expectedResponse = "expected response";

        when(ollamaAPI.chat(any(OllamaChatRequest.class)))
                .thenReturn(new OllamaChatResult(expectedResponse, 0L, 200, new ArrayList<>()));

        String response = ollamaClient.generateResponse(userPrompt, systemPrompt);

        assertNotNull(response);
        assertEquals(expectedResponse, response);
        verify(ollamaAPI, times(1)).chat(any(OllamaChatRequest.class));
    }

    @Test
    void testGenerateResponseWithException() throws OllamaBaseException, IOException, InterruptedException {
        String userPrompt = "hello, how are you?";
        String systemPrompt = "some world context";

        when(ollamaAPI.chat(any(OllamaChatRequest.class)))
                .thenThrow(new OllamaBaseException("ollama exception"));

        String response = ollamaClient.generateResponse(userPrompt, systemPrompt);

        assertNull(response);
        verify(ollamaAPI, times(1)).chat(any(OllamaChatRequest.class));
    }

    @Test
    void testGenerateResponseWithNullPrompt() throws OllamaBaseException, IOException, InterruptedException {
        String systemPrompt = "some world context";

        String response = ollamaClient.generateResponse(null, systemPrompt);

        assertNull(response);
        verify(ollamaAPI, never()).chat(any(OllamaChatRequest.class));
    }

    @Test
    void testGenerateEmbedding() throws IOException, OllamaBaseException, InterruptedException {
        String prompt = "hello, how are you?";
        double[] expectedEmbedding = {1.0f, 2.0f, 3.0f, 4.0f};

        OllamaEmbedResponseModel ollamaEmbed = new OllamaEmbedResponseModel();
        ollamaEmbed.setEmbeddings(List.of(List.of(1.0, 2.0), List.of(3.0, 4.0)));
        when(ollamaAPI.embed(anyString(), anyList())).thenReturn(ollamaEmbed);

        double[] embedding = ollamaClient.generateEmbedding(Collections.singletonList(prompt));

        assertNotNull(embedding);
        assertArrayEquals(expectedEmbedding, embedding);
    }

    @Test
    void testGenerateEmbeddingWithException() throws IOException, OllamaBaseException, InterruptedException {
        String prompt = "hello, how are you?";

        when(ollamaAPI.embed(anyString(), anyList()))
                .thenThrow(new OllamaBaseException("ollama exception"));

        double[] embedding = ollamaClient.generateEmbedding(Collections.singletonList(prompt));

        assertNull(embedding);
    }

    @Test
    void testGenerateEmbeddingWithNullPrompt() throws IOException, OllamaBaseException, InterruptedException {
        double[] embedding = ollamaClient.generateEmbedding(null);

        assertNull(embedding);
        verify(ollamaAPI, never()).embed(anyString(), anyList());
    }

}
