package com.nosqldb.controller.fileservice;

import java.io.File;

public interface FileService {
    File getUsersFile();

    File getDatabaseSchemaFile(String DB);

    File[] getAllDatabaseFiles();
}