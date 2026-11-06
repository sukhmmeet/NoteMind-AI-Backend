package com.dhaliwal.notemind.service.impl;

import com.dhaliwal.notemind.entity.Note;
import com.dhaliwal.notemind.entity.User;
import com.dhaliwal.notemind.mapper.NoteMapper;
import com.dhaliwal.notemind.repository.FlashCardRepository;
import com.dhaliwal.notemind.repository.FolderRepository;
import com.dhaliwal.notemind.repository.NoteRepository;
import com.dhaliwal.notemind.repository.QuizRepository;
import com.dhaliwal.notemind.security.CurrentUserProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private CurrentUserProvider currentUserProvider;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private FlashCardRepository flashCardRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private NoteMapper noteMapper;
    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    void getDashboardReturnsUserScopedSummaryAndRecentNotes() {
        User user = User.builder().id(7L).username("alice").build();
        Note note = Note.builder().id(1L).title("Recent").build();
        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(noteRepository.countByUserId(7L)).thenReturn(8L);
        when(folderRepository.countByUserId(7L)).thenReturn(3L);
        when(flashCardRepository.countByUserId(7L)).thenReturn(12L);
        when(quizRepository.countByUserId(7L)).thenReturn(2L);
        when(noteRepository.findAllByUserId(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(note)));

        var result = dashboardService.getDashboard();

        assertEquals("alice", result.getUsername());
        assertEquals(8, result.getTotalNotes());
        assertEquals(3, result.getTotalFolders());
        assertEquals(12, result.getTotalFlashcards());
        assertEquals(2, result.getTotalQuizzes());
        assertEquals(1, result.getRecentNotes().size());
        verify(noteRepository).findAllByUserId(eq(7L), any(Pageable.class));
    }
}
