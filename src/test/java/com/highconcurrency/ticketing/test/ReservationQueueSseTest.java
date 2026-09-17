package com.highconcurrency.ticketing.test;

import com.highconcurrency.ticketing.infrastructure.database.java.JavaReservationQueueRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("java")
class ReservationQueueSseTest {

    private static final Long CONCERT_ID = 999L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JavaReservationQueueRepository reservationQueueRepository;

    @Test
    void 다음_대기_사용자가_허용되면_SSE로_PERMITTED와_userId를_받는다() throws Exception {
        for (long userId = 1L; userId <= 10001L; userId++) {
            reservationQueueRepository.enter(CONCERT_ID, userId);
        }

        MvcResult mvcResult = mockMvc.perform(get("/v1/reservation-queues/{concertId}/events", CONCERT_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("10001")))
                        .accept(MediaType.TEXT_EVENT_STREAM))
                .andExpect(status().isOk())
                .andExpect(request().asyncStarted())
                .andReturn();

        // 허용된 사용자가 이탈하면 다음 대기자에게 PERMITTED 이벤트가 발행된다.
        mockMvc.perform(delete("/v1/reservation-queues/{concertId}", CONCERT_ID)
                        .with(jwt().jwt(jwt -> jwt.subject("1"))))
                .andExpect(status().isNoContent());

        // 비동기 SSE 응답이 완료된 뒤 실제 전송된 body를 검증한다.
        mockMvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("PERMITTED")))
                .andExpect(content().string(containsString("\"userId\":10001")));
    }
}
