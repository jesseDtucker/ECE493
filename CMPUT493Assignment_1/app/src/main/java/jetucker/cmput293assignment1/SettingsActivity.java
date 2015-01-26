package jetucker.cmput293assignment1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;


/**
 * Simple activity for setting the mean and median filter size.
 */
public class SettingsActivity extends ActionBarActivity {

    private final int MIN_FILTER_SIZE = 3;
    private final int MAX_FILTER_SIZE = 25;

    private Integer m_newValue = null;
    private boolean m_isSettingMedian = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button medianBtn = (Button) findViewById(R.id.medianFilterSizeField);
        Button meanBtn = (Button) findViewById(R.id.meanFilterSizeField);

        Integer medianFilterSize = Settings.GetMedianFilterSize(getApplicationContext());
        Integer meanFilterSize = Settings.GetMeanFilterSize(getApplicationContext());

        medianBtn.setText(medianFilterSize.toString());
        meanBtn.setText(meanFilterSize.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return false;
    }

    public void MedianFilterSelected(View v)
    {
        m_isSettingMedian = true;
        StartSelectOddNumber(Settings.GetMedianFilterSize(getApplicationContext()));
    }

    public void MeanFilterSelected(View v)
    {
        m_isSettingMedian = false;
        StartSelectOddNumber(Settings.GetMeanFilterSize(getApplicationContext()));
    }

    /**
     * Set the value the user selected to the setting they selected
     */
    private void SetSelectedValue()
    {
        Util.Assert(m_newValue != null);

        if(m_isSettingMedian)
        {
            Button medianBtn = (Button) findViewById(R.id.medianFilterSizeField);
            Settings.SetMedianFilterSize(getApplicationContext(), m_newValue);
            medianBtn.setText(m_newValue.toString());
        }
        else
        {
            Button meanBtn = (Button) findViewById(R.id.meanFilterSizeField);
            Settings.SetMeanFilterSize(getApplicationContext(), m_newValue);
            meanBtn.setText(m_newValue.toString());
        }

        m_newValue = null;
    }

    /**
     * Request an odd number within the valid range from the user.
     * @param currentValue
     */
    private void StartSelectOddNumber(int currentValue)
    {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        View view = inflater.inflate(R.layout.filter_size_selector, null);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        final TextView seekBarVal = (TextView) view.findViewById(R.id.seek_bar_val);
        seekBarVal.setText(((Integer)(currentValue)).toString());

        seekBar.setMax(MAX_FILTER_SIZE - MIN_FILTER_SIZE);
        seekBar.setProgress(currentValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser)
                {
                    // set the new value and round off to an odd number
                    m_newValue = MIN_FILTER_SIZE + progress;
                    m_newValue = m_newValue % 2 == 0 ? m_newValue + 1 : m_newValue;
                    seekBarVal.setText(m_newValue.toString());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });

        AlertDialog dialog = dialogBuilder.setTitle("Select a filter size:")
                .setView(view)
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        SetSelectedValue();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        m_newValue = null;
                    }
                }).create();

        dialog.show();
    }
}
