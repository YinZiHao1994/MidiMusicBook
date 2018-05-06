package yin.source.com.midimusicbooksample;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.zip.CRC32;

import yin.source.com.midimusicbook.midi.baseBean.MidiFile;
import yin.source.com.midimusicbook.midi.baseBean.MidiOptions;
import yin.source.com.midimusicbook.midi.musicBook.ClefSymbol;
import yin.source.com.midimusicbook.midi.musicBook.MidiPlayer;
import yin.source.com.midimusicbook.midi.musicBook.Piano;
import yin.source.com.midimusicbook.midi.musicBook.SheetMusic;
import yin.source.com.midimusicbook.midi.musicBook.TimeSigSymbol;
import yin.source.com.midimusicbook.utils.IOUtil;

public class MainActivity extends AppCompatActivity {

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final String MidiTitleID = "MidiTitleID";
    public static final int settingsRequestCode = 1;
    // Debugging
    private static final String TAG = "BluetoothChat";
    private static final boolean D = true;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 2;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 3;
    private static final int REQUEST_ENABLE_BT = 4;
    private MidiPlayer player; /* The play/stop/rewind toolbar */
    private Piano piano; /* The piano at the top */ // 顶部的钢琴
    private SheetMusic sheet; /* The sheet music */ // 乐谱
    private MidiFile midifile; /* The midi file to play */ // 需要播放的midi文件
    private MidiOptions options; /* The options for sheet music and sound */ // 乐谱和声音的选项
    private long midiCRC; /* CRC of the midi bytes */ // midi字节数组的循环校验码

    // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

    // Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    private String PIANOORDER =
            "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000000000000000" +
                    "00000000";
    private int PRELEFTPOSITION = -1;
    private int PRERIGHTPOSITION = -1;

    /**
     * Create this SheetMusicActivity. The Intent should have two parameters: -
     * data: The uri of the midi file to open. - MidiTitleID: The title of the
     * song (String)
     * 创建这个乐谱Activity.传过来的intent必须有两个参数：
     * data:将要打开的midi文件的uri
     * MidiTitleID:歌曲的标题
     */
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        player = (MidiPlayer) findViewById(R.id.midi_player);
        piano = (Piano) findViewById(R.id.piano);
        sheet = (SheetMusic) findViewById(R.id.sheet);
//        player.SetPiano(piano);
        player.addMidiPlayerCallback(new MidiPlayer.MidiPlayerCallback() {
            @Override
            public void onSettingMenuButtonClick() {
                openOptionsMenu();
            }

            @Override
            public void onSheetNeedShadeNotes(int currentPulseTime, int prevPulseTime, int gradualScroll) {

            }

            @Override
            public void onPianoNeedShadeNotes(int currentPulseTime, int prevPulseTime) {

            }
        });
        piano.setPianoListener(new Piano.PianoListener() {
            @Override
            public void pianoKey(int position, int channel) {
                sendPianoMessage(position - 24, channel);
            }
        });


        ClefSymbol.LoadImages(this);
        TimeSigSymbol.LoadImages(this);


        File fileFromAssets = FileManagerUtils.getFileFromAssets("piano_guide.mid", this);
        midifile = new MidiFile(fileFromAssets, fileFromAssets.getName());

        // Initialize the settings (MidiOptions).
        // If previous settings have been saved, used those
        options = new MidiOptions(midifile);
        CRC32 crc = new CRC32();
        byte[] byteDataByFile = IOUtil.getByteDataByFile(fileFromAssets);
        crc.update(byteDataByFile);
        midiCRC = crc.getValue();
        SharedPreferences settings = getPreferences(0);
        options.scrollVert = settings.getBoolean("scrollVert", false);
        options.shade1Color = settings.getInt("shade1Color", options.shade1Color);
        options.shade2Color = settings.getInt("shade2Color", options.shade2Color);
        options.showPiano = settings.getBoolean("showPiano", true);
        String json = settings.getString("" + midiCRC, null);
        MidiOptions savedOptions = MidiOptions.fromJson(json);
        if (savedOptions != null) {
            options.merge(savedOptions);
        }
        createSheetMusic(options);
    }


    private void sendPianoMessage(int position, int channel) {
        Log.i("SheetMusicActivity", position + "");
        if (channel == 1) {
            if (PRELEFTPOSITION != -1)
                PIANOORDER = PIANOORDER.substring(0, PRELEFTPOSITION * 2 + 1) + "0" +
                        PIANOORDER.substring(PRELEFTPOSITION * 2 + 2);
            PRELEFTPOSITION = position;
            PIANOORDER = PIANOORDER.substring(0, PRELEFTPOSITION * 2 + 1) + "1" +
                    PIANOORDER.substring(PRELEFTPOSITION * 2 + 2);
        } else {
            if (PRERIGHTPOSITION != -1)
                PIANOORDER = PIANOORDER.substring(0, PRERIGHTPOSITION * 2) + "0" +
                        PIANOORDER.substring(PRERIGHTPOSITION * 2 + 1);
            PRERIGHTPOSITION = position;
            PIANOORDER = PIANOORDER.substring(0, PRERIGHTPOSITION * 2) + "1" +
                    PIANOORDER.substring(PRERIGHTPOSITION * 2 + 1);

        }
    }


    /**
     * Create the SheetMusic view with the given options
     */
    private void createSheetMusic(MidiOptions options) {
        if (!options.showPiano) {
            piano.setVisibility(View.GONE);
        } else {
            piano.setVisibility(View.VISIBLE);
        }
        sheet.init(midifile, options);
        sheet.setPlayer(player);
        piano.SetMidiFile(midifile, options, player);
        piano.SetShadeColors(options.shade1Color, options.shade2Color);
        player.SetMidiFile(midifile, options, sheet);
        sheet.callOnDraw();
    }

    /**
     * Always display this activity in landscape mode.
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    /**
     * When the menu button is pressed, initialize the menus.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (player != null) {
            player.Pause();
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sheet_menu, menu);
        return true;
    }

    /**
     * Callback when a menu item is selected. - Choose Song : Choose a new song
     * - Song Settings : Adjust the sheet music and sound options - Save As
     * Images: Save the sheet music as PNG images - Help : Display the HTML help
     * screen
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_song:
                chooseSong();
                return true;
            case R.id.song_settings:
                changeSettings();
                return true;
            case R.id.save_images:
                showSaveImagesDialog();
                return true;
            case R.id.help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * To choose a new song, simply finish this activity. The previous activity
     * is always the ChooseSongActivity.
     */
    private void chooseSong() {
        this.finish();
    }

    /**
     * To change the sheet music options, start the SettingsActivity. Pass the
     * current MidiOptions as a parameter to the Intent. Also pass the 'default'
     * MidiOptions as a parameter to the Intent. When the SettingsActivity has
     * finished, the onActivityResult() method will be called.
     */
    private void changeSettings() {
        MidiOptions defaultOptions = new MidiOptions(midifile);
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.settingsID, options);
        intent.putExtra(SettingsActivity.defaultSettingsID, defaultOptions);
        startActivityForResult(intent, settingsRequestCode);
    }

    /* Show the "Save As Images" dialog */
    private void showSaveImagesDialog() {
        LayoutInflater inflator = LayoutInflater.from(this);
        final View dialogView = inflator.inflate(R.layout.save_images_dialog,
                null);
        final EditText filenameView = (EditText) dialogView
                .findViewById(R.id.save_images_filename);
        filenameView.setText(midifile.getFileName().replace("_", " "));
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save As Images");
        builder.setView(dialogView);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface builder, int whichButton) {
                saveAsImages(filenameView.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface builder, int whichButton) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /* Save the current sheet music as PNG images. */
    private void saveAsImages(String name) {
        String filename = name;
        try {
            filename = URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        if (!options.scrollVert) {
            options.scrollVert = true;
            createSheetMusic(options);
        }
        try {
            int numpages = sheet.GetTotalPages();
            for (int page = 1; page <= numpages; page++) {
                Bitmap image = Bitmap.createBitmap(SheetMusic.PageWidth + 40,
                        SheetMusic.PageHeight + 40, Bitmap.Config.ARGB_8888);
                Canvas imageCanvas = new Canvas(image);
                sheet.DrawPage(imageCanvas, page);
                File path = Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES
                                + "/MidiSheetMusic");
                File file = new File(path, "" + filename + page + ".png");
                path.mkdirs();
                OutputStream stream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.PNG, 0, stream);
                image = null;
                stream.close();

                // Inform the media scanner about the file
                MediaScannerConnection.scanFile(this,
                        new String[]{file.toString()}, null, null);
            }
        } catch (IOException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Error saving image to file "
                    + Environment.DIRECTORY_PICTURES + "/MidiSheetMusic/"
                    + filename + ".png");
            builder.setCancelable(false);
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (NullPointerException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Ran out of memory while saving image to file "
                    + Environment.DIRECTORY_PICTURES + "/MidiSheetMusic/"
                    + filename + ".png");
            builder.setCancelable(false);
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    /**
     * Show the HTML help screen.
     */
    private void showHelp() {
//        Intent intent = new Intent(this, HelpActivity.class);
//        startActivity(intent);
    }

    /**
     * This is the callback when the SettingsActivity is finished. Get the
     * modified MidiOptions (passed as a parameter in the Intent). Save the
     * MidiOptions. The key is the CRC checksum of the midi data, and the value
     * is a JSON dump of the MidiOptions. Finally, re-create the SheetMusic View
     * with the new options.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        if (D) Log.d(TAG, "onActivityResult " + resultCode);

        if (requestCode != settingsRequestCode) {
            return;
        }
        options = (MidiOptions) intent
                .getSerializableExtra(SettingsActivity.settingsID);

        // Check whether the default instruments have changed.
        for (int i = 0; i < options.instruments.length; i++) {
            if (options.instruments[i] != midifile.getTracks().get(i)
                    .getInstrument()) {
                options.useDefaultInstruments = false;
            }
        }
        // Save the options.
        SharedPreferences.Editor editor = getPreferences(0).edit();
        editor.putBoolean("scrollVert", options.scrollVert);
        editor.putInt("shade1Color", options.shade1Color);
        editor.putInt("shade2Color", options.shade2Color);
        editor.putBoolean("showPiano", options.showPiano);
        String json = options.toJson();
        if (json != null) {
            editor.putString("" + midiCRC, json);
        }
        editor.commit();

        // Recreate the sheet music with the new options
        createSheetMusic(options);
    }

    /**
     * When this activity resumes, redraw all the views
     */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * When this activity pauses, stop the music
     */
    @Override
    protected void onPause() {
        if (player != null) {
            player.Pause();
        }
        super.onPause();
    }
}
