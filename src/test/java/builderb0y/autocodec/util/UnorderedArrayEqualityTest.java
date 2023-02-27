package builderb0y.autocodec.util;

import org.junit.Test;

import static org.junit.Assert.*;

public class UnorderedArrayEqualityTest {

	@Test
	public void test() {
		this.check(new Object[0], new Object[0], true);
		//note that the time complexity of this extremely recursive
		//unit test is maxLength! * maxLength^maxLength * maxLength.
		//as such, we must ensure that maxLength is small,
		//or else we will be waiting a very *very* long time for results.
		//the time for maxLength = 7 is already 20 seconds.
		//6 seems to be < 1 second though.
		final int maxLength = 6;
		for (int length = 1; length < maxLength; length++) {
			Object[] array1 = new Object[length], array2 = new Object[length];
			this.generatePalletRecursive(array1, array2, 0, length);
		}
	}

	public void generatePalletRecursive(Object[] array1, Object[] array2, int start, int length) {
		if (start == length) {
			this.check(array1, array2, true);
			this.generatePermutationsRecursive(array1, array2, 0, length);
		}
		else {
			for (int number = 0; number < length; number++) {
				array1[start] = newObject(number);
				array2[start] = newObject(number);
				this.generatePalletRecursive(array1, array2, start + 1, length);
			}
		}
	}

	public void generatePermutationsRecursive(Object[] array1, Object[] array2, int start, int length) {
		if (start == length - 1) {
			this.check(array1, array2, true);
			for (int index = 0; index < length; index++) {
				Object old = array1[index];
				array1[index] = newObject(-1);
				this.check(array1, array2, false);
				array1[index] = old;

				old = array2[index];
				array2[index] = newObject(-1);
				this.check(array1, array2, false);
				array2[index] = old;
			}
		}
		else {
			for (int secondIndex = start; ++secondIndex < length;) {
				Object a = array2[start];
				Object b = array2[secondIndex];
				array2[start] = b;
				array2[secondIndex] = a;
				this.generatePermutationsRecursive(array1, array2, start + 1, length);
				array2[start] = a;
				array2[secondIndex] = b;
			}
		}
	}

	public void check(Object[] array1, Object[] array2, boolean expected) {
		assertEquals(expected, HashStrategies.unorderedArrayEqualsSmall(HashStrategies.defaultStrategy(), array1, array2));
		assertEquals(expected, HashStrategies.unorderedArrayEqualsSmall(HashStrategies.defaultStrategy(), array2, array1));
		assertEquals(expected, HashStrategies.unorderedArrayEqualsBig(HashStrategies.defaultStrategy(), array1, array2));
		assertEquals(expected, HashStrategies.unorderedArrayEqualsBig(HashStrategies.defaultStrategy(), array2, array1));
	}

	public static Object newObject(int number) {
		return number == 0 ? null : Integer.valueOf(number);
	}
}