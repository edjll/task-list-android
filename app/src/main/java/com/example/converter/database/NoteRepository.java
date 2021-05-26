package com.example.converter.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.converter.dto.NoteDto;
import com.example.converter.model.Note;
import com.example.converter.model.Task;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoteRepository {

    private final DatabaseHelper databaseHelper;

    private static final String NOTE_ID = "id";
    private static final String NOTE_TITLE = "title";
    private static final String NOTE_BODY = "body";
    private static final String NOTE_DATE = "date";
    private static final String TASK_ID = "id";
    private static final String TASK_TEXT = "text";
    private static final String TASK_COMPLETED = "completed";

    private final TaskRepository taskRepository;

    public NoteRepository(DatabaseHelper databaseHelper) {
        this.databaseHelper = databaseHelper;
        this.taskRepository = databaseHelper.getTaskRepository();
    }

    public Long save(Note note) {
        Long result = null;
        if (note != null) {
            ContentValues values = new ContentValues();
            values.put("title", note.getTitle());
            values.put("body", note.getBody());
            values.put("date", note.getDate().getTime());

            if (note.getId() != null) {
                values.put("id", note.getId());
                Note oldNote = this.findById(note.getId());
                SQLiteDatabase db = databaseHelper.getReadableDatabase();

                db.update("note", values, "id = ?", new String[] { String.valueOf(note.getId()) } );

                result = note.getId();

                List<Task> tasks = new ArrayList<>(note.getTasks());
                List<Long> oldTasks = new ArrayList<>();
                for (Task task : oldNote.getTasks()) {
                    oldTasks.add(task.getId());
                }
                List<Long> currentTasks = new ArrayList<>();
                db.close();
                for (int i = 0; i < tasks.size(); i++) {

                    long id = this.taskRepository.save(tasks.get(i));
                    db = databaseHelper.getReadableDatabase();
                    currentTasks.add(id);

                    if (oldTasks.contains(id)) continue;

                    ContentValues noteTagValues = new ContentValues();
                    noteTagValues.put("note_id", result);
                    noteTagValues.put("task_id", id);
                    db.insert("note_task", null, noteTagValues);
                    db.close();
                }
                for (int i = 0; i < oldTasks.size(); i++) {
                    if (currentTasks.contains(oldTasks.get(i))) continue;
                    db = databaseHelper.getReadableDatabase();
                    db.delete("note_task", "note_id = ? and task_id = ?", new String[] { String.valueOf(note.getId()), String.valueOf(oldTasks.get(i)) });
                    db.close();
                    taskRepository.deleteById(oldTasks.get(i));
                }

                db.close();

            } else {
                SQLiteDatabase db = databaseHelper.getReadableDatabase();
                result = db.insert("note", null, values);
                db.close();
                if (note.getTasks() != null && !note.getTasks().isEmpty()) {
                    for (Task task : note.getTasks()) {
                        long id = this.taskRepository.save(task);
                        db = databaseHelper.getReadableDatabase();
                        ContentValues noteTaskValues = new ContentValues();
                        noteTaskValues.put("note_id", result);
                        noteTaskValues.put("task_id", id);
                        db.insert("note_task", null, noteTaskValues);
                        db.close();
                    }
                }
            }
        }
        return result;
    }

    public void deleteById(long id) {
        Note note = this.findById(id);
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        db.delete("note_task", "note_id = ?", new String[] { String.valueOf(id) });
        db.delete("note", "id = ?", new String[] { String.valueOf(id) });
        db.close();
        for (Task task : note.getTasks()) {
            this.taskRepository.deleteById(task.getId());
        }
    }

    public Note findById(Long id) {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor noteCursor = db.rawQuery("select * from note where id = " + id, null);

        if (noteCursor == null || !noteCursor.moveToFirst()) {
            db.close();
            if (noteCursor != null) noteCursor.close();
            return null;
        }

        Note note = new Note();

        note.setId(noteCursor.getLong(noteCursor.getColumnIndex(NOTE_ID)));
        note.setTitle(noteCursor.getString(noteCursor.getColumnIndex(NOTE_TITLE)));
        note.setBody(noteCursor.getString(noteCursor.getColumnIndex(NOTE_BODY)));
        note.setDate(new Date(noteCursor.getLong(noteCursor.getColumnIndex(NOTE_DATE))));

        noteCursor.close();

        List<Task> tasks = new ArrayList<>();

        Cursor taskCursor = db.rawQuery(
                "select task.* " +
                    "from task " +
                        "join note_task on note_task.note_id = " + note.getId() + " " +
                            "and note_task.task_id = task.id ",
                null);

        if (taskCursor == null || !taskCursor.moveToFirst()) {
            db.close();
            if (taskCursor != null) taskCursor.close();
            note.setTasks(tasks);
            return note;
        }

        do {
            Task task = new Task();

            task.setId(taskCursor.getLong(taskCursor.getColumnIndex(TASK_ID)));
            task.setText(taskCursor.getString(taskCursor.getColumnIndex(TASK_TEXT)));
            task.setCompleted(taskCursor.getInt(taskCursor.getColumnIndex(TASK_COMPLETED)) != 0);

            tasks.add(task);
        } while (taskCursor.moveToNext());

        note.setTasks(tasks);

        taskCursor.close();
        db.close();

        return note;
    }

    public List<NoteDto> findAllNoteDto() {
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "select * " +
                    "from note " +
                        "left join (" +
                            "select note_task.note_id, count(task.id) as count_all, count(case when task.completed then 1 else null end) as count_completed " +
                            "from note_task " +
                                "join task on note_task.task_id = task.id " +
                            "group by note_task.note_id) as data " +
                        "on data.note_id = note.id " +
                    "order by note.date asc",
                null
        );
        List<NoteDto> notes = new ArrayList<>();

        if (cursor == null || !cursor.moveToFirst()) {
            db.close();
            if (cursor != null) cursor.close();
            return notes;
        }

        do {
            NoteDto noteDto = new NoteDto();
            noteDto.setId(cursor.getLong(cursor.getColumnIndex("id")));
            noteDto.setBody(cursor.getString(cursor.getColumnIndex("body")));
            noteDto.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            noteDto.setDate(new Date(cursor.getLong(cursor.getColumnIndex("date"))));
            noteDto.setCountAll(cursor.getInt(cursor.getColumnIndex("count_all")));
            noteDto.setCountCompleted(cursor.getInt(cursor.getColumnIndex("count_completed")));
            notes.add(noteDto);
        } while (cursor.moveToNext());

        return notes;
    }
}
