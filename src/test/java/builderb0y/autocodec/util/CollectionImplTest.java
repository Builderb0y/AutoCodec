package builderb0y.autocodec.util;

import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.junit.Test;

import static org.junit.Assert.*;

public class CollectionImplTest {

	@Test
	public void testConstructor() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		assertEquals(6, collection.size());
		for (int i = 1; i <= 3; i++) {
			assertEquals(i, collection.count(i));
		}
		collection.clear();
		assertTrue(collection.isEmpty());
		collection.add(1);
		collection.add(2);
		collection.add(2);
		collection.add(3);
		collection.add(3);
		collection.add(3);
		assertEquals(6, collection.size());
		for (int i = 1; i <= 3; i++) {
			assertEquals(i, collection.count(i));
		}
	}

	@Test
	public void testIterator() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		Object2IntOpenHashMap<Integer> map = new Object2IntOpenHashMap<>(3);
		for (Integer i : collection) {
			map.addTo(i, 1);
		}
		assertEquals(collection.map, map);
	}

	@Test
	public void testRemove() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));

		collection.remove(1);
		assertFalse(collection.contains(1));

		collection.remove(2);
		assertTrue(collection.contains(2));
		collection.remove(2);
		assertFalse(collection.contains(2));

		collection.remove(3);
		assertTrue(collection.contains(3));
		collection.remove(3);
		assertTrue(collection.contains(3));
		collection.remove(3);
		assertFalse(collection.contains(3));

		assertTrue(collection.isEmpty());
	}

	@Test
	public void testToArray() {
		Integer[] integers = new Integer[] { 1, 2, 2, 3, 3, 3 };
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));

		assertTrue(HashStrategies.unorderedArrayEqualsAuto(HashStrategies.defaultStrategy(), integers, collection.toArray(new Integer[6])));
	}

	@Test
	public void testContainsAll() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		assertTrue(collection.containsAll(List.of(1)));
		assertTrue(collection.containsAll(List.of(1, 2)));
		assertTrue(collection.containsAll(List.of(1, 2, 3)));
	}

	@Test
	public void testRemoveAll() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));

		assertTrue(collection.contains(1));
		assertTrue(collection.removeAll(1));
		assertFalse(collection.contains(1));
		assertFalse(collection.removeAll(1));

		assertTrue(collection.contains(2));
		assertTrue(collection.removeAll(2));
		assertFalse(collection.contains(2));
		assertFalse(collection.removeAll(2));

		assertTrue(collection.contains(3));
		assertTrue(collection.removeAll(3));
		assertFalse(collection.contains(3));
		assertFalse(collection.removeAll(3));

		assertTrue(collection.isEmpty());
	}

	@Test
	public void testRemoveAllBulk() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		collection.removeAll(Set.of(1, 2, 3));
		assertTrue(collection.isEmpty());
	}

	@Test
	public void testRetainAll() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		collection.retainAll(Set.of(1, 3));
		assertEquals(1, collection.count(1));
		assertFalse(collection.contains(2));
		assertEquals(3, collection.count(3));
	}

	@Test
	public void testRemoveIf() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		collection.removeIf((Integer i) -> (i & 1) == 0);
		assertEquals(1, collection.count(1));
		assertFalse(collection.contains(2));
		assertEquals(3, collection.count(3));
	}

	@Test
	public void testToString() {
		CollectionImpl<Integer> collection = new CollectionImpl<>(List.of(1, 2, 2, 3, 3, 3));
		assertTrue(
			List.of(
				"{ 1x 1, 2x 2, 3x 3 }",
				"{ 1x 1, 3x 3, 2x 2 }",
				"{ 2x 2, 1x 1, 3x 3 }",
				"{ 2x 2, 3x 3, 1x 1 }",
				"{ 3x 3, 1x 1, 2x 2 }",
				"{ 3x 3, 2x 2, 1x 1 }"
			)
			.contains(collection.toString())
		);
	}
}