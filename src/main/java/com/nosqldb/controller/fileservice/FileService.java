package com.nosqldb.controller.fileservice;

import java.io.File;
import java.io.IOException;

public interface FileService {
    File getUsersFile();
    File getDatabaseSchemaFile(String DB);
    File[] getAllDatabaseFiles();
}