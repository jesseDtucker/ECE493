package jetucker.cmput293assignment1;

import android.graphics.Point;

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
        if (BuildConfig.DEBUG)
        {
            if (!check)
            {
                throw new AssertionError(message);
            }
        }
    }

    public static void Fail(String message)
    {
        if (BuildConfig.DEBUG)
        {
            throw new AssertionError(message);
        }
    }

    public static int GetDistSquared(Point p1, Point p2)
    {
        int xSqrd = (p1.x - p2.x)* (p1.x - p2.x);
        int ySqrd = (p1.y - p2.y) * (p1.y - p2.y);
        return xSqrd + ySqrd;
    }

    public static float GetAngle(Point p1, Point p2)
    {
        return (float)Math.atan2(   p1.y - p2.y,
                                    p1.x - p2.x);
    }

    // Thanks to stack overflow: http://stackoverflow.com/questions/3365171/calculating-the-angle-between-two-lines-without-having-to-calculate-the-slope
    public static float AngleBetween2Lines(Point line1Point1, Point line1Point2, Point line2Point1, Point line2Point2)
    {
        float angle1 = GetAngle(line1Point1, line1Point2);
        float angle2 = GetAngle(line2Point1, line2Point2);
        return (float)(angle1-angle2);
    }
}
