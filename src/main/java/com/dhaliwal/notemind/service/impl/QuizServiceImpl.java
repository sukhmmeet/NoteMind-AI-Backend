package com.dhaliwal.notemind.service.impl;

import com.dhaliwal.notemind.dto.quiz.ai.AIQuizResponse;
import com.dhaliwal.notemind.dto.quiz.ai.QuizQuestion;
import com.dhaliwal.notemind.dto.quiz.response.QuizQuestionDto;
import com.dhaliwal.notemind.entity.Note;
import com.dhaliwal.notemind.entity.Quiz;
import com.dhaliwal.notemind.entity.User;
import com.dhaliwal.notemind.exception.InvalidCredentialsException;
import com.dhaliwal.notemind.exception.NoteNotFoundException;
import com.dhaliwal.notemind.repository.NoteRepository;
import com.dhaliwal.notemind.repository.QuizRepository;
import com.dhaliwal.notemind.security.CurrentUserProvider;
import com.dhaliwal.notemind.service.AIService;
import com.dhaliwal.notemind.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final NoteRepository noteRepository;
    private final QuizRepository quizRepository;
    private final CurrentUserProvider currentUserProvider;
    private final AIService aiService;

    @Override
    public List<QuizQuestionDto> generateQuiz(Long noteId, int count) {
        User user = currentUserProvider.getCurrentUser();
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new NoteNotFoundException("Note not found"));

        if (!user.getId().equals(note.getUser().getId())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        AIQuizResponse response = aiService.getAIQuizResponse(
                note.getTitle(),
                note.getContent(),
                note.getImageUrl(),
                count
        );

        quizRepository.save(Quiz.builder()
                .user(user)
                .note(note)
                .questionCount(response.getQuestions().size())
                .build());

        return response.getQuestions().stream()
                .map(this::toDto)
                .toList();
    }

    private QuizQuestionDto toDto(QuizQuestion question) {
        return QuizQuestionDto.builder()
                .question(question.getQuestion())
                .options(question.getOptions())
                .correctAnswer(question.getCorrectAnswer())
                .explanation(question.getExplanation())
                .build();
    }
}