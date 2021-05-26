package com.example.converter.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_NOTE = "create table note (id integer primary key, title text, body text, date date)";
    private static final String CREATE_TABLE_TASK = "create table task (id integer primary key, text text, completed boolean)";
    private static final String CREATE_TABLE_NOTE_TASK = "create table note_task (note_id integer, task_id integer, foreign key (note_id) references note(id), foreign key (task_id) references task(id))";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTE);
        db.execSQL(CREATE_TABLE_TASK);
        db.execSQL(CREATE_TABLE_NOTE_TASK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists note");
        db.execSQL("drop table if exists task");
        db.execSQL("drop table if exists note_task");

        onCreate(db);
    }

    public NoteRepository getNoteRepository() {
        return new NoteRepository(this);
    }

    public TaskRepository getTaskRepository() {return new TaskRepository(this);}
}
