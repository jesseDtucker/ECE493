package jetucker.cmput293assignment1;

/**
 * Util functions
 */
public class Util
{
    // Android is dumb and doesn't actually respect asserts. This function
    // will demand the respect that assert deserves.
    public static void Assert(boolean check)
    {
        Assert(check, "Assertion Failed!");
    }

    public static void Assert(boolean check, String message)
    {
        // I consider asserts to be a sanity check for the programmer, so only use in
        // debug builds
        if(BuildConfig.DEBUG)
        {
            if(!check)
            {
                throw new AssertionError(message);
            }
        }
    }

    public static void Fail(String message)
    {
        if(BuildConfig.DEBUG)
        {
            throw new AssertionError(message);
        }
    }
}
