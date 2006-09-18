/**
 * 
 */
package org.mbs3.jkaraoke;

import java.io.File;
import java.io.InputStream;
import java.util.zip.*;

import javax.swing.JApplet;

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
            String name = "testsong";
            
        	File zipFileObject;
        	
            if(false)
            {
            	java.net.URL temp = this.getClass().getResource("/" + name + ".zip");
            	java.io.InputStream s = this.getClass().getResourceAsStream("/" + name + ".zip");
            	System.out.println(s.available());
            	zipFileObject = new File(temp.getFile());
            }
            else
            {
            	zipFileObject = new File("C:\\" + name + ".zip");
            }
            
            System.out.println("Does my zip file exist? " + zipFileObject.exists());
        	System.out.println("Can I read my zip file? " + zipFileObject.canRead());
        	
        	
            ZipFile zf = new ZipFile(zipFileObject);
            
            ZipEntry zeMusicFile = zf.getEntry(name + ".mp3");
            ZipEntry zeCdgFile = zf.getEntry(name + ".cdg");
            
            InputStream f  = zf.getInputStream(zeMusicFile);
            InputStream c  = zf.getInputStream(zeCdgFile);
            
            
            Display display = new Display();
            super.getContentPane().add(display);
            Dispatcher dispatcher = new Dispatcher(display);
            musicPlayer = new MusicPlayer(f, c, dispatcher);
            playerThread = new Thread(musicPlayer);
            
            super.setSize(display.getPreferredSize());
            playerThread.start();

        } catch (Exception ex) { ex.printStackTrace(System.err); }
		
	}
	
	public void stop() {
		playerThread.interrupt();
	}
	
	public void destroy() {
		
	}
	
}
