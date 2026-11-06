package com.dhaliwal.notemind.service.impl;

import com.dhaliwal.notemind.dto.DashboardDto;
import com.dhaliwal.notemind.entity.User;
import com.dhaliwal.notemind.mapper.NoteMapper;
import com.dhaliwal.notemind.repository.FlashCardRepository;
import com.dhaliwal.notemind.repository.FolderRepository;
import com.dhaliwal.notemind.repository.NoteRepository;
import com.dhaliwal.notemind.repository.QuizRepository;
import com.dhaliwal.notemind.security.CurrentUserProvider;
import com.dhaliwal.notemind.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final int RECENT_NOTES_LIMIT = 4;

    private final CurrentUserProvider currentUserProvider;
    private final NoteRepository noteRepository;
    private final FolderRepository folderRepository;
    private final FlashCardRepository flashCardRepository;
    private final QuizRepository quizRepository;
    private final NoteMapper noteMapper;

    @Override
    public DashboardDto getDashboard() {
        User user = currentUserProvider.getCurrentUser();
        Long userId = user.getId();
        PageRequest recentNotesPage = PageRequest.of(
                0,
                RECENT_NOTES_LIMIT,
                Sort.by(Sort.Direction.DESC, "updatedAt")
        );

        return DashboardDto.builder()
                .username(user.getUsername())
                .totalNotes(noteRepository.countByUserId(userId))
                .totalFolders(folderRepository.countByUserId(userId))
                .totalFlashcards(flashCardRepository.countByUserId(userId))
                .totalQuizzes(quizRepository.countByUserId(userId))
                .recentNotes(noteRepository.findAllByUserId(userId, recentNotesPage)
                        .getContent()
                        .stream()
                        .map(noteMapper::toDto)
                        .toList())
                .build();
    }
}
