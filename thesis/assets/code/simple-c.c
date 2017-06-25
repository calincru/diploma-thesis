int max3(int a, int b, int c)
{
	int max;

	if (a < b) {
		if (c < b) {
			max = b;
		} else {
			max = c;
		}
	} else {
		if (c < a) {
			max = a;
		} else {
			max = c;
		}
	}
	return max;
}
