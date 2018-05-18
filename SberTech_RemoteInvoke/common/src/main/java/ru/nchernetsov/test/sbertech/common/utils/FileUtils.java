package ru.nchernetsov.test.sbertech.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;

public class FileUtils {
    private static final Logger LOG = LoggerFactory.getLogger(FileUtils.class);

    public static boolean isFolderExists(String pathToFolder) {
        Path path = Paths.get(pathToFolder);
        return Files.exists(path);
    }

    public static boolean isFileExists(String pathToFolder, String fileName) {
        Path path = Paths.get(pathToFolder, fileName);
        return Files.exists(path);
    }

    public static boolean createNewDirectory(String pathToFolder, String directoryName) {
        Path dir = Paths.get(pathToFolder, directoryName);
        try {
            Files.createDirectory(dir);
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    public static boolean createNewFile(String pathToFolder, String fileName, byte[] filePayload) {
        Path path = Paths.get(pathToFolder, fileName);
        try {
            Files.write(path, filePayload, StandardOpenOption.CREATE_NEW);
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    // создать файл, если он не существует, или перезаписать существующий
    public static boolean createNewOrUpdateFile(String pathToFolder, String fileName, byte[] filePayload) {
        Path path = Paths.get(pathToFolder, fileName);
        try {
            if (isFileExists(pathToFolder, fileName)) {
                Files.write(path, filePayload, StandardOpenOption.WRITE);
            } else {
                Files.write(path, filePayload, StandardOpenOption.CREATE_NEW);
            }
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    public static boolean deleteDirectory(String pathToFolder) {
        Path pathFolderToDelete = Paths.get(pathToFolder);
        try {
            Files.walk(pathFolderToDelete)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
            return !Files.exists(pathFolderToDelete);  // если папка всё ещё не удалена, то false
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    public static boolean renameFile(String pathToFolder, String oldName, String newName) {
        Path fileToRenamePath = Paths.get(pathToFolder, oldName);
        Path fileAfterRenamePath = Paths.get(pathToFolder, newName);
        try {
            Files.move(fileToRenamePath, fileAfterRenamePath);
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    public static boolean changeFileContent(String pathToFolder, String fileName, byte[] filePayload) {
        Path path = Paths.get(pathToFolder, fileName);
        try {
            Files.write(path, filePayload, StandardOpenOption.WRITE);
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }

    public static boolean deleteFile(String pathToFolder, String fileName) {
        try {
            Files.delete(Paths.get(pathToFolder, fileName));
            return true;
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return false;
    }
}
