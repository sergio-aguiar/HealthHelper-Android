package pt.ua.aguiar.sergio.healthhelper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import pt.ua.aguiar.sergio.healthhelper.Extras.Structures.HealthHelperListenedResult;
import pt.ua.aguiar.sergio.healthhelper.Extras.ListenerInterfaces.OnListenedResultChangeListener;

public class HealthHelperSignInPage extends Activity {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    private ImageButton appBackButton;
    private EditText emailText;
    private EditText passwordText;
    private Button signInButton;

    private HealthHelperListenedResult listenedResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.health_helper_sign_in_page);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        findViewById(R.id.sign_in_page_constraint_layout).setBackgroundResource(R.drawable.tween_gradient_animation);

        appBackButton = findViewById(R.id.top_app_bar_back);

        appBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), HealthHelperMainPage.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        emailText = findViewById(R.id.sign_in_email_input_box);
        passwordText = findViewById(R.id.sign_in_password_input_box);

        signInButton = findViewById(R.id.form_sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                emailText.getText().toString();
                if(emailText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Email must not be empty!", Toast.LENGTH_LONG).show();
                } else if(passwordText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), "Password must not be empty!", Toast.LENGTH_LONG).show();
                } else {
                    firebaseAuth.signInWithEmailAndPassword(emailText.getText().toString(), passwordText.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if(task.isSuccessful()) {
                                        listenedResult.setResult(true);
                                    } else {
                                        listenedResult.setResult(false);
                                    }
                                }
                            });
                }
            }
        });

        listenedResult = new HealthHelperListenedResult();
        listenedResult.setChangeListener(new OnListenedResultChangeListener() {
            @Override
            public void onListenedResultChangeListener(HealthHelperListenedResult result) {
                if(result.isResultObtained() && result.getResult()) {
                    startActivity(new Intent(getApplicationContext(), HealthHelperMainMenuPage.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Sign in failed!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), HealthHelperMainPage.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        AnimationDrawable gradientAnimation = (AnimationDrawable) findViewById(R.id.sign_in_page_constraint_layout).getBackground();
        gradientAnimation.setEnterFadeDuration(30);
        gradientAnimation.setExitFadeDuration(2000);
        gradientAnimation.setVisible(true, true);
        gradientAnimation.start();
    }
}
