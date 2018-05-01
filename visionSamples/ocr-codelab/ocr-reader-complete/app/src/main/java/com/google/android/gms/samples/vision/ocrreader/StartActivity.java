package com.google.android.gms.samples.vision.ocrreader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.Toast;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StartActivity extends AppCompatActivity {
    private static final int READ_REQUEST_CODE = 42;
    private static final int SPEECH_REQUEST_CODE = 0;

    @BindView(R.id.img_bt_take_photo)
    ImageButton takePhotoImageButton;
    @BindView(R.id.img_bt_open_file)
    ImageButton openFileImageButton;
    private TextToSpeech tts;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        takePhotoImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openOcrCaptureActivity();
            }
        });

        openFileImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileExplorerActivity();
            }
        });

        Intent intent = getIntent();
        if (intent == null) {
            throw new NullPointerException("intent not found!");
        } else {
            if (Objects.equals(intent.getAction(), MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA)) {
                openOcrCaptureActivity();
            }
        }

        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
    }

    private void openFileExplorerActivity() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("*/*");
        String[] mimetypes = {"application/pdf", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            try {
                readTheFile(data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == SPEECH_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);

            String spokenText = results.get(0);

            // Do something with spokenText
            Log.d("spokenText", "text: " + spokenText);
            if (spokenText.contains("camera")) {
                speakTheText(getString(R.string.voice_camera_opened));
                openOcrCaptureActivity();
            } else if (spokenText.contains("file") || spokenText.contains("pdf")) {
                speakTheText(getString(R.string.voice_file_explorer_opened));
                openFileExplorerActivity();
            } else {
                speakTheText(getString(R.string.voice_try_again));
            }
        }
    }

    private void openOcrCaptureActivity() {
        Intent intentA = new Intent(this, OcrCaptureActivity.class);
        startActivity(intentA);
    }

    @SuppressLint("StaticFieldLeak")
    private void readTheFile(Uri uri) throws IOException {
        if (uri == null) return;

        final StringBuilder result = new StringBuilder();

        String MIMEType = getMimeType(uri);

        switch (MIMEType) {
            case "pdf":
                new AsyncTask<Uri, Void, String>() {
                    @Override
                    protected String doInBackground(Uri... uris) {
                        Uri uri = uris[0];
                        InputStream inputStream = null;
                        try {
                            inputStream = getContentResolver().openInputStream(uri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            assert inputStream != null;
                            PdfReader reader = new PdfReader(inputStream);
                            int n = reader.getNumberOfPages();
                            for (int i = 0; i < n; i++) {
                                result.append(PdfTextExtractor.getTextFromPage(reader, i + 1).trim())
                                        .append("\n");
                            }

                            reader.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return result.toString();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            if (mToast != null) mToast.cancel();

                            mToast = Toast.makeText(StartActivity.this, s, Toast.LENGTH_LONG);
                            mToast.show();
                            speakTheText(s);
                        }
                    }
                }.execute(uri);
            case "txt":
                new AsyncTask<Uri, Void, String>() {
                    @Override
                    protected String doInBackground(Uri... uris) {
                        Uri uri = uris[0];
                        InputStream inputStream = null;
                        try {
                            inputStream = getContentResolver().openInputStream(uri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            assert inputStream != null;
                            BufferedReader reader = new BufferedReader(new InputStreamReader(
                                    inputStream, "UTF-8"));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                result.append(line);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        return result.toString();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        if (!TextUtils.isEmpty(s)) {
                            if (mToast != null) mToast.cancel();

                            mToast = Toast.makeText(StartActivity.this, s, Toast.LENGTH_LONG);
                            mToast.show();
                            speakTheText(s);
                        }
                    }
                }.execute(uri);
            default:
                Log.d("MimeType", "file extension not match!");
        }

    }

    private String getMimeType(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();

        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void speakTheText(String content) {
        tts.speak(content, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_about: {
                Intent aboutIntent = new Intent(this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            }

            case R.id.action_start_voice_trigger: {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

                startActivityForResult(intent, SPEECH_REQUEST_CODE);

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
}