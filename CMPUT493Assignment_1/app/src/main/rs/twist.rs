
#include "rs_core.rsh"
#include "sampleHelper.rsh"

#pragma version(1)
#pragma rs java_package_name(jetucker.cmput293assignment1.rs)
#pragma rs_fp_relaxed

rs_allocation m_source;

int m_width;
int m_height;
float m_radius;
float2 m_center;
float m_twist;

uchar4 __attribute__((kernel)) twist(uchar4 in, uint32_t x, uint32_t y)
{
	float2 poscur = {x, y};
	float radius = fast_distance(poscur, m_center);

	uchar4 result = in;
	if(radius < m_radius)
	{
		float twist = (m_radius - radius) / m_radius * m_twist;

		float oldX = x;
		float oldY = y;

		float relative_x = x - m_center.x;
		float relative_y = y - m_center.y;

		float sin_twist = sin(twist);
		float cos_twist = cos(twist);

		float newX = relative_x * cos_twist + relative_y * sin_twist;
		float newY = -1.0f * relative_x * sin_twist + relative_y * cos_twist;

		newX += m_center.x;
		newY += m_center.y;

		result = sampleSource(m_source, newX, newY, m_width, m_height);
	}

	return result;
}