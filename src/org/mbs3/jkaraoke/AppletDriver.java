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

import java.io.File;
import java.io.InputStream;
import java.util.zip.*;

import javax.swing.JApplet;
import javax.swing.JFileChooser;

/**
 * @author Martin
 * 
 */
public class AppletDriver extends JApplet {

	MusicPlayer musicPlayer;

	Thread playerThread;

	public void init() {

	}

	public void start() {
		try {
			JFileChooser jfc = new JFileChooser();
			jfc.setDialogType(JFileChooser.OPEN_DIALOG);
			int response = jfc.showOpenDialog(null);
			if (response != JFileChooser.APPROVE_OPTION)
				return;

			String name = jfc.getSelectedFile().getName();
			if (!name.endsWith("zip"))
				return;

			name = name.substring(0, name.length() - 4);
			File zipFileObject = jfc.getSelectedFile();
			ZipFile zf = new ZipFile(zipFileObject);

			ZipEntry zeMusicFile = zf.getEntry(name + ".mp3");
			ZipEntry zeCdgFile = zf.getEntry(name + ".cdg");

			InputStream f = zf.getInputStream(zeMusicFile);
			InputStream c = zf.getInputStream(zeCdgFile);

			Display display = new Display();
			super.getContentPane().add(display);
			Dispatcher dispatcher = new Dispatcher(display);
			musicPlayer = new MusicPlayer(f, c, dispatcher);
			playerThread = new Thread(musicPlayer);

			super.setSize(display.getPreferredSize());
			playerThread.start();

		} catch (Exception ex) {
			ex.printStackTrace(System.err);
		}

	}

	public void stop() {
		playerThread.interrupt();
	}

	public void destroy() {

	}

}
