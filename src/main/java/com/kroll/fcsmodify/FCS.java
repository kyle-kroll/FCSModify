/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kroll.fcsmodify;

/*
    Purpose: Class for interfacing with FCS files from flow cytometry
    Author: Kyle Kroll
    Date: 2020-09-24
    Environment: JDK14
    Justification: There is a lack of programs available for modifying raw FCS file attributes. An example would be that FCS
    files were exported without proper channel names. There is no easy way to add these except during analyis with 3rd part programs. 
    The goal of this program will be to allow users to modify these parameters and save as a new FCS file.
*/
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class FCS{
    private String filepath;
    private String fcsVersion;
    private LinkedHashMap<String, Integer> HEADER=  new LinkedHashMap<String, Integer>();
    private LinkedHashMap<String, String> TEXT = new LinkedHashMap<String, String>();
    private byte [] DATA;
    private RandomAccessFile raf;
    private byte[] delim = new byte[1];
    private String textDelim = String.valueOf((char)124);
    
    public void setFilepath(String path) {
        filepath = path;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getFCSVersion(){
        return fcsVersion;
    }

    public LinkedHashMap<String, Integer> getHeader() {
        return HEADER;
    }

    public byte [] getFCSData(){
        return DATA;
    }

    public LinkedHashMap<String, String> getText(){
        return TEXT;
    }
    
    public void setNewParameter(String parameter, String newValue) throws Exception {
        if (TEXT.containsKey(parameter)) {
            TEXT.put(parameter, newValue);
        } else {
            throw new Exception("Error: key not found.");
        }
    }

    private void parseHeader() throws IOException {
        String[] headers = {"text_start", "text_end", "data_start", "data_end", "analysis_start", "analysis_end"};
        
        for (String s: headers) {
            byte[] b = new byte[8];
            raf.read(b);
            int pos = Integer.parseInt(new String(b, StandardCharsets.US_ASCII).trim());
            HEADER.put(s, pos);
        }   
    }
    private void parseText() throws IOException {
        byte[] c = new byte[(HEADER.get("text_end")) - HEADER.get("text_start")];
        
        raf.seek(HEADER.get("text_start"));
        raf.read(delim);
        raf.read(c);
        String txt = new String(c, StandardCharsets.US_ASCII);
        String[] splitText = txt.split(String.valueOf(new String(delim, StandardCharsets.US_ASCII).charAt(0)));
        int i = 0;
        while (i < splitText.length-1) {
            TEXT.put(splitText[i], splitText[i+1]);
            i += 2;
        }
        
    }

    private void parseData() throws IOException{
        int dataStart = HEADER.get("data_start");
        int dataEnd = HEADER.get("data_end");
        if (HEADER.get("data_start").equals(0)) {
            dataStart = Integer.parseInt(TEXT.get("$BEGINDATA").trim());
            dataEnd = Integer.parseInt(TEXT.get("$ENDDATA").trim());
        }
        byte[] d = new byte[dataEnd - dataStart];
        raf.seek(HEADER.get("data_start"));
        raf.read(d);
        DATA = d;
    }
    /*
        The parsing and writing largely work for now. I need to figure out how
        to programmatically handle skipping of bytes.
    */
    public void parseFCS() throws Exception{
        byte[] bytes = new byte[6];
        raf = new RandomAccessFile(filepath, "r");
        raf.seek(0);
        raf.read(bytes);
        String ver = new String(bytes);
        if (ver.equals("FCS3.0") || ver.equals("FCS3.1")) {
            fcsVersion = ver;
        } else {
            raf.close();
            throw new Exception("Error: Wrong FCS version. This program only supports FCS3.0 and FCS3.1");
        } 
        raf.skipBytes(4);
        try {
            parseHeader();
            parseText();
            parseData();
        } catch (IOException e) {
            System.out.println(e);
        }
        
        raf.close();
        
    }
    /*
        This will be a large function that will parse all the data in the file.
        I am not concerned with the data section but I will need their positions for when the new file is being written.

        I think I am going to switch to creating a byte array for each section, then use the sizes of those blocks to change the header positions.
        Will use ByteArrayOutputStream and it's function write(byte[] b, int offset, int length)
    */
    public void writeFCS(String filepath) throws IOException {
        RandomAccessFile newFCS = new RandomAccessFile(filepath, "rw");
        
        /*
            I need to re-write this section to prepare the TEXT and DATA first so I can get the sizes.
            With these sizes I can set the new values in HEADER then write it all to file.
        */
        String fcsFile = "";
        
        int filePos = 0; // int to hold current position of the file
        filePos += 58; // simulate the header being inserted by adding 58 to pos
        // TEXT will always start at 256 and the header will always be 58 bytes
        String bufferHeaderToText = String.valueOf((char)32).repeat(HEADER.get("text_start")-filePos);
        String text = prepareText();
        byte[] fcsD = prepareData();
        filePos += bufferHeaderToText.length() + text.length();
        HEADER.put("text_end", filePos);
        String bufferTextToData = String.valueOf((char)32).repeat(5);
        filePos += 5;
        
        /* Need to handle files > 100MB because data start and end are stored
            in TEXT not header. Also need to modify TEXT to update positions
            when they change and make sure everything matches up
        
            $BEGINDATA is 5 bytes
            $ENDDATA is 19 bytes where any unused bytes are trailing whitespace
        */
        
        HEADER.put("data_start", filePos);
        HEADER.put("data_end", fcsD.length - filePos);
        
        String newHEADER = prepareHeader();
        
        fcsFile += newHEADER;
        fcsFile += bufferHeaderToText;
        fcsFile += text;
        fcsFile += bufferTextToData;
        byte[] fcsB = fcsFile.getBytes();
        
        
        // The next few lines are for 
        byte[] allByteArray = new byte[fcsB.length + fcsD.length];

        ByteBuffer buff = ByteBuffer.wrap(allByteArray);
        buff.put(fcsB);
        buff.put(fcsD);

        byte[] combined = buff.array();
        //System.out.println(combined.length);
        //byte[] fcsB = fcsFile.getBytes();
        //System.out.println(fcsFile.length());
        newFCS.write(combined);
        newFCS.close();
    }
    
   /* 
    *   This function will prepare the header as a byte array
    *   First it will create the header as a string then convert to byte[]
    */
   
    private String prepareHeader(){
        String head = "";
        head += fcsVersion;
        head += String.valueOf((char)32).repeat(4);
        for (String key: HEADER.keySet()) {
            String v = Integer.toString(HEADER.get(key));
            String buff = String.valueOf((char)32).repeat(8-v.length());
            buff += v;
            head += buff;
        }
        return head;
    }
    
    /*
    *   Prepare the TEXT section as string then convert to byte[]
    */
    private String prepareText() {
        String textBody = "";
        for (String key: TEXT.keySet()) {
            textBody += textDelim + key + textDelim + TEXT.get(key);
        }
        return textBody;
    }
    
    /*
    *   Prepare the DATA section
    *   DATA will be kept as a byte[] from the original FCS file
    *   so this should just return the fcsData object
    */
    private byte [] prepareData() {
        return DATA;
    }
}
