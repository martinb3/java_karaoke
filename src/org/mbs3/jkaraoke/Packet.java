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
import java.util.BitSet;

/*
 * Created on Jul 8, 2006 TODO Nothing yet.
 */

/**
 * @author Martin Smith typedef struct { char command; char instruction; char
 *         parityQ[2]; char data[16]; char parityP[4]; } SubCode; The 16 bytes
 *         of data are each divided like so: Channel# P Q R S T U V W Bit# 0-15
 *         7 6 5 4 3 2 1 0
 */
public class Packet
{

    // the entire packet
    private byte[]           packetBytes;

    // instruction and command mask
    public static final long SC_MASK                      = 0x3F;

    public static final long SC_CDG_COMMAND               = 0x09;

    // instruction constants
    // Set the screen to a particular color
    public static final int  INSTR_MEMORY_PRESET          = 1;

    // Set the border of the screen to a particular color
    public static final int  INSTR_BORDER_PRESET          = 2;

    // Load a 12 x 6, 2 color tile and display it normally.
    public static final int  INSTR_TILE_BLOCK             = 6;

    // Scroll the image, filling in the new area with a color.
    public static final int  INSTR_SCROLL_PRESET          = 20;

    // Scroll the image, rotating the bits back around.
    public static final int  INSTR_SCROLL_COPY            = 24;

    // Define a specific color as being transparent.
    public static final int  INSTR_DEFINE_TRANSPARENT     = 28;

    // Load in the lower 8 entries of the color table (0-7).
    public static final int  INSTR_LOAD_COLOR_TABLE_LOWER = 30;

    // Load in the upper 8 entries of the color table (8-15).
    public static final int  INSTR_LOAD_COLOR_TABLE_UPPER = 31;

    // Load a 12 x 6, 2 color tile and display it using the XOR method.
    public static final int  INSTR_TILE_BLOCK_XOR         = 38;

    /**
     * 
     */
    public Packet (byte[] packetBytes)
    {
        super();
        this.packetBytes = new byte[packetBytes.length];
        for (int i = 0; i < packetBytes.length; i++)
            this.packetBytes[i] = packetBytes[i];
    }

    public byte getCommand ()
    {
        return (byte)(this.packetBytes[0] & SC_MASK);
    }

    public byte getInstruction ()
    {
        return (byte)(this.packetBytes[1] & SC_MASK);
    }

    public byte[] getParityQ ()
    {
        byte[] ret = new byte[2];
        ret[0] = this.packetBytes[2];
        ret[1] = this.packetBytes[3];
        return ret;
    }

    public byte[] getData ()
    {
        byte[] ret = new byte[16];
        for (int i = 0; i < 16; i++)
            ret[i] = (byte)(this.packetBytes[i + 4] & 0x3F);
        return ret;
    }

    public String toString ()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("Command: " + constToString(this.getCommand()) + "\n");
        sb.append("Instruction: " + constToString(this.getInstruction()) + "\n");
        sb.append("Data: " + byteArrayToBitString(this.getData()) + "\n");
        return sb.toString();
    }

    public static String byteArrayToIntString (byte[] bytes)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < bytes.length; i++)
        {
            sb.append(bytes[i]);
            if (i != bytes.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String byteArrayToBitString (byte[] bytes)
    {
        StringBuffer sb = new StringBuffer();
        sb.append("[");
        for (int i = 0; i < bytes.length; i++)
        {
            sb.append(Integer.toBinaryString((int)bytes[i]));
            if (i != bytes.length - 1)
                sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    
    static String byteArrayToHexString (byte in[])
    {

        byte ch = 0x00;
        int i = 0;
        if (in == null || in.length <= 0)
            return null;

        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "A", "B", "C", "D", "E", "F" };

        StringBuffer out = new StringBuffer(in.length * 2);
        while (i < in.length)
        {
            ch = (byte) (in[i] & 0xF0); // Strip off high nibble
            ch = (byte) (ch >>> 4);
            // shift the bits down
            ch = (byte) (ch & 0x0F);
            // must do this is high order bit is on!
            out.append(pseudo[(int) ch]); // convert the nibble to a String
            // Character
            ch = (byte) (in[i] & 0x0F); // Strip off low nibble
            out.append(pseudo[(int) ch]); // convert the nibble to a String
            // Character
            i++;
        }

        String rslt = new String(out);
        return rslt;
    }

    // Returns a bitset containing the values in bytes.
    // The byte-ordering of bytes must be big-endian which means the most
    // significant bit is in element 0.
    public static BitSet toBitSet (byte[] bytes)
    {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++)
        {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0)
            {
                bits.set(i);
            }
        }
        return bits;
    }

    public static BitSet toBitSet (byte myByte)
    {
        byte[] b = new byte[1];
        b[0] = myByte;
        return toBitSet(b);
    }

    // Returns a byte array of at least length 1.
    // The most significant bit in the result is guaranteed not to be a 1
    // (since BitSet does not support sign extension).
    // The byte-ordering of the result is big-endian which means the most
    // significant bit is in element 0.
    // The bit at index 0 of the bit set is assumed to be the least significant
    // bit.
    public static byte[] toByteArray (BitSet bits)
    {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++)
        {
            if (bits.get(i))
            {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }

    public String constToString (long constVal)
    {
        if(constVal == SC_CDG_COMMAND)
            return "SC_CDG_COMMAND";
        else if (constVal == INSTR_MEMORY_PRESET)
            return "INSTR_MEMORY_PRESET";
        else if (constVal == INSTR_BORDER_PRESET)
            return "INSTR_BORDER_PRESET";
        else if (constVal == INSTR_TILE_BLOCK)
            return "INSTR_TILE_BLOCK";
        else if (constVal == INSTR_SCROLL_PRESET)
            return "INSTR_SCROLL_PRESET";
        else if (constVal == INSTR_SCROLL_COPY)
            return "INSTR_SCROLL_COPY";
        else if (constVal == INSTR_DEFINE_TRANSPARENT)
            return "INSTR_DEFINE_TRANSPARENT";
        else if (constVal == INSTR_LOAD_COLOR_TABLE_LOWER)
            return "INSTR_LOAD_COLOR_TABLE_LOWER";
        else if (constVal == INSTR_LOAD_COLOR_TABLE_UPPER)
            return "INSTR_LOAD_COLOR_TABLE_UPPER";
        else if (constVal == INSTR_TILE_BLOCK_XOR)
            return "INSTR_TILE_BLOCK_XOR";
        else
            return "UNKNOWN - " + constVal;
    }

    public static String bitSetToBinaryString (BitSet bs)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bs.size(); i++)
            if (bs.get(i))
                sb.append("1");
            else
                sb.append("0");

        return sb.toString();
    }

}
