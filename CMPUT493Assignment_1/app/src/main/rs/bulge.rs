
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

uchar4 __attribute__((kernel)) bulge(uchar4 in, uint32_t x, uint32_t y)
{
	float2 poscur = {x, y};
	float radius = fast_distance(poscur, m_center);

	rsDebug("radius : ", radius);
	rsDebug("poscur : ", poscur);

	uchar4 result = in;
	if(radius < m_radius)
	{
		float dist = (1.0f - (m_radius - radius) / m_radius) * radius;
		float2 dir = fast_normalize(poscur - m_center);
		float2 offset = dist * dir + poscur;
		result = sampleSource(m_source, offset.x, offset.y, m_width, m_height);
	}

	return result;
}