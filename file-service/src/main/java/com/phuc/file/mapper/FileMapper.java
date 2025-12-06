package com.phuc.file.mapper;

import com.phuc.file.dto.response.FileResponse;
import com.phuc.file.entity.File;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

      FileResponse toFileResponse(File file);

}


