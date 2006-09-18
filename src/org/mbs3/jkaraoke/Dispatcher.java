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
import javax.swing.SwingUtilities;

/*
 * Created on Jul 8, 2006 TODO Nothing yet.
 */

/**
 * @author Martin Smith TODO None yet.
 */
public class Dispatcher
{

    Display               display;

    /**
     * 
     */
    public Dispatcher (Display display)
    {
        super();
        this.display = display;
    }

    public void dispatchInstruction (Packet cdgPacket)
    {
        byte[] cdgData = cdgPacket.getData();

        if (cdgPacket.getInstruction() == Packet.INSTR_MEMORY_PRESET)
        {
            byte color = (byte) (cdgData[0] & 0x0F);
            byte repeat = (byte) (cdgData[1] & 0x0F);
            if (repeat != 0)
            {
                //System.out.println("Skipping Repeat command: " + repeat);
                return;
            }

            final int colorIndex = color;

            Runnable doWorkRunnable = new Runnable() {

                public void run ()
                {
                    display.setBackgroundColor(colorIndex);
                }
            };
            try
            {
                SwingUtilities.invokeAndWait(doWorkRunnable);
            }
            catch (Exception ex)
            {
                System.err.println(ex);
            }

        }
        else if (cdgPacket.getInstruction() == Packet.INSTR_BORDER_PRESET)
        {
            byte color = (byte) (cdgData[0] & 0x0F);

            final int c = color;
            //System.out.println("Change border color to " +
            //Integer.toBinaryString(c) + "(" + c + ")");
            //System.out.println();

            Runnable doWorkRunnable = new Runnable() {

                public void run ()
                {
                    display.setBorderColor(c);
                }
            };
            try
            {
                SwingUtilities.invokeAndWait(doWorkRunnable);
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
        }
        else if (cdgPacket.getInstruction() == Packet.INSTR_TILE_BLOCK_XOR
                || cdgPacket.getInstruction() == Packet.INSTR_TILE_BLOCK)
        {
            final int color0 = (cdgData[0] & 0x0F);
            final int color1 = (cdgData[1] & 0x0F);
            final int row = cdgData[2] & 0x1F;
            final int column = cdgData[3] & 0x3F;
            
            byte [] tilePixels = new byte[12];

            for (int i = 0; i < tilePixels.length; i++)
                tilePixels[i] = (byte) (cdgData[i + 4] & 0x3F);

            final byte [] tp = tilePixels;
            final int flag = cdgPacket.getInstruction();

            Runnable doWorkRunnable = new Runnable() {

                public void run ()
                {
                    display.setTileColor(row, column, color0, color1, tp, flag);
                }
            };
            try
            {
                SwingUtilities.invokeAndWait(doWorkRunnable);
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }
        }
        else if (cdgPacket.getInstruction() == Packet.INSTR_SCROLL_PRESET
                || cdgPacket.getInstruction() == Packet.INSTR_SCROLL_COPY)
        {
            // only used in scroll preset, in copy the screen is wrapped
            byte color = (byte) (cdgData[0] & 0x0F);

            /*
             * hScrollCmd is a scrolliing instruction, which is either 0, 1 or
             * 2. 0 means don't scroll 1 means scroll 6 pixels to the right, 2
             * means scroll 6 pixels to the left. hOffset is a horizontal offset
             * which is used for offsetting the graphic display by amounts less
             * than 6 pixels. It can assume values from 0 to 5.
             */

            byte hScroll = (byte) (cdgData[1] & 0x0F);
            byte hScrollCmd = (byte) ((hScroll & 0x30) >>> 4);
            byte hOffset = (byte) (hScroll & 0x07);

            System.out.println("Scroll the screen vertically " + hScroll
                    + " with offset " + hOffset + " in the direction "
                    + hScrollCmd);

            /*
             * vScrollCmd is a scrolliing instruction, which is either 0, 1 or
             * 2. 0 means don't scroll 1 means scroll 12 pixels down, 2 means
             * scroll 12 pixels up. vOffset is a vertical offset which is used
             * for offsetting the graphic display by amounts less than 12
             * pixels. It can assume values from 0 to 11.
             */

            byte vScroll = (byte) (cdgData[2] & 0x3F);
            byte vScrollCmd = (byte) ((vScroll & 0x30) >>> 4);
            byte vOffset = (byte) (vScroll & 0x0F);

            System.out.println("Scroll the screen vertically " + vScroll
                    + " with offset " + vOffset + " in the direction "
                    + vScrollCmd);

            if (cdgPacket.getInstruction() == Packet.INSTR_SCROLL_PRESET)
            {
                System.out.println("Don't wrap the screen, but paint the uncovered tiles " + color);
            }
            else
            {
                System.out.println("Wrap the screen during the scroll.");
            }

            //System.out.println();

        }
        else if (cdgPacket.getInstruction() == Packet.INSTR_DEFINE_TRANSPARENT)
        {
            /*
             * This command is used to define a CLUT color as being transparent,
             * for example so that the the graphics can be overlayed on top of a
             * live video signal. We don't really use it (maybe someday?)
             */
            //System.out.println("Sent transparent color code");
        }
        else if (cdgPacket.getInstruction() == Packet.INSTR_LOAD_COLOR_TABLE_LOWER
                || cdgPacket.getInstruction() == Packet.INSTR_LOAD_COLOR_TABLE_UPPER)
        {
            
            // [---high byte---] [---low byte----]
            //  7 6 5 4 3 2 1 0   7 6 5 4 3 2 1 0
            //  X X r r r r g g   X X g g b b b b
            
            int [] colorTable = new int[8];
            
            //System.out.println("Color table loading");
            for (int i = 0; i < 8; i++)
            {
                int upper = (cdgData[2*i] & 0x3F) << 6;
                int lower = (cdgData[2*i + 1] & 0x3F);
                colorTable[i] = upper | lower;
            }
            
            final int [] ptr = colorTable;
            final int flag = cdgPacket.getInstruction();
            
            Runnable doWorkRunnable = new Runnable() {

                public void run ()
                {
                    display.loadColorTable(ptr, flag);
                }
            };
            try
            {
                SwingUtilities.invokeAndWait(doWorkRunnable);
            }
            catch (Exception ex)
            {
                System.out.println(ex);
            }

        } else
        {
            // totally unknown CDG packet
            System.out.println("Unknown packet with command " + cdgPacket.getCommand() + " and instruction " + cdgPacket.getInstruction());
            System.out.println(cdgPacket.constToString(cdgPacket.getInstruction()));
        }
    }
}
