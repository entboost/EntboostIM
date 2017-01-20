package com.entboost.voice;

import java.io.IOException;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class RealTimeAudioRecorder {
//	private final static int[] sampleRates = { 44100, 22050, 11025, 8000 };
//	private static RealTimeAudioRecorder result = null;
//
//	/* 解码器和编码器 */
//	aac.Encoder encoder;
//	aac.Decoder decoder;
//
//	public static RealTimeAudioRecorder getInstanse() {
//		int i = 3;
//		do {
//			result = new RealTimeAudioRecorder(AudioSource.MIC, sampleRates[i],
//					AudioFormat.CHANNEL_CONFIGURATION_MONO,
//					AudioFormat.ENCODING_PCM_16BIT);
//		} while ((--i >= 0)
//				&& !(result.getState() == RealTimeAudioRecorder.State.INITIALIZING));
//		return result;
//	}
//
//	/**
//	 * INITIALIZING : recorder is initializing; READY : recorder has been
//	 * initialized, recorder not yet started RECORDING : recording ERROR :
//	 * reconstruction needed STOPPED: reset needed
//	 */
//	public enum State {
//		INITIALIZING, READY, RECORDING, ERROR, STOPPED
//	};
//
//	private static final int TIMER_INTERVAL = 120;
//
//	private AudioRecord audioRecorder = null;
//
//	// Recorder state; see State
//	private State state;
//
//	// Number of channels, sample rate, sample size(size in bits), buffer size,
//	// audio source, sample size(see AudioFormat)
//	private short nChannels;
//	private int sRate;
//	private short bSamples;
//	private int bufferSize;
//	private int aSource;
//	private int aFormat;
//
//	// Number of frames written to file on each output
//	private int framePeriod;
//
//	// Number of bytes written to file after header(only in uncompressed mode)
//	// after stop() is called, this size is written to the header/data chunk in
//	// the wave file
//	private int payloadSize;
//
//	/**
//	 * 
//	 * Returns the state of the recorder in a RehearsalAudioRecord.State typed
//	 * object. Useful, as no exceptions are thrown.
//	 * 
//	 * @return recorder state
//	 */
//	public State getState() {
//		return state;
//	}
//
//	/*
//	 * 
//	 * Method used for recording.
//	 */
//	private AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
//		public void onPeriodicNotification(AudioRecord recorder) {
//			/* 计算每次送给编码器的frame的大小 = 1024 * 2 * 声道数 */
//			int in_size = aac.Encoder.frameSize(nChannels); // 2048
//			audioRecorder.read(encoder.inbuf, 0, in_size); // Fill buffer
//			Log.e("RealTimeAudioRecorder", "录音了一段");
//			/* 对录到的声音进行AAC编码 */
//			/* 编码后的数据在encoder.outbuf里 */
//			int out_size = encoder.encode(in_size);
//			if (out_size <= 0) {
//				/* 软件 未注册 */
//				Log.d("RealTimeAudioRecorder",
//						"please register the aac library !\n");
//			} else {
//				payloadSize += in_size;
//				Log.e("RealTimeAudioRecorder", new String(encoder.outbuf));
//			}
//
//		}
//
//		public void onMarkerReached(AudioRecord recorder) {
//			// NOT USED
//		}
//	};
//
//	/**
//	 * 
//	 * 
//	 * Default constructor
//	 * 
//	 * Instantiates a new recorder, in case of compressed recording the
//	 * parameters can be left as 0. In case of errors, no exception is thrown,
//	 * but the state is set to ERROR
//	 * 
//	 */
//	public RealTimeAudioRecorder(int audioSource, int sampleRate,
//			int channelConfig, int audioFormat) {
//		try {
//			if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) {
//				bSamples = 16;
//			} else {
//				bSamples = 8;
//			}
//
//			if (channelConfig == AudioFormat.CHANNEL_CONFIGURATION_MONO) {
//				nChannels = 1;
//			} else {
//				nChannels = 2;
//			}
//
//			aSource = audioSource;
//			sRate = sampleRate;
//			aFormat = audioFormat;
//
//			framePeriod = sampleRate * TIMER_INTERVAL / 1000;
//			bufferSize = framePeriod * 2 * bSamples * nChannels / 8;
//			if (bufferSize < AudioRecord.getMinBufferSize(sampleRate,
//					channelConfig, audioFormat)) { // Check to make sure
//				// buffer size is not
//				// smaller than the
//				// smallest allowed one
//				bufferSize = AudioRecord.getMinBufferSize(sampleRate,
//						channelConfig, audioFormat);
//				// Set frame period and timer interval accordingly
//				framePeriod = bufferSize / (2 * bSamples * nChannels / 8);
//				Log.w(RealTimeAudioRecorder.class.getName(),
//						"Increasing buffer size to "
//								+ Integer.toString(bufferSize));
//			}
//
//			audioRecorder = new AudioRecord(audioSource, sampleRate,
//					channelConfig, audioFormat, bufferSize);
//
//			if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED)
//				throw new Exception("AudioRecord initialization failed");
//
//			/* 注册本Library */
//			aac.Register r = new aac.Register();
//			r.setLicense("你好",
//					"0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF");
//			/* 创建编码器对象 */
//			encoder = new aac.Encoder();
//			if (!encoder.open(sampleRate, nChannels)) {
//				Log.d("RealTimeAudioRecorder", "failed to open encoder !\n");
//				encoder = null;
//			}
//
//			/* 创建解码器对象 */
//			decoder = new aac.Decoder();
//			if (!decoder.open()) {
//				Log.d("RealTimeAudioRecorder", "failed to open decoder !\n");
//				decoder = null;
//			}
//
//			audioRecorder.setRecordPositionUpdateListener(updateListener);
//			audioRecorder.setPositionNotificationPeriod(framePeriod);
//
//			state = State.INITIALIZING;
//		} catch (Exception e) {
//			if (e.getMessage() != null) {
//				Log.e(RealTimeAudioRecorder.class.getName(), e.getMessage());
//			} else {
//				Log.e(RealTimeAudioRecorder.class.getName(),
//						"Unknown error occured while initializing recording");
//			}
//			state = State.ERROR;
//		}
//	}
//
//	/**
//	 * 
//	 * Prepares the recorder for recording, in case the recorder is not in the
//	 * INITIALIZING state and the file path was not set the recorder is set to
//	 * the ERROR state, which makes a reconstruction necessary. In case
//	 * uncompressed recording is toggled, the header of the wave file is
//	 * written. In case of an exception, the state is changed to ERROR
//	 * 
//	 */
//	public void prepare() {
//		try {
//			if (state == State.INITIALIZING) {
//				if ((audioRecorder.getState() == AudioRecord.STATE_INITIALIZED)) {
//					state = State.READY;
//				} else {
//					Log.e(RealTimeAudioRecorder.class.getName(),
//							"prepare() method called on uninitialized recorder");
//					state = State.ERROR;
//				}
//			} else {
//				Log.e(RealTimeAudioRecorder.class.getName(),
//						"prepare() method called on illegal state");
//				release();
//				state = State.ERROR;
//			}
//		} catch (Exception e) {
//			if (e.getMessage() != null) {
//				Log.e(RealTimeAudioRecorder.class.getName(), e.getMessage());
//			} else {
//				Log.e(RealTimeAudioRecorder.class.getName(),
//						"Unknown error occured in prepare()");
//			}
//			state = State.ERROR;
//		}
//	}
//
//	/**
//	 * 
//	 * 
//	 * Releases the resources associated with this class, and removes the
//	 * unnecessary files, when necessary
//	 * 
//	 */
//	public void release() {
//		if (state == State.RECORDING) {
//			stop();
//		}
//		if (audioRecorder != null) {
//			audioRecorder.release();
//		}
//	}
//
//	/**
//	 * 
//	 * 
//	 * Resets the recorder to the INITIALIZING state, as if it was just created.
//	 * In case the class was in RECORDING state, the recording is stopped. In
//	 * case of exceptions the class is set to the ERROR state.
//	 * 
//	 */
//	public void reset() {
//		try {
//			if (state != State.ERROR) {
//				release();
//				audioRecorder = new AudioRecord(aSource, sRate, nChannels + 1,
//						aFormat, bufferSize);
//				state = State.INITIALIZING;
//			}
//		} catch (Exception e) {
//			Log.e(RealTimeAudioRecorder.class.getName(), e.getMessage());
//			state = State.ERROR;
//		}
//	}
//
//	/**
//	 * 
//	 * 
//	 * Starts the recording, and sets the state to RECORDING. Call after
//	 * prepare().
//	 * 
//	 */
//	public void start() {
//		if (state == State.READY) {
//			payloadSize = 0;
//			audioRecorder.startRecording();
//			state = State.RECORDING;
//		} else {
//			Log.e(RealTimeAudioRecorder.class.getName(),
//					"start() called on illegal state");
//			state = State.ERROR;
//		}
//	}
//
//	/**
//	 * 
//	 * 
//	 * Stops the recording, and sets the state to STOPPED. In case of further
//	 * usage, a reset is needed. Also finalizes the wave file in case of
//	 * uncompressed recording.
//	 * 
//	 */
//	public void stop() {
//		if (state == State.RECORDING) {
//			audioRecorder.stop();
//			state = State.STOPPED;
//		} else {
//			Log.e(RealTimeAudioRecorder.class.getName(),
//					"stop() called on illegal state");
//			state = State.ERROR;
//		}
//	}
//
//	/*
//	 * 
//	 * Converts a byte[2] to a short, in LITTLE_ENDIAN format
//	 */
//	private short getShort(byte argB1, byte argB2) {
//		return (short) (argB1 | (argB2 << 8));
//	}
//
//	public static void play(String filePathName) {
//		MediaPlayer player = new MediaPlayer();
//		try {
//			player.setDataSource(filePathName);
//			player.prepare();
//			player.start();
//		} catch (IllegalArgumentException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IllegalStateException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
