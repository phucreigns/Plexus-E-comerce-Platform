package com.phuc.file.repository;

import com.phuc.file.entity.File;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface FileRepository extends MongoRepository<File, String> {

    void deleteByName(String fileName);

    Optional<File> findByName(String fileName);

}
