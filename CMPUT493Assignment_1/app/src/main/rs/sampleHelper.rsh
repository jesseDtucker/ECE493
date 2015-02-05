#ifndef SAMPLE_HELPER_RSH
#define SAMPLE_HELPER_RSH

#include "rs_core.rsh"

#pragma version(1)
#pragma rs java_package_name(jetucker.cmput293assignment1.rs)
#pragma rs_fp_relaxed

uchar4 static GetColourAndClamp(rs_allocation source, int x, int y, int width, int height)
{
	int x_clamp = rsClamp(x, 0, width - 1);
	int y_clamp = rsClamp(y, 0, height - 1);
	return *(uchar4*)(rsGetElementAt(source, x_clamp, y_clamp));
}

uchar4 static WeightedAverage(uchar4 p1, uchar4 p2, float p1Weight, float p2Weight)
{
	float total = p1Weight + p2Weight;
	uchar4 result = 0;

	result.r = (p1.r * p1Weight + p2.r * p2Weight) / total;
	result.g = (p1.g * p1Weight + p2.g * p2Weight) / total;
	result.b = (p1.b * p1Weight + p2.b * p2Weight) / total;
	result.a = (p1.a * p1Weight + p2.a * p2Weight) / total;

	return result;
}

uchar4 static AveragePixels(uchar4 px1, uchar4 px2)
{
	return WeightedAverage(px1, px2, 0.5f, 0.5f);
}

uchar4 static sampleSource(rs_allocation source, float x, float y, int width, int height)
{
	int left = (int)(floor(x));
	int right = left + 1;
	int top = (int)(floor(y));
	int bottom = top + 1;

	uchar4 topLeft = GetColourAndClamp(source, left, top, width, height);
	uchar4 topRight = GetColourAndClamp(source, right, top, width, height);
	uchar4 bottomLeft = GetColourAndClamp(source, left, bottom, width, height);
	uchar4 bottomRight = GetColourAndClamp(source, right, bottom, width, height);

	uchar4 leftColour = AveragePixels(topLeft, bottomLeft);
	uchar4 rightColour = AveragePixels(topRight, bottomRight);

	float leftWeight = 1.0f - (x - (float)(left));
	float rightWeight = 1.0f - ((float)(right) - x);

	uchar4 horizontalColour = WeightedAverage(leftColour, rightColour, leftWeight, rightWeight);

	uchar4 topColour = AveragePixels(topLeft, topRight);
	uchar4 bottomColour = AveragePixels(bottomLeft, bottomRight);

	float topWeight = 1.0f - (y - (float)(top));
	float bottomWeight = 1.0f - ((float)(bottom) - y);

	uchar4 verticalColour = WeightedAverage(topColour, bottomColour, topWeight, bottomWeight);

	return AveragePixels(horizontalColour, verticalColour);
}

#endif