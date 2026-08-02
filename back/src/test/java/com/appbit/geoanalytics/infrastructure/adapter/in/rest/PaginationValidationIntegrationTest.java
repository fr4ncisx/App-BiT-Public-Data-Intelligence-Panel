package com.appbit.geoanalytics.infrastructure.adapter.in.rest;

import com.appbit.geoanalytics.infrastructure.adapter.in.rest.code.ApiResponseCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaginationValidationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsLimitBelowMinimumOnSocialGap() throws Exception {
        mockMvc.perform(get("/api/v1/social/gap/training").param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()));
    }

    @Test
    void rejectsLimitAboveMaximumOnSocialGap() throws Exception {
        mockMvc.perform(get("/api/v1/social/gap/training").param("limit", "1001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNegativeOffsetOnSocialGap() throws Exception {
        mockMvc.perform(get("/api/v1/social/gap/training").param("offset", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsLimitBelowMinimumOnRanking() throws Exception {
        mockMvc.perform(get("/api/v1/ranking")
                        .param("indicatorType", "TRAINING")
                        .param("limit", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ApiResponseCode.VALIDATION_ERROR.name()));
    }

    @Test
    void rejectsLimitAboveMaximumOnRanking() throws Exception {
        mockMvc.perform(get("/api/v1/ranking")
                        .param("indicatorType", "TRAINING")
                        .param("limit", "1001"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsBoundaryLimits() throws Exception {
        mockMvc.perform(get("/api/v1/social/gap/training")
                        .param("limit", "1")
                        .param("offset", "0"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/ranking")
                        .param("indicatorType", "TRAINING")
                        .param("limit", "1000"))
                .andExpect(status().isOk());
    }
}
