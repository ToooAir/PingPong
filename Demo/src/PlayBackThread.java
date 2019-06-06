
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.io.RandomAccessFile;

import java.util.Scanner;

/**
 * @author Chandler
 */
public class PlayBackThread extends Thread {
    private SourceDataLine dataline;
    private final int dataOffset = 0x2e;
    private String url;

    public PlayBackThread(String input) {
        super("playBackThread");
        url = input;
    }

    @Override
    public void run() {
        try {
            RandomAccessFile raf = new RandomAccessFile(url, "r");
            AudioFormat af;
            af = new AudioFormat(22050, 16, 1, true, false);
            dataline = AudioSystem.getSourceDataLine(af);
            dataline.open(af);
            raf.seek(dataOffset);
            int hasRead = 0;
            dataline.start();
            byte[] buff = new byte[4096];
            while ((hasRead = raf.read(buff)) > 0) {
                dataline.write(buff, 0, hasRead);
            }
            dataline.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void print(long pointer, byte[] buff) {
        System.out.format("%x: ", pointer);
        System.out.format("%x ", buff[0]);
        System.out.format("%x ", buff[1]);
        System.out.format("%x ", buff[2]);
        System.out.format("%x ", buff[3]);

        System.out.println();
    }

}
