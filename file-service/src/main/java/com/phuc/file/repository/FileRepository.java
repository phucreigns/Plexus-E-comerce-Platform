package com.phuc.file.repository;

import com.phuc.file.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {

    void deleteByName(String fileName);

    Optional<File> findByName(String fileName);

}
