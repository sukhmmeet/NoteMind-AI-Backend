package com.dhaliwal.notemind.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {
    private String username;
    private long totalNotes;
    private long totalFolders;
    private long totalFlashcards;
    private long totalQuizzes;
    private List<NoteDto> recentNotes;
}
