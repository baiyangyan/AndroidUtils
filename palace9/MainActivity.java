package hlh.palace9;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Palace9View viewById;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewById = findViewById(R.id.palace);
        viewById.setPW("123");
        viewById.setInterface(new Palace9View.RoundInterface() {
            @Override
            public void failure(String string) {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void success() {
                Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
