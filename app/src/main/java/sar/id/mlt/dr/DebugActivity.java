package sar.id.mlt.dr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class DebugActivity extends Activity {

    private static final Map<String, String> exceptionMap = Map.of(
            "StringIndexOutOfBoundsException", "Invalid string operation\n",
            "IndexOutOfBoundsException", "Invalid list operation\n",
            "ArithmeticException", "Invalid arithmetical operation\n",
            "NumberFormatException", "Invalid toNumber block operation\n",
            "ActivityNotFoundException", "Invalid intent operation\n"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SpannableStringBuilder formattedMessage = new SpannableStringBuilder();
        String errorMessage = Objects.toString(
                getIntent() != null ? getIntent().getStringExtra("error") : "", ""
        );

        if (!errorMessage.isEmpty()) {
            String[] split = errorMessage.split("\n", -1);

            String friendlyMessage = Objects.requireNonNullElse(
                    exceptionMap.getOrDefault(split[0], ""), ""
            );
            formattedMessage.append(friendlyMessage);

            IntStream.range(1, split.length)
                    .forEach(i -> formattedMessage.append(split[i]).append("\n"));

        } else {
            formattedMessage.append("No error message available.");
        }

        setTitle(getTitle() + " Crashed");

        TextView errorView = new TextView(this);
        errorView.setText(formattedMessage);
        errorView.setTextIsSelectable(true);

        ScrollView vscroll = new ScrollView(this);
        HorizontalScrollView hscroll = new HorizontalScrollView(this);

        vscroll.addView(errorView);
        hscroll.addView(vscroll);

        setContentView(hscroll);
    }
}