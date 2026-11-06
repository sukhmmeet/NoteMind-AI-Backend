package com.dhaliwal.notemind.repository;

import com.dhaliwal.notemind.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Long> {
    long countByUserId(Long userId);

    Optional<Folder> findByName(String name);

    List<Folder> findAllByUserId(Long id);

    Optional<Folder> findByNameAndUserId(String name, Long userId);
}
