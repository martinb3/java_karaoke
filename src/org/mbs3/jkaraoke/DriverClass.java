package org.mbs3.jkaraoke;
import java.io.*;
import java.util.*;
import java.util.zip.*;
import javax.sound.sampled.*;

/*
 * Created on Jul 12, 2006
 *
 * TODO Nothing yet.
 */

/**
 * @author Martin Smith
 *
 * TODO None yet.
 */
public class DriverClass
{
    public static void main (String[] args)
    {
        try {
            File zipFileObject = new File("joel.zip");
            ZipFile zf = new ZipFile(zipFileObject);
            
            ZipEntry zeMusicFile = zf.getEntry("joel.mp3");
            ZipEntry zeCdgFile = zf.getEntry("joel.cdg");
            
            Enumeration e = zf.entries();
            while(e.hasMoreElements())
                    System.out.println(e.nextElement());
            
            InputStream f  = zf.getInputStream(zeMusicFile);
            InputStream c  = zf.getInputStream(zeCdgFile);
            
            Frame kFrame = new Frame();
            Dispatcher dispatcher = new Dispatcher(kFrame);
            MusicPlayer mp = new MusicPlayer(f,zeMusicFile.getSize(), c, zeCdgFile.getSize(), dispatcher);
            kFrame.setVisible(true);
            
            System.out.println("Creating thread");
            Thread t = new Thread(mp);
            System.out.println("Thread sent off");
            t.start();

        } catch (Exception ex) { ex.printStackTrace(System.err); }
        
    }
}
