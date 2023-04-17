package edu.uob;

import edu.uob.DBExceptions.QueryException;
import edu.uob.DBExceptions.QueryException.*;
import edu.uob.DBExceptions.DBException.Entity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DatabaseManager {
    private String currentDataBase;
    private String storageFolderPath;
    private File storageFile;
    private HashMap<String, File> databasesFile = new HashMap<>();

    public DatabaseManager(String storagePath) {
        try {
            this.storageFolderPath = storagePath;
            this.storageFile = new File(storagePath);
            readDatabaseList();
        } catch (Exception e) {
            System.out.println("Failed to initialize database manager");
        }
    }

    public void switchToDB(String dbName) throws QueryException {
        if (!this.databasesFile.containsKey(dbName) || !this.databasesFile.get(dbName).exists()) {
            throw new EntityNotFoundException(dbName, Entity.DATABASE);
        }
        this.currentDataBase = dbName;
    }

    public Database getCurrentDB() throws QueryException {
        if (this.currentDataBase == null) {
            throw new NotDefinedException(Entity.DATABASE);
        }
        return getDB(this.currentDataBase);
    }

    public void createDatabase(String dbName) throws QueryException {
        if (this.databasesFile.containsKey(dbName)) {
            throw new DoubleCreateException(dbName, Entity.DATABASE);
        }
        File newDB = new File(this.storageFile + File.separator + dbName);
        if(!newDB.mkdir()) {
            throw new QueryException(String.format("Failed to create file for database [%s]", dbName));
        }
        this.databasesFile.put(dbName, newDB);
    }

    public void dropDataBase(String dbName) throws QueryException {
        if (!this.databasesFile.containsKey(dbName)) {
            throw new EntityNotFoundException(dbName, Entity.DATABASE);
        }
        if (!this.databasesFile.get(dbName).exists() || !this.databasesFile.get(dbName).isDirectory()) {
            throw new EntityNotFoundException(dbName, Entity.DATABASE);
        }
        deleteDB(this.databasesFile.get(dbName));
        if (this.currentDataBase != null && this.currentDataBase.equals(dbName)) {
            this.currentDataBase = null;
        }
    }

    private void readDatabaseList() {
        File[] databases = this.storageFile.listFiles();
        if (databases == null) return;
        for (File database: databases) {
            if (database.isDirectory()) {
                this.databasesFile.put(database.getName(), database);
            }
        }
    }

    private Database getDB(String dbName) throws QueryException {
        if (!databasesFile.containsKey(dbName)) {
            throw new EntityNotFoundException(dbName, Entity.DATABASE);
        }
        if (!databasesFile.get(dbName).exists()) {
            throw new EntityNotFoundException(dbName, Entity.DATABASE);
        }
        return new Database(databasesFile.get(dbName));
    }

    private void deleteDB(File dbFile) throws QueryException {
        String dbName = dbFile.getName();
        File[] tables = dbFile.listFiles();
        if (tables == null) return;
        for (File table: tables) {
            if (!table.delete()) {
                throw new QueryException(String.format("Failed to delete file for " +
                        "table [%s] in database [%s]", table.getName(), dbName));
            }
        }
        if (!dbFile.delete()) {
            throw new QueryException(String.format("Failed to delete file for database [%s]", dbName));
        }
        this.databasesFile.remove(dbName);
    }
}
