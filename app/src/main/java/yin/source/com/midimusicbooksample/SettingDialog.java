package yin.source.com.midimusicbooksample;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import yin.source.com.midimusicbook.midi.baseBean.MidiOptions;

/**
 * Created by Yin on 2018/5/26.
 */
public class SettingDialog extends DialogFragment implements View.OnClickListener {

    public static final String MIDI_OPTIONS = "midiOptions";
    private CheckBox checkShowBarNumber;
    private CheckBox checkShowNoteName;

    private SettingCallback settingCallback;

    public static SettingDialog newInstance(Context context, MidiOptions midiOptions) {
        SettingDialog settingDialog = new SettingDialog();
        Bundle bundle = new Bundle();
        bundle.putSerializable(MIDI_OPTIONS, midiOptions);
        settingDialog.setArguments(bundle);
        return settingDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_dialog, container);
        checkShowBarNumber = (CheckBox) view.findViewById(R.id.check_show_bar_number);
        checkShowNoteName = (CheckBox) view.findViewById(R.id.check_show_note_name);
        TextView positive = view.findViewById(R.id.positive);

        Bundle arguments = getArguments();
        MidiOptions midiOptions = (MidiOptions) arguments.getSerializable(MIDI_OPTIONS);
        if (midiOptions != null) {
            checkShowBarNumber.setChecked(midiOptions.showMeasures);
            checkShowNoteName.setChecked(midiOptions.showNoteLetters == 1);
        }

        positive.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.positive:
                Bundle arguments = getArguments();
                MidiOptions midiOptions = (MidiOptions) arguments.getSerializable(MIDI_OPTIONS);
                if (midiOptions != null) {
                    midiOptions.showMeasures = checkShowBarNumber.isChecked();
                    midiOptions.showNoteLetters = checkShowNoteName.isChecked() ? 1 : 0;
                }
                if (settingCallback != null) {
                    settingCallback.onSettingFinish(midiOptions);
                }
                dismiss();
                break;
        }
    }

    public SettingCallback getSettingCallback() {
        return settingCallback;
    }

    public void setSettingCallback(SettingCallback settingCallback) {
        this.settingCallback = settingCallback;
    }

    public interface SettingCallback {
        void onSettingFinish(MidiOptions midiOptions);
    }
}
