/*
 *   Copyright 2006 Martin B. Smith
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.mbs3.jkaraoke;

import java.io.*;
import javazoom.jlgui.basicplayer.*;

import java.util.*;

public class MusicPlayer implements Runnable, BasicPlayerListener {

	private InputStream audioFileInputStream;

	private InputStream cdgFileInputStream;

	BufferedInputStream cdfFileInputStream;

	// private long asize, csize;

	PipedInputStream in; // = new PipedInputStream();

	PipedOutputStream out; // = new PipedOutputStream(in);

	private Dispatcher dispatcher;

	private long secondsAmount = 0;

	// 75 sectors per second
	// 4 packets per sector
	final private double cdgPacketsPerMicroSecond = (75.0d * 4.0d) / (1000000.0d);

	private int currentPacketIndex = 0;

	//private int cdgPacketsTotal;

	private BasicPlayer player;

	private BasicController controller;

	private Map audioInfo;

	Packet[] cdgPackets;

	public MusicPlayer(InputStream audioFile, InputStream cdgFile, Dispatcher d) {
		this.audioFileInputStream = audioFile; // this.asize = asize;
		this.cdgFileInputStream = cdgFile; // this.csize = csize;

		// this.cdgPacketsTotal = (int)csize/24; // 24 bytes per packet -
		// this file should only
		// be cdg packets
		this.player = new BasicPlayer();
		this.dispatcher = d;
		setupCDG();
	}

	public void run() {
		try {
			this.player.addBasicPlayerListener(this);
			// System.out.println("Pausing the thread after addBasicListener");
			// Thread.sleep(15000);
			this.player
					.open(new BufferedInputStream(this.audioFileInputStream));
			// System.out.println("Pausing the thread after open");
			// Thread.sleep(15000);
			// System.out.println("Calling play");
			this.player.play();
			// System.out.println("done");
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}

	public void setupCDG ()
    {
        try
        {
            
        	cdfFileInputStream = new BufferedInputStream(cdgFileInputStream);
        }
        catch (Exception ex)
        {
            System.err.println(ex); ex.printStackTrace(System.err);
        }

    }

	public void pollCDG(long microSeconds) {
		long expectedPacketNo = (long) (cdgPacketsPerMicroSecond * microSeconds);

		// System.out.println("pollCDG -- next packet is " + cdgPacketsConsumed
		// + " and we should have processed at least " + expectedPacketNo);

		int thisRound = 0;
		while (this.currentPacketIndex <= expectedPacketNo) {

	        byte[] packet = getNextPacket();
            Packet p = new Packet(packet);

            if (p.getCommand() == Packet.SC_CDG_COMMAND) {
				System.out.println(p);
				dispatcher.dispatchInstruction(p);
				// Thread.sleep(10);
			}
			System.out.println("Packet number " + thisRound + " at microseconds=" + microSeconds + ", and we should have processed at least (" + cdgPacketsPerMicroSecond + "*" + microSeconds + ") = " + expectedPacketNo);
			this.currentPacketIndex++;
			thisRound++;
		}
		// System.out.println("Leaving loop");
	}

	public void opened(Object o, Map p) {
		audioInfo = p;
		System.err.println(p.toString());
	}

	public void setController(BasicController controller) {
		System.err.println("setController");
		this.controller = controller;
	}

	public void stateUpdated(BasicPlayerEvent event) {
		System.err.println("stateUpdated");
	}

	private byte[] getNextPacket() {
		byte[] cdgBytes = new byte[24];
		int offset = 0;
		int numRead = 0;

		try {
			while (offset < cdgBytes.length
					&& (numRead = cdfFileInputStream.read(cdgBytes, offset,
							cdgBytes.length - offset)) >= 0) {
				offset += numRead;
			}
			if (cdfFileInputStream.available() <= 0)
				cdfFileInputStream.close();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return cdgBytes;
	}

	public void progress(int bytesread, long microseconds, byte[] pcmdata,
			java.util.Map properties) {
		int byteslength = -1;
		long total = -1;
		// If it fails then try again with JavaSound SPI.
		if (total <= 0)
			total = (long) Math
					.round(getTimeLengthEstimation(audioInfo) / 1000);
		// If it fails again then it might be stream => Total = -1
		if (total <= 0)
			total = -1;
		if (audioInfo.containsKey("audio.length.bytes")) {
			byteslength = ((Integer) audioInfo.get("audio.length.bytes"))
					.intValue();
		}
		float progress = -1.0f;
		if ((bytesread > 0) && ((byteslength > 0)))
			progress = bytesread * 1.0f / byteslength * 1.0f;

		if (audioInfo.containsKey("audio.type")) {
			String audioformat = (String) audioInfo.get("audio.type");
			if (audioformat.equalsIgnoreCase("mp3")) {
				// if (properties.containsKey("mp3.position.microseconds"))
				// secondsAmount = (long) Math.round(((Long)
				// properties.get("mp3.position.microseconds")).longValue()/1000000);
				// Equalizer
				if (total > 0)
					secondsAmount = (long) (total * progress);
				else
					secondsAmount = -1;
			} else if (audioformat.equalsIgnoreCase("wave")) {
				secondsAmount = (long) (total * progress);
			} else {
				secondsAmount = (long) Math.round(microseconds / 1000000);
			}
		} else {
			secondsAmount = (long) Math.round(microseconds / 1000000);
		}
		if (secondsAmount < 0)
			secondsAmount = (long) Math.round(microseconds / 1000000);

		pollCDG(microseconds);
	}

	/**
	 * Try to compute time length in milliseconds.
	 */
	public long getTimeLengthEstimation(Map properties) {
		long milliseconds = -1;
		int byteslength = -1;
		if (properties != null) {
			if (properties.containsKey("audio.length.bytes")) {
				byteslength = ((Integer) properties.get("audio.length.bytes"))
						.intValue();
			}
			if (properties.containsKey("duration")) {
				milliseconds = (int) (((Long) properties.get("duration"))
						.longValue()) / 1000;
			} else {
				// Try to compute duration
				int bitspersample = -1;
				int channels = -1;
				float samplerate = -1.0f;
				int framesize = -1;
				if (properties.containsKey("audio.samplesize.bits")) {
					bitspersample = ((Integer) properties
							.get("audio.samplesize.bits")).intValue();
				}
				if (properties.containsKey("audio.channels")) {
					channels = ((Integer) properties.get("audio.channels"))
							.intValue();
				}
				if (properties.containsKey("audio.samplerate.hz")) {
					samplerate = ((Float) properties.get("audio.samplerate.hz"))
							.floatValue();
				}
				if (properties.containsKey("audio.framesize.bytes")) {
					framesize = ((Integer) properties
							.get("audio.framesize.bytes")).intValue();
				}
				if (bitspersample > 0) {
					milliseconds = (int) (1000.0f * byteslength / (samplerate
							* channels * (bitspersample / 8)));
				} else {
					milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
				}
			}
		}
		return milliseconds;
	}

}
