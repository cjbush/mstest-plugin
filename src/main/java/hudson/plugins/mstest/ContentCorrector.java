package hudson.plugins.mstest;

import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;

import java.io.*;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivo on 27/03/2015.
 */
public class ContentCorrector
{
    private String file;

    public ContentCorrector(String file)
    {
        this.file = file;
    }

    public void fix() throws IOException
    {
        String filename = Integer.toString(randInt(1000, 1000000)) + ".trx";
        File inFile = new File(file);
        File parent = inFile.getParentFile();
        File outfile = new File(parent, filename);
        PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(outfile), com.google.common.base.Charsets.UTF_8));
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(inFile), com.google.common.base.Charsets.UTF_8));
        String line = in.readLine();
        boolean replace = false;
        while (line != null) {
            String newline = stripIllegalEntities(line);
            if (line.length() != newline.length())
                replace = true;
            out.println(newline);
            line = in.readLine();
        }
        in.close();
        out.close();
        if (replace) {
            inFile.delete();
            outfile.renameTo(inFile);
            /*java.nio.file.Files.move(
                    outfile.toPath(),
                    inFile.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.LinkOption.NOFOLLOW_LINKS);*/
        }
        else
            outfile.delete();
          // java.nio.file.Files.delete(outfile.toPath());
    }

    /*
    private String stripIllegalCharacters(String line)
    {
        String xml10pattern = "[^"
                + "\u0009\r\n"
                + "\u0020-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]";
        String xml11pattern = "[^"
                + "\u0001-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]+";
        return line.replaceAll(xml10pattern, "").replaceAll(xml11pattern, "");
    }*/

    private String stripIllegalEntities(String line)
    {
        final String pattern = "(&#x([0-9A-Fa-f]{1,4});)";
        /* Java 1.7+
        final String pattern = "(?<entity>&#x(?<char>[0-9A-Fa-f]{1,4});)";*/
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(line);

        while (m.find()) {
            String charGroup = m.group(2);
            /* Java 1.7+
            String charGroupJava17 = m.group("char");*/
            long c = Long.parseLong(charGroup, 16);
            if (!isAllowed(c)) {
                line = new StringBuilder(line).replace(m.start(1), m.end(1), "").toString();
                m = p.matcher(line);
            }
        }
        return line;
    }

    public boolean isAllowed(long c)
    {
        return c == 9 || c == 0xA || c == 0xD || (c > 0x20 && c < 0xD7FF) || (c > 0xE000 && c < 0xFFFD) || (c > 0x10000 && c < 0x10FFFF);
    }

    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }
}
