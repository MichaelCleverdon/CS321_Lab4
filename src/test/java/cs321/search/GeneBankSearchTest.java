package cs321.search;

import cs321.common.ParseArgumentException;
import org.junit.Test;

import static org.junit.Assert.*;

public class GeneBankSearchTest
{
    private String[] args;
    private GeneBankSearchArguments expectedConfig;
    private GeneBankSearchArguments actualConfig;

    //region Config

    @Test
    public void parse3CorrectArgumentsTest() throws ParseArgumentException{
        args = new String[3];
        args[0] = "0";
        args[1] = "file.bin";
        args[2] = "query1";

        expectedConfig = new GeneBankSearchArguments(false, "file.bin", "query1", 0, 0);
        actualConfig = GeneBankSearch.parseArguments(args);
        assertEquals(expectedConfig.toString(), actualConfig.toString());
    }

    @Test
    public void parse4CorrectArgumentsTest() throws ParseArgumentException{
        args = new String[4];
        args[0] = "1";
        args[1] = "file.bin";
        args[2] = "query1";
        args[3] = "100";

        expectedConfig = new GeneBankSearchArguments(true, "file.bin", "query1", 100, 0);
        actualConfig = GeneBankSearch.parseArguments(args);
        assertEquals(expectedConfig.toString(), actualConfig.toString());
    }

    @Test
    public void parse5CorrectArgumentsTest() throws ParseArgumentException{
        args = new String[5];
        args[0] = "1";
        args[1] = "file.bin";
        args[2] = "query1";
        args[3] = "100";
        args[4] = "0";

        expectedConfig = new GeneBankSearchArguments(true, "file.bin", "query1", 100, 0);
        actualConfig = GeneBankSearch.parseArguments(args);
        assertEquals(expectedConfig.toString(), actualConfig.toString());
    }

    @Test(expected = ParseArgumentException.class)
    public void parse2Arguments() throws ParseArgumentException{
        args = new String[2];
        args[0] = "1";
        args[1] = "file.bin";

        GeneBankSearch.parseArguments(args);
    }

    @Test(expected = ParseArgumentException.class)
    public void parse1Argument() throws ParseArgumentException{
        args = new String[3];
        args[0] = "1";

        GeneBankSearch.parseArguments(args);
    }

    @Test(expected = ParseArgumentException.class)
    public void parseUsingCacheWithoutSize() throws ParseArgumentException{
        args = new String[3];
        args[0] = "1";
        args[1] = "file.bin";
        args[2] = "query1";

        GeneBankSearch.parseArguments(args);
    }

    @Test(expected = ParseArgumentException.class)
    public void parseUsingCacheWith0CacheSize() throws ParseArgumentException{
        args = new String[4];
        args[0] = "1";
        args[1] = "file.bin";
        args[2] = "query1";
        args[3] = "0";

        GeneBankSearch.parseArguments(args);
    }

    @Test
    public void parseWithoutCacheWithCacheSize() throws ParseArgumentException{
        args = new String[4];
        args[0] = "0";
        args[1] = "file.bin";
        args[2] = "query1";
        args[3] = "100";

        GeneBankSearch.parseArguments(args);
    }
//
//    @Test
//    public void parseUsingCacheWith0CacheSize(){
//        args = new String[3];
//        args[0] = "1";
//        args[1] = "file.bin";
//        args[2] = "query1";
//        args[3] = "0";
//
//        ParseArgumentException ex = assertThrows(ParseArgumentException.class, () -> GeneBankSearch.parseArguments(args));
//        assertEquals("Please supply a cache size above 0", ex.getMessage());
//    }

    //endregion

}