
#include "rs_core.rsh"
#include "sampleHelper.rsh"

#pragma version(1)
#pragma rs java_package_name(jetucker.cmput293assignment1.rs)
#pragma rs_fp_relaxed

rs_allocation m_source;

int m_width;
int m_height;
float m_magnitude;
float m_period;
float m_strength;

uchar4 __attribute__((kernel)) partialBlockify(uchar4 in, uint32_t x, uint32_t y)
{
	float newX = (sin((float)(x) / (m_period)) - 0.5f) * m_magnitude + (float)(x);
	float newY = -1.0f * (cos((float)(y) / (m_period)) - 0.5f) * m_magnitude + (float)(y);
	float originalStrength = 1.0f - m_strength;

	uchar4 newColour = sampleSource(m_source, newX, newY, m_width, m_height);

	return WeightedAverage(newColour, in, m_strength, originalStrength);
}