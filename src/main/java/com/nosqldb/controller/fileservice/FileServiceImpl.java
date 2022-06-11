package com.nosqldb.controller.fileservice;

import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Paths;

/**
 *  FileServiceImpl is an implementation of the FileService interface,
 *  it handles all the interaction with the files, and holds the directory
 *  of the data.
 */
@Service
public class FileServiceImpl implements FileService{
    private static final String DATA_PATH = "Data/";

    @Override
    public File getUsersFile() {
        return Paths.get(DATA_PATH + "users.json").toFile();
    }

    @Override
    public File getDatabaseSchemaFile(String DB) {
        return Paths.get(DATA_PATH + DB+"_schema.json").toFile();
    }

    @Override
    public File[] getAllDatabaseFiles() {
        return new File(DATA_PATH).listFiles();
    }
}
