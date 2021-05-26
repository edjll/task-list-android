package com.example.converter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.converter.watcher.TextWatcherImpl;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;

import com.example.converter.database.DatabaseHelper;
import com.example.converter.database.NoteRepository;
import com.example.converter.model.Note;
import com.example.converter.model.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoteActivity extends AppCompatActivity {

    private NoteRepository noteRepository;
    private LinearLayout llTasks;
    private FloatingActionButton btnAddTask;
    private EditText currentTaskText;
    private TextWatcher textWatcher;

    private EditText editTextTitle;
    private EditText editTextBody;
    private Long id;
    private DatePickerDialog datePickerDialog;
    private Date date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        this.llTasks = findViewById(R.id.llTasks);
        this.btnAddTask = findViewById(R.id.btnAddTask);
        this.btnAddTask.setOnClickListener(v -> this.llTasks.addView(createTaskView(null)));

        long id = this.getIntent().getLongExtra("id", -1);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        this.editTextTitle = findViewById(R.id.editTextNoteTitle);
        this.editTextBody = findViewById(R.id.editTextNoteBody);

        noteRepository = databaseHelper.getNoteRepository();

        noteRepository.findAllNoteDto();

        this.textWatcher = new TextWatcherImpl(this.btnAddTask);

        if (id > 0) {
            this.id = id;
            Note note = noteRepository.findById(id);
            editTextTitle.setText(note.getTitle());
            editTextBody.setText(note.getBody());
            this.date = note.getDate();

            for (Task task : note.getTasks()) {
                this.llTasks.addView(createTaskView(task));
            }

            this.btnAddTask.setVisibility(View.VISIBLE);
        } else {
            this.date = new Date();
        }

        this.datePickerDialog = new DatePickerDialog((date) -> this.date = date, this.date);
    }

    private View createTaskView(Task task) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(15, 15, 15, 15);

        TextView txtId = new TextView(this);
        txtId.setVisibility(View.INVISIBLE);
        txtId.setText(task != null ? String.valueOf(task.getId()) : "");

        EditText txtText = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.weight = 1;
        txtText.setLayoutParams(layoutParams);
        txtText.setText(task != null ? task.getText() : "");

        CheckBox cbCompleted = new CheckBox(this);
        cbCompleted.setButtonTintList(ColorStateList.valueOf(Color.rgb(98, 0, 238)));
        cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                txtText.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                txtText.setPaintFlags(Paint.ANTI_ALIAS_FLAG);
            }
        });
        cbCompleted.setChecked(task != null && task.isCompleted());

        Button btnDelete = new Button(this);
        LinearLayout.LayoutParams btnLP = new LinearLayout.LayoutParams(80, 80);
        btnLP.gravity = Gravity.BOTTOM;
        btnLP.bottomMargin = 7;
        btnDelete.setLayoutParams(btnLP);
        btnDelete.setBackgroundResource(android.R.drawable.ic_delete);
        btnDelete.setOnClickListener(v -> ((LinearLayout)layout.getParent()).removeView(layout));

        if (this.currentTaskText != null)
            this.currentTaskText.removeTextChangedListener(this.textWatcher);

        txtText.addTextChangedListener(this.textWatcher);
        this.btnAddTask.setVisibility(View.GONE);
        this.currentTaskText = txtText;

        layout.addView(txtId);
        layout.addView(cbCompleted);
        layout.addView(txtText);
        layout.addView(btnDelete);

        return layout;
    }

    public void save(View view) {
        if (this.editTextTitle.getText().toString().length() == 0) {
            Toast.makeText(this, "Заголовок не должен быть пуст", Toast.LENGTH_LONG).show();
            return;
        }

        if (this.editTextBody.getText().toString().length() == 0) {
            Toast.makeText(this, "Текст заметки не должен быть пуст", Toast.LENGTH_LONG).show();
            return;
        }

        Note note = new Note();

        note.setId(this.id);
        note.setTitle(this.editTextTitle.getText().toString());
        note.setBody(this.editTextBody.getText().toString());

        note.setDate(this.date);

        int childCount = this.llTasks.getChildCount();
        List<Task> tasks = new ArrayList<>();

        for (int i = 0; i < childCount; i++) {
            LinearLayout ll = (LinearLayout) this.llTasks.getChildAt(i);

            String text = ((TextView) ll.getChildAt(2)).getText().toString();
            if (!text.isEmpty()) {
                String txtIdValue = (String) ((TextView) ll.getChildAt(0)).getText();
                Long id = null;
                if (!txtIdValue.isEmpty()) id = Long.parseLong(txtIdValue);

                boolean completed = ((CheckBox) ll.getChildAt(1)).isChecked();

                tasks.add(new Task(id, text, completed));
            }
        }

        note.setTasks(tasks);
        noteRepository.save(note);

        if (this.id == null)
            Toast.makeText(this, "Заметка создана", Toast.LENGTH_LONG).show();
        else
            Toast.makeText(this, "Заметка изменена", Toast.LENGTH_LONG).show();

        redirectToMainActivity();
    }

    public void delete(View view) {
        noteRepository.deleteById(this.id);

        Toast.makeText(this, "Заметка удалена", Toast.LENGTH_LONG).show();

        redirectToMainActivity();
    }

    public void redirectToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void showDatePickerDialog(View view) {
        this.datePickerDialog.show(getSupportFragmentManager(), "datePicker");
    }
}