package com.example.carrental;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test through the real Spring context: real controller, service, repository and
 * fleet from application.yml (1 van). Uses a date far in the future so the real clock is fine.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CarRentalApplicationIT {

    private static final String START = "2099-06-01T10:00:00";
    private static final String BOOK_VAN = """
            {"carType": "VAN", "start": "%s", "days": 2}
            """.formatted(START);

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void bookUntilFullThenCancelFreesTheCar() throws Exception {
        // 1. One van is available
        mvc.perform(vanAvailability())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available[0].count").value(1));

        // 2. Book it
        String body = mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(BOOK_VAN))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.carId").value("VAN-1"))
                .andReturn().getResponse().getContentAsString();
        String id = objectMapper.readTree(body).get("id").asText();

        // 3. Second booking for the same period is rejected and availability is now 0
        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(BOOK_VAN))
                .andExpect(status().isConflict());
        mvc.perform(vanAvailability())
                .andExpect(jsonPath("$.available[0].count").value(0));

        // 4. The reservation can be read back
        mvc.perform(get("/reservations/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        // 5. Cancel it; it stays readable as CANCELLED
        mvc.perform(delete("/reservations/{id}", id))
                .andExpect(status().isNoContent());
        mvc.perform(get("/reservations/{id}", id))
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        // 6. The van can be booked again
        mvc.perform(post("/reservations").contentType(MediaType.APPLICATION_JSON).content(BOOK_VAN))
                .andExpect(status().isCreated());
    }

    private static MockHttpServletRequestBuilder vanAvailability() {
        return get("/availability")
                .param("start", START)
                .param("days", "2")
                .param("carType", "VAN");
    }
}
