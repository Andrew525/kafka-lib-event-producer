package com.kafkalibevent.example.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafkalibevent.example.domain.LibraryEvent;
import com.kafkalibevent.example.producer.LibraryEventProducer;
import com.kafkalibevent.example.util.TestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventController.class)
class LibraryEventControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    LibraryEventProducer producer;


    @Test
    void postLibraryEvent() throws Exception {
        //given
        doNothing().when(producer).sendLibraryEventV3_producerRecord(any(LibraryEvent.class));
        var json = objectMapper.writeValueAsBytes(TestUtil.libraryEventRecord());

        //when
        mvc.perform(post("/v1/libraryevents")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        //then
        verify(producer).sendLibraryEventV3_producerRecord(any(LibraryEvent.class));
    }

    @Test
    void postLibraryEvent_invalidValues() throws Exception {
        //given
        doNothing().when(producer).sendLibraryEventV3_producerRecord(any(LibraryEvent.class));
        var json = objectMapper.writeValueAsBytes(TestUtil.libraryEventRecordWithInvalidBook());
        var expectedErrMsg = "book.bookAuthor - must not be blank; book.bookId - must not be null";

        //when
        mvc.perform(post("/v1/libraryevents")
                        .content(json)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedErrMsg))
                .andDo(print());

        //then
        verifyNoInteractions(producer);
    }
}