package com.example.converter;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.converter.database.DatabaseHelper;
import com.example.converter.database.NoteRepository;
import com.example.converter.dto.NoteDto;
import com.example.converter.model.Note;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinearLayout layoutNotes = findViewById(R.id.layoutNotes);

        DatabaseHelper databaseHelper = new DatabaseHelper(this);

        NoteRepository noteRepository = databaseHelper.getNoteRepository();

        List<NoteDto> notes = noteRepository.findAllNoteDto();

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(30, 30, 30, 30);

        for (NoteDto note : notes) {
            layoutNotes.addView(this.createNoteView(note), layoutParams);
        }
    }

    private View createNoteView(NoteDto note) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(30, 30, 30, 30);
        layout.setOnClickListener((view) -> this.redirectToNoteActivity(note.getId()));

        LinearLayout titleLayout = new LinearLayout(this);

        TextView title = new TextView(this);
        title.setText(note.getTitle());
        title.setTextSize(title.getTextSize() * .5f);
        titleLayout.addView(title);
        title.setTextColor(Color.WHITE);


        TextView date = new TextView(this);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long days = (calendar.getTime().getTime() - note.getDate().getTime()) / 1000 / 60 / 60 / 24;
        String prefix = days > 0 ? "Прошло: " : "Осталось: ";
        String postfix = "дней";
        long daysAbs = Math.abs(days);
        if (daysAbs % 100 < 10 || daysAbs % 100 > 20) {
            if (daysAbs % 10 == 1) postfix = "день";
            else if (daysAbs % 10 >= 2 && daysAbs % 10 <= 4) postfix = "дня";
        }
        date.setText(prefix + daysAbs + " " + postfix);
        date.setTextColor(Color.WHITE);

        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.completed);
        if (days < -5) {
            if (!note.getCountCompleted().equals(note.getCountAll()))
                imageView.setImageResource(R.drawable.island);
            layout.setBackgroundResource(R.drawable.note_calmness);
        } else if (days > 0) {
            if (!note.getCountCompleted().equals(note.getCountAll()))
                imageView.setImageResource(R.drawable.dead);
            layout.setBackgroundResource(R.drawable.note_dead);
        } else {
            if (!note.getCountCompleted().equals(note.getCountAll()))
                imageView.setImageResource(R.drawable.fire);
            layout.setBackgroundResource(R.drawable.note);
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(60, 60);
        layoutParams.setMarginStart(10);
        imageView.setLayoutParams(layoutParams);

        titleLayout.addView(imageView);

        TextView body = new TextView(this);
        body.setText(note.getBody());
        body.setTextColor(Color.WHITE);

        TextView txtCount = new TextView(this);
        txtCount.setText("Выполнено: " + note.getCountCompleted() + "/" + note.getCountAll());
        txtCount.setTextColor(Color.WHITE);

        layout.addView(titleLayout);
        layout.addView(body);
        layout.addView(date);
        layout.addView(txtCount);

        return layout;
    }

    public void redirectToNoteActivity(View view) {
        this.redirectToNoteActivity(-1L);
    }

    public void redirectToNoteActivity(Long id) {
        Intent intent = new Intent(this, NoteActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }
}
