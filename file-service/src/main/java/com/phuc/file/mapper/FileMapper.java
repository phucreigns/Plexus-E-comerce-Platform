package com.phuc.file.mapper;

import com.phuc.file.dto.request.FileRequest;
import com.phuc.file.dto.response.FileResponse;
import com.phuc.file.entity.File;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FileMapper {
    File toFile(FileRequest request);

    FileResponse toFileResponse(File file);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFile(@MappingTarget File file, FileRequest request);
}

