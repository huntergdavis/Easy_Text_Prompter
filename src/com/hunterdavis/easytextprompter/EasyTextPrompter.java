package com.hunterdavis.easytextprompter;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class EasyTextPrompter extends Activity {

	// globals
	int SELECT_TEXT = 55;
	int fileLines = 0;
	String filePath = null;
	int currentProgress = 0;
	int currentLineNumber = 0;
	int currentLineWord = 0;
	int stopTimer = 0;

	private RefreshHandler mRedrawHandler = new RefreshHandler();

	public void SetTextFromPositionAndRefreshTimer() {
		if (stopTimer == 1) {
			stopTimer = 0;
			return;
		}
		SeekBar onlySeekBar = (SeekBar) findViewById(R.id.progressbar);
		int progress = onlySeekBar.getProgress();
		setTextFromLineNumber(progress);
		int delayMillis = getDelayFromSpinner();
		mRedrawHandler.scheduleRefreshIn(delayMillis);
	}

	class RefreshHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {

			// do an update here
			SetTextFromPositionAndRefreshTimer();

		}

		public void scheduleRefreshIn(long delayMillis) {

			this.removeMessages(0);

			sendMessageDelayed(obtainMessage(0), delayMillis);

		}

	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// load image button
		OnClickListener imageListener = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				// in onCreate or any event where your want the user to
				Intent intent = new Intent(v.getContext(), FileDialog.class);
				intent.putExtra(FileDialog.START_PATH, "/sdcard");
				startActivityForResult(intent, SELECT_TEXT);
			}
		};

		TextView loadb = (TextView) findViewById(R.id.filenametext);
		loadb.setOnClickListener(imageListener);

		OnClickListener pauseListener = new OnClickListener() {
			public void onClick(View v) {
				// do something when the button is clicked

				PlayOrPause(v.getContext());
			}
		};
		Button pause = (Button) findViewById(R.id.pauseButton);
		pause.setOnClickListener(pauseListener);

		// set up our three spinners

		Spinner spinner = (Spinner) findViewById(R.id.chunkspin);
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.chunks, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());
		spinner.setSelection(3);

		spinner = (Spinner) findViewById(R.id.speedspin);
		adapter = ArrayAdapter.createFromResource(this, R.array.speed,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());
		spinner.setSelection(3);

		spinner = (Spinner) findViewById(R.id.fontsizes);
		adapter = ArrayAdapter.createFromResource(this, R.array.fonts,
				android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new MyUnitsOnItemSelectedListener());
		spinner.setSelection(3);

		// implement a seekbarchangelistener for this class
		SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if (currentProgress != progress) {
					currentProgress = progress;
					currentLineWord = 0;
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				// only update our progress here after we're done tracking!
				if (filePath != null) {
					setTextFromLineNumber(currentProgress);
				}
			}
		};

		SeekBar onlySeekBar = (SeekBar) findViewById(R.id.progressbar);
		onlySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

		// Look up the AdView as a resource and load a request.
		AdView adView = (AdView) this.findViewById(R.id.adView);
		adView.loadAd(new AdRequest());

	} // end of oncreate

	// set up the listener class for spinner
	class MyUnitsOnItemSelectedListener implements OnItemSelectedListener {
		public void onItemSelected(AdapterView<?> parent, View view, int pos,
				long id) {
			// switchPauseToPlay();
		}

		public void onNothingSelected(AdapterView<?> parent) {
			// Do nothing.
		}
	}

	public void onActivityResult(final int requestCode, int resultCode,
			final Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_TEXT) {
				int localFileLines = 0;
				String localfilePath = data
						.getStringExtra(FileDialog.RESULT_PATH);
				TextView loadb = (TextView) findViewById(R.id.filenametext);
				BufferedReader br;
				try {
					br = new BufferedReader(new InputStreamReader(
							new DataInputStream(new FileInputStream(
									localfilePath))));
				} catch (FileNotFoundException e) {
					return;
				}
				String readLine = null;

				try {
					while ((readLine = br.readLine()) != null) {
						localFileLines++;
					}
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}

				currentProgress = 0;
				currentLineNumber = 0;
				currentLineWord = 0;
				fileLines = localFileLines;
				filePath = localfilePath;
				// set the filename txt
				loadb.setText(filePath);
				SeekBar onlySeekBar = (SeekBar) findViewById(R.id.progressbar);
				onlySeekBar.setProgress(0);
				onlySeekBar.setMax(fileLines);
				EditText onlyEdit = (EditText) findViewById(R.id.outputText);
				onlyEdit.setText("");

			}
		} else if (resultCode == RESULT_CANCELED) {
		}
	}

	public void setTextFromLineNumber(int lineNumber) {

		EditText ourText = (EditText) findViewById(R.id.outputText);
		ourText.setTextSize(getFontSize());
		String ourLine = getLineFromNumber(lineNumber);

		currentLineNumber = lineNumber;
		SeekBar onlySeekBar = (SeekBar) findViewById(R.id.progressbar);
		int progress = onlySeekBar.getProgress();

		if (lineNumber == 0) {
			ourText.setText("");
			progress++;
			onlySeekBar.setProgress(progress);
			return;
		}

		Pattern p = Pattern.compile(" +");
		String[] words = p.split(ourLine);
		int chunkSize = getSelectedWordCount();
		int numberWords = words.length;
		// currentLineWord

		if (chunkSize > numberWords) {
			ourText.setText(ourLine);
			currentLineWord = 0;
			progress++;
			onlySeekBar.setProgress(progress);
			currentLineNumber++;
			return;
		}

		int retwordsize = 0;
		int oldcurrentLineWord = currentLineWord;
		if ((chunkSize + currentLineWord) >= numberWords) {
			retwordsize = numberWords - currentLineWord;
			currentLineWord = 0;
			progress++;
			onlySeekBar.setProgress(progress);
			currentLineNumber++;
		} else {
			retwordsize = chunkSize;
			currentLineWord = currentLineWord + chunkSize;
		}

		String retText = "";

		for (int i = 0; i < retwordsize; i++) {
			retText += words[i + oldcurrentLineWord];
			if (i != (retwordsize - 1)) {
				retText += " ";
			} 
		}

		ourText.setText(retText);

	}

	public int getFontSize() {
		Spinner spinner = (Spinner) findViewById(R.id.fontsizes);

		return 14 + 4 * spinner.getSelectedItemPosition();
	}

	public int getSelectedWordCount() {
		Spinner spinner = (Spinner) findViewById(R.id.chunkspin);
		return spinner.getSelectedItemPosition() + 1;
	}

	public int getDelayFromSpinner() {
		Spinner spinner = (Spinner) findViewById(R.id.speedspin);
		return (7 - spinner.getSelectedItemPosition()) * 500;

	}

	public String getLineFromNumber(int lineNumber) {
		BufferedReader br;
		int localfileLines = 0;
		try {
			br = new BufferedReader(new InputStreamReader(new DataInputStream(
					new FileInputStream(filePath))));
		} catch (FileNotFoundException e) {
			return "";
		}
		String readLine = null;

		try {
			while ((readLine = br.readLine()) != null) {
				localfileLines++;
				if (localfileLines == lineNumber) {
					return readLine;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";

	}

	public void PlayOrPause(Context context) {
		Button pause = (Button) findViewById(R.id.pauseButton);
		String ourText = (String) pause.getText();
		if (ourText.equals("Play")) {
			if (filePath == null) {
				Toast.makeText(context, "Please Select A File",
						Toast.LENGTH_SHORT).show();
				return;
			}

			switchPlayToPause();

		} else {

			switchPauseToPlay();
		}

	}

	public void switchPlayToPause() {
		Button pause = (Button) findViewById(R.id.pauseButton);
		String ourText = (String) pause.getText();
		if (ourText.equals("Play")) {
			pause.setText("Pause");

			SetTextFromPositionAndRefreshTimer();
		}
	}

	public void switchPauseToPlay() {
		Button pause = (Button) findViewById(R.id.pauseButton);
		String ourText = (String) pause.getText();
		if (ourText.equals("Pause")) {
			pause.setText("Play");
			stopTimer = 1;
		}
	}

} // end of file