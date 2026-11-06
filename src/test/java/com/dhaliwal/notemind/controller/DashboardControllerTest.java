package com.dhaliwal.notemind.controller;

import com.dhaliwal.notemind.dto.DashboardDto;
import com.dhaliwal.notemind.dto.NoteDto;
import com.dhaliwal.notemind.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new DashboardController(dashboardService)).build();
    }

    @Test
    void getDashboardReturnsSummary() throws Exception {
        DashboardDto dashboard = DashboardDto.builder()
                .username("alice")
                .totalNotes(8)
                .totalFolders(3)
                .totalFlashcards(12)
                .totalQuizzes(2)
                .recentNotes(List.of(NoteDto.builder().id(1L).title("Recent note").build()))
                .build();
        when(dashboardService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.username").value("alice"))
                .andExpect(jsonPath("$.data.totalNotes").value(8))
                .andExpect(jsonPath("$.data.totalFolders").value(3))
                .andExpect(jsonPath("$.data.totalFlashcards").value(12))
                .andExpect(jsonPath("$.data.totalQuizzes").value(2))
                .andExpect(jsonPath("$.data.recentNotes.length()").value(1))
                .andExpect(jsonPath("$.data.recentNotes[0].title").value("Recent note"));
    }
}
