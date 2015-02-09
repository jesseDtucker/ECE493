package jetucker.cmput293assignment1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;


/**
 * Simple activity for setting the mean and median filter size.
 */
public class SettingsActivity extends ActionBarActivity
{

    private final int MIN_FILTER_SIZE = 3;
    private final int MAX_FILTER_SIZE = 25;

    private Integer m_newValue = null;

    enum SelectedSetting
    {
        MEDIAN,
        MEAN,
        UNDO_LIMIT
    }

    private SelectedSetting m_selectedSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button medianBtn = (Button) findViewById(R.id.medianFilterSizeField);
        Button meanBtn = (Button) findViewById(R.id.meanFilterSizeField);
        Button undoBtn = (Button) findViewById(R.id.undoLimitSizeField);

        Integer medianFilterSize = Settings.GetMedianFilterSize(getApplicationContext());
        Integer meanFilterSize = Settings.GetMeanFilterSize(getApplicationContext());
        Integer undoLimit = Settings.GetUndoLimit(getApplicationContext());

        medianBtn.setText(medianFilterSize.toString());
        meanBtn.setText(meanFilterSize.toString());
        undoBtn.setText(undoLimit.toString());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        return false;
    }

    public void MedianFilterSelected(View v)
    {
        m_selectedSetting = SelectedSetting.MEDIAN;
        StartSelectNumber(Settings.GetMedianFilterSize(getApplicationContext()), true);
    }

    public void MeanFilterSelected(View v)
    {
        m_selectedSetting = SelectedSetting.MEAN;
        StartSelectNumber(Settings.GetMeanFilterSize(getApplicationContext()), true);
    }

    public void UndoFilterSelected(View v)
    {
        m_selectedSetting = SelectedSetting.UNDO_LIMIT;
        StartSelectNumber(Settings.GetUndoLimit(getApplicationContext()), false);
    }

    /**
     * Set the value the user selected to the setting they selected
     */
    private void SetSelectedValue()
    {
        Util.Assert(m_newValue != null);

        switch (m_selectedSetting)
        {
            case MEAN:
                Button meanBtn = (Button) findViewById(R.id.meanFilterSizeField);
                Settings.SetMeanFilterSize(getApplicationContext(), m_newValue);
                meanBtn.setText(m_newValue.toString());
                break;
            case MEDIAN:
                Button medianBtn = (Button) findViewById(R.id.medianFilterSizeField);
                Settings.SetMedianFilterSize(getApplicationContext(), m_newValue);
                medianBtn.setText(m_newValue.toString());
                break;
            case UNDO_LIMIT:
                Button undoLimitBtn = (Button) findViewById(R.id.undoLimitSizeField);
                Settings.SetUndoLimit(getApplicationContext(), m_newValue);
                undoLimitBtn.setText(m_newValue.toString());
                break;
            default:
                Util.Fail("Unknown value to set");
        }

        m_newValue = null;
    }

    /**
     * Request an odd number within the valid range from the user.
     *
     * @param currentValue the current value of the field
     */
    private void StartSelectNumber(int currentValue, final boolean mustBeOdd)
    {
        LayoutInflater inflater = getLayoutInflater();
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SettingsActivity.this);
        View view = inflater.inflate(R.layout.filter_size_selector, null);
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.seek_bar);
        final TextView seekBarVal = (TextView) view.findViewById(R.id.seek_bar_val);
        seekBarVal.setText(((Integer) (currentValue)).toString());

        seekBar.setMax(MAX_FILTER_SIZE - MIN_FILTER_SIZE);
        seekBar.setProgress(currentValue);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                if (fromUser)
                {
                    // set the new value and round off to an odd number
                    m_newValue = MIN_FILTER_SIZE + progress;
                    if(mustBeOdd)
                    {
                        m_newValue = m_newValue % 2 == 0 ? m_newValue + 1 : m_newValue;
                    }
                    seekBarVal.setText(m_newValue.toString());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                // Do nothing
            }
        });

        AlertDialog dialog = dialogBuilder.setTitle("Select a filter size:")
                .setView(view)
                .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        SetSelectedValue();
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        m_newValue = null;
                    }
                }).create();

        dialog.show();
    }
}
