package com.example.converter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.converter.model.Note;
import com.example.converter.model.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class TaskRepository {

    private final DatabaseHelper databaseHelper;

    private static final String TABLE = "task";
    private static final String TASK_ID = "id";
    private static final String TASK_TEXT = "text";
    private static final String TASK_COMPLETED = "completed";

    public TaskRepository(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
    }

    public Long save(Task task) {
        Long result = null;
        if (task != null) {
            ContentValues values = new ContentValues();
            values.put("text", task.getText());
            values.put("completed", task.isCompleted() ? 1 : 0);

            if (task.getId() != null) {
                values.put("id", task.getId());
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                db.update(TABLE, values, "id = ?", new String[] { String.valueOf(task.getId()) } );
                result = task.getId();
                db.close();
            } else {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                result = db.insert("task", null, values);
                db.close();
            }
        }
        return result;
    }

    public void deleteById(Long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.delete(TABLE, "id = ? ", new String[] { String.valueOf(id)});
        db.close();
    }
}
