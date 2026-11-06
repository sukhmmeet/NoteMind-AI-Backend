package com.dhaliwal.notemind.service.impl;

import com.dhaliwal.notemind.dto.quiz.ai.AIQuizResponse;
import com.dhaliwal.notemind.dto.quiz.ai.QuizQuestion;
import com.dhaliwal.notemind.dto.quiz.response.QuizQuestionDto;
import com.dhaliwal.notemind.entity.Note;
import com.dhaliwal.notemind.entity.User;
import com.dhaliwal.notemind.exception.InvalidCredentialsException;
import com.dhaliwal.notemind.exception.NoteNotFoundException;
import com.dhaliwal.notemind.repository.NoteRepository;
import com.dhaliwal.notemind.repository.QuizRepository;
import com.dhaliwal.notemind.security.CurrentUserProvider;
import com.dhaliwal.notemind.service.AIService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private AIService aiService;

    @InjectMocks
    private QuizServiceImpl quizService;

    @Test
    void generateQuizReturnsAiQuestions() {
        User user = User.builder().id(1L).build();
        Note note = Note.builder()
                .id(10L)
                .title("Java")
                .content("Java content")
                .user(user)
                .build();
        QuizQuestion question = QuizQuestion.builder()
                .question("What is Java?")
                .options(List.of("Language", "Database"))
                .correctAnswer("Language")
                .explanation("It is a language.")
                .build();

        when(currentUserProvider.getCurrentUser()).thenReturn(user);
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        when(aiService.getAIQuizResponse("Java", "Java content", null, 3))
                .thenReturn(new AIQuizResponse(List.of(question)));

        List<QuizQuestionDto> result = quizService.generateQuiz(10L, 3);

        assertEquals(1, result.size());
        assertEquals("What is Java?", result.getFirst().getQuestion());
        assertEquals("Language", result.getFirst().getCorrectAnswer());
        verify(aiService).getAIQuizResponse("Java", "Java content", null, 3);
        verify(quizRepository).save(any());
    }

    @Test
    void generateQuizRejectsAnotherUsersNote() {
        User currentUser = User.builder().id(1L).build();
        User owner = User.builder().id(2L).build();
        Note note = Note.builder().id(10L).user(owner).build();

        when(currentUserProvider.getCurrentUser()).thenReturn(currentUser);
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        assertThrows(InvalidCredentialsException.class, () -> quizService.generateQuiz(10L, 3));
        verify(aiService, never()).getAIQuizResponse(any(), any(), any(), any(Integer.class));
    }

    @Test
    void generateQuizRejectsMissingNote() {
        when(currentUserProvider.getCurrentUser()).thenReturn(User.builder().id(1L).build());
        when(noteRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(NoteNotFoundException.class, () -> quizService.generateQuiz(10L, 3));
    }
}