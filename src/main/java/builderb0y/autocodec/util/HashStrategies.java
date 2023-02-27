package builderb0y.autocodec.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Hash.Strategy;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
a collection of utility methods for creating and deriving {@link Hash.Strategy}'s.
Strategy's created by this class make 3 important assumptions:
	1: the hash code of null is 0.
	2: null only equals itself.
	3: reference equality implies regular equality.
these contracts are stated or implied in various places, including
	{@link Object#equals(Object)},
	{@link Objects#equals(Object, Object)}, and
	{@link Objects#hashCode(Object)},
but they are not explicitly stated in {@link Hash.Strategy#equals(Object, Object)}.
so to be more specific, Strategy's returned by the methods
in this class make the assumption that no one would ever
want a Strategy where any of these contracts are violated.
Strategy's returned by the methods in this class
use these assumptions to implement fast paths.
additionally, these fast paths typically mean less
boilerplate work for callbacks used by such Strategy's.
see the docs on specific methods for information
on how these fast paths are implemented,
and what they mean for callbacks.
*/
public class HashStrategies {

	/**
	creates and returns a {@link Hash.Strategy} whose
	{@link Hash.Strategy#equals(Object, Object)} method
	delegates to hasher.{@link ToIntFunction#applyAsInt(Object)},
	and whose {@link Hash.Strategy#hashCode(Object)} method
	delegates to equalizer.{@link BiPredicate#test(Object, Object)}.

	example usage: {@code
		Hash.Strategy<String> strategy = HashStrategies.of(
			s -> s.toLowerCase().hashCode(),
			String::equalsIgnoreCase
		);
		assert strategy.hashCode("foo") == strategy.hashCode("FOO");
		assert strategy.equals("foo", "FOO");
	}

	technical details for the implementation of
	{@link Hash.Strategy#hashCode(Object)}
	on the returned Strategy:
		if the object is null, 0 is returned immediately,
		and hasher.{@link ToIntFunction#applyAsInt(Object)}
		is not called.

		as such, hasher.{@link ToIntFunction#applyAsInt(Object)}
		does not need to handle null inputs on its own.

	technical details for the implementation of
	{@link Hash.Strategy#equals(Object, Object)}
	on the returned Strategy:
		if the two objects have reference equality,
		including the case where both objects are null,
		true is returned immediately, and
		equalizer.{@link BiPredicate#test(Object, Object)}
		is not called.

		if exactly one of the objects is null,
		then false is returned immediately, and
		equalizer.{@link BiPredicate#test(Object, Object)}
		is not called.

		as such, equalizer.{@link BiPredicate#test(Object, Object)}
		does not need to handle null inputs on its own,
		nor does it need to check for reference equality.
	*/
	public static <T> Hash.@NotNull Strategy<@Nullable T> of(
		@NotNull ToIntFunction<? super @NotNull T> hasher,
		@NotNull BiPredicate<? super @NotNull T, ? super @NotNull T> equalizer
	) {
		return new Hash.Strategy<>() {

			@Override
			public int hashCode(@Nullable T o) {
				return o == null ? 0 : hasher.applyAsInt(o);
			}

			@Override
			public boolean equals(@Nullable T a, @Nullable T b) {
				if (a == b) return true;
				if (a == null || b == null) return false;
				return equalizer.test(a, b);
			}

			@Override
			public String toString() {
				return "HashStrategies.of(hasher: " + hasher + ", equalizer: " + equalizer + ')';
			}
		};
	}

	/**
	creates and returns a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method accumulates the hashes of all provided strategies.

	the final hash of an object is not specified,
	but it is guaranteed that the order in which strategies
	are provided to this method will not affect the final hash.
	in other words: {@code
		HashStrategies.allOf(strategyA, strategyB).hashCode(object)
		==
		HashStrategies.allOf(strategyB, strategyA).hashCode(object)
	}

	the returned Strategy's {@link Hash.Strategy#equals(Object, Object)}
	method returns true for any two objects a and b if, and only if,
	all of the provided strategies in the varargs array return true for a and b.

	example usage: {@code
		class Point {

			int x, y, z;

			Point(int x, int y, int z) {
				this.x = x;
				this.y = y;
				this.z = z;
			}
		}

		Hash.Strategy<Point> strategy = HashStrategies.allOf(
			HashStrategies.of(
				point -> point.x,
				(point1, point2) -> point1.x == point2.x
			),
			HashStrategies.of(
				point -> point.y,
				(point1, point2) -> point1.y == point2.y
			)
			//don't include z.
		);

		Point p1 = new Point(1, 2, 3);
		Point p2 = new Point(1, 2, 4);
		//p1 and p2 have different z values.
		assert strategy.hashCode(p1) == strategy.hashCode(p2);
		assert strategy.equals(p1, p2);
	}

	technical details for the implementation of
	{@link Hash.Strategy#hashCode(Object)}
	on the returned Strategy:
		if the object is null, 0 is returned immediately,
		and none of the provided strategies will have their
		{@link Hash.Strategy#hashCode(Object)} method called.

		as such, none of the provided Strategy's need to handle null
		inputs in their {@link Hash.Strategy#hashCode(Object)} method.

	technical details for the implementation of
	{@link Hash.Strategy#equals(Object, Object)}
	on the returned Strategy:
		if the two objects have reference equality,
		including the case where both objects are null,
		true is returned immediately,
		and none of the provided strategies will have their
		{@link Hash.Strategy#equals(Object, Object)} method called.

		if exactly one of the objects is null,
		then false is returned immediately,
		and none of the provided strategies will have their
		{@link Hash.Strategy#equals(Object, Object)} method called.

		if any of the provided strategies determines that the two objects are not equal,
		false is returned immediately, and none of the subsequent strategies will have their
		{@link Hash.Strategy#equals(Object, Object)} method called.

		as such, none of the provided Strategy's need to handle null
		inputs in their {@link Hash.Strategy#equals(Object, Object)} method,
		nor do they need to check for reference equality.
	*/
	@SafeVarargs
	public static <T> Hash.@NotNull Strategy<@Nullable T> allOf(
		Hash.@NotNull Strategy<? super @Nullable T> @NotNull ... strategies
	) {
		if (strategies.length == 0) {
			throw new IllegalArgumentException("Must provide at least 1 strategy.");
		}
		return new Hash.Strategy<>() {

			@Override
			public int hashCode(@Nullable T o) {
				if (o == null) return 0;
				int hash = HashCommon.mix(strategies[0].hashCode(o));
				for (int index = 1, length = strategies.length; index < length; index++) {
					hash += HashCommon.mix(strategies[index].hashCode(o));
				}
				return hash;
			}

			@Override
			public boolean equals(@Nullable T a, @Nullable T b) {
				if (a == b) return true;
				if (a == null || b == null) return false;
				for (Hash.Strategy<? super T> strategy : strategies) {
					if (!strategy.equals(a, b)) return false;
				}
				return true;
			}

			@Override
			public String toString() {
				return (
					Arrays
					.stream(strategies)
					//to aid in debugging, toString will handle and
					//report null strategies in the provided array.
					.map(String::valueOf)
					.collect(Collectors.joining(", ", "HashStrategies.allOf(", ")"))
				);
			}
		};
	}

	/**
	returns a {@link Hash.Strategy} which applies
	only to one specific property of its argument(s).
	that property is obtained via propertyGetter.{@link Function#apply(Object)},
	and the property itself is passed into propertyStrategy.

	example usage: {@code
		class Person {

			String name;

			Person(String name) {
				this.name = name;
			}
		}

		Hash.Strategy<Person> strategy = HashStrategies.map(
			HashStrategies.defaultStrategy(),
			person -> person.name
		);

		Person alex = new Person("Alex");
		Person bob  = new Person("Bob" );
		assert strategy.hashCode(alex) == "Alex".hashCode();
		assert strategy.hashCode(bob ) == "Bob" .hashCode();
		Person alex2 = new Person("Alex");
		assert  strategy.hashCode(alex) == strategy.hashCode(alex2);
		assert  strategy.equals(alex,  alex2);
		assert !strategy.equals(alex,  bob);
		assert !strategy.equals(alex2, bob);
	}

	technical details for the implementation of
	{@link Hash.Strategy#hashCode(Object)}
	on the returned Strategy:
		if the object is null, then 0 is returned immediately,
		and {@link Function#apply(Object) propertyGetter.apply()} is not called.

		if the object is non-null, then
		{@link Function#apply(Object) propertyGetter.apply()}
		is called, and the return value is passed
		directly into propertyStrategy.
		this is also the case when propertyGetter.apply() returns null.

		as such, {@link Function#apply(Object) propertyGetter.apply()}
		does not need to handle null inputs on its own, but
		{@link Hash.Strategy#hashCode(Object) propertyStrategy.hashCode()} does.

	technical details for the implementation of
	{@link Hash.Strategy#equals(Object, Object)}
	on the returned Strategy:
		if the two objects have reference equality,
		including the case where both objects are null,
		then true is returned immediately,
		and neither {@link Function#apply(Object) propertyGetter.apply()} nor
		{@link Hash.Strategy#equals(Object, Object) propertyStrategy.equals()}
		are called.

		if exactly one of the objects is null,
		then false is returned immediately,
		and neither {@link Function#apply(Object) propertyGetter.apply()} nor
		{@link Hash.Strategy#equals(Object, Object) propertyStrategy.equals()}
		are called.

		if both objects are non-null, then
		{@link Function#apply(Object) propertyGetter.apply()}
		is called on both objects, and the
		return values are passed directly into
		{@link Hash.Strategy#equals(Object, Object) propertyStrategy.equals()}.
		this includes the case where propertyGetter.apply()
		returns null for one or both objects.

		as such, {@link Function#apply(Object) propertyGetter.apply()}
		does not need to handle null inputs on its own, but
		{@link Hash.Strategy#equals(Object, Object) propertyStrategy.equals()} does.
	*/
	public static <T_From, T_To> Hash.@NotNull Strategy<@Nullable T_To> map(
		Hash.@NotNull Strategy<? super @Nullable T_From> propertyStrategy,
		@NotNull Function<? super @NotNull T_To, ? extends @Nullable T_From> propertyGetter
	) {
		return new Hash.Strategy<>() {

			@Override
			public int hashCode(@Nullable T_To o) {
				if (o == null) return 0;
				return propertyStrategy.hashCode(propertyGetter.apply(o));
			}

			@Override
			public boolean equals(@Nullable T_To a, @Nullable T_To b) {
				if (a == b) return true;
				if (a == null || b == null) return false;
				return propertyStrategy.equals(propertyGetter.apply(a), propertyGetter.apply(b));
			}

			@Override
			public String toString() {
				return "HashStrategies.map(propertyStrategy: " + propertyStrategy + ", propertyGetter: " + propertyGetter + ')';
			}
		};
	}

	/**
	constant for a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link Object#hashCode()},
	and whose {@link Hash.Strategy#equals(Object, Object)}
	method delegates to {@link Object#equals(Object)}.

	technical details for the implementation of
	{@link Hash.Strategy#hashCode(Object)}
	on this Strategy:
		if the object is null, 0 is returned immediately,
		and {@link Object#hashCode() o.hashCode()} is not called.

	technical details for the implementation of
	{@link Hash.Strategy#equals(Object, Object)}
	on this Strategy:
		if the two objects have reference equality,
		including the case where both objects are null,
		then true is returned immediately, and
		{@link Object#equals(Object) a.equals(b)}
		is not called.

		if exactly one of the objects is null,
		then false is returned immediately, and
		{@link Object#equals(Object) a.equals(b)}
		is not called.

		if both objects are non-null and the two
		objects do not have reference equality,
		then equality is determined by
		{@link Object#equals(Object) a.equals(b)}.
		the contract on {@link Object#equals(Object)}
		states that this should be equivalent to b.equals(a).
	*/
	public static final Hash.@NotNull Strategy<@Nullable Object> DEFAULT_STRATEGY = new Hash.Strategy<>() {

		@Override
		public int hashCode(@Nullable Object o) {
			return o == null ? 0 : o.hashCode();
		}

		@Override
		public boolean equals(@Nullable Object a, @Nullable Object b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			return a.equals(b);
		}

		@Override
		public String toString() {
			return "HashStrategies.DEFAULT_STRATEGY";
		}
	};

	/**
	returns a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link T#hashCode()},
	and whose {@link Hash.Strategy#equals(Object, Object)}
	method delegates to {@link T#equals(Object)}.

	technical details for the implementation of
	{@link Hash.Strategy#hashCode(Object)}
	on the returned Strategy:
		if the object is null, 0 is returned immediately,
		and {@link Object#hashCode() o.hashCode()} is not called.

	technical details for the implementation of
	{@link Hash.Strategy#equals(Object, Object)}
	on the returned Strategy:
		if the two objects have reference equality,
		including the case where both objects are null,
		then true is returned immediately, and
		{@link Object#equals(Object) a.equals(b)}
		is not called.

		if exactly one of the objects is null,
		then false is returned immediately, and
		{@link Object#equals(Object) a.equals(b)}
		is not called.

		if both objects are non-null and the two
		objects do not have reference equality,
		then equality is determined by
		{@link Object#equals(Object) a.equals(b)}.
		the contract on {@link Object#equals(Object)}
		states that this should be equivalent to b.equals(a).
	*/
	@SuppressWarnings("unchecked")
	public static <T> Hash.@NotNull Strategy<@Nullable T> defaultStrategy() {
		return (Hash.Strategy<T>)(DEFAULT_STRATEGY);
	}

	/**
	constant for a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link System#identityHashCode(Object)},
	and whose {@link Hash.Strategy#equals(Object, Object)}
	method checks only for reference equality via the == operator.
	*/
	public static final Hash.@NotNull Strategy<@Nullable Object> IDENTITY_STRATEGY = new Hash.Strategy<>() {

		@Override
		public int hashCode(@Nullable Object o) {
			return System.identityHashCode(o);
		}

		@Override
		public boolean equals(@Nullable Object a, @Nullable Object b) {
			return a == b;
		}

		@Override
		public String toString() {
			return "HashStrategies.IDENTITY_STRATEGY";
		}
	};

	/**
	returns a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link System#identityHashCode(Object)},
	and whose {@link Hash.Strategy#equals(Object, Object)}
	method checks only for reference equality via the == operator.
	*/
	@SuppressWarnings("unchecked")
	public static <T> Hash.@NotNull Strategy<@Nullable T> identityStrategy() {
		return (Hash.Strategy<T>)(IDENTITY_STRATEGY);
	}

	/**
	returns a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link #orderedArrayHashCode(Strategy, Object[])},
	using the provided (elementStrategy) as the first argument,
	and whose {@link Hash.Strategy#equals(Object, Object)}
	method delegates to {@link #orderedArrayEquals(Strategy, Object[], Object[])},
	again using the provided (elementStrategy) as the first argument.
	*/
	public static <T> Hash.@NotNull Strategy<T @Nullable []> orderedArrayStrategy(
		Hash.@NotNull Strategy<? super T> elementStrategy
	) {
		return new Hash.Strategy<>() {

			@Override
			public int hashCode(T @Nullable [] o) {
				return orderedArrayHashCode(elementStrategy, o);
			}

			@Override
			public boolean equals(T @Nullable [] a, T @Nullable [] b) {
				return orderedArrayEquals(elementStrategy, a, b);
			}

			@Override
			public String toString() {
				return "HashStrategies.orderedArrayStrategy(" + elementStrategy + ')';
			}
		};
	}

	/**
	returns a hash which depends on all the
	objects in the provided varargs array,
	including their order.

	technical details:
		if the provided varargs array is null or has a length of 0,
		then the returned hash code will be 0.

		otherwise, strategy.{@link Hash.Strategy#hashCode(Object)}
		is invoked on each element in the array,
		and the return value of that is accumulated into the
		final hash in a way which is not directly specified,
		but is guaranteed to depend on the order
		in which objects are present in the array.
	*/
	@SafeVarargs
	public static <T> int orderedArrayHashCode(
		Hash.@NotNull Strategy<? super T> strategy,
		T @Nullable ... objects
	) {
		if (objects == null) return 0;
		int length = objects.length;
		if (length == 0) return 0;
		int hash = HashCommon.mix(strategy.hashCode(objects[0]));
		for (int index = 1; index < length; index++) {
			hash = HashCommon.mix(hash + strategy.hashCode(objects[index]));
		}
		return hash;
	}

	/**
	returns true if the two arrays have the same
	contents in the same order, false otherwise.
	equality of elements in the arrays is determined by the provided Strategy.

	if the provided Strategy is the {@link #defaultStrategy()},
	then this method is equivalent to {@link Arrays#equals(Object[], Object[])}.

	technical details:
		if the two arrays have reference equality,
		including the case where both arrays are null,
		then true is returned immediately,
		and strategy.{@link Hash.Strategy#equals(Object, Object)}
		will not be invoked on any of the elements in the arrays.

		if exactly one of the arrays is null,
		then false is returned immediately,
		and strategy.{@link Hash.Strategy#equals(Object, Object)}
		will not be invoked on any of the elements in the non-null array.

		if both of the arrays are non-null,
		but have different lengths,
		then false is returned immediately,
		and strategy.{@link Hash.Strategy#equals(Object, Object)}
		will not be invoked on any of the elements in the arrays.

		if the strategy determines that any of
		the elements in the arrays are unequal,
		then false is returned at that point,
		and strategy.{@link Hash.Strategy#equals(Object, Object)}
		will not be invoked on any of the remaining elements in the arrays.
	*/
	public static <T> boolean orderedArrayEquals(
		Hash.@NotNull Strategy<? super T> strategy,
		T @Nullable [] array1,
		T @Nullable [] array2
	) {
		if (array1 == array2) return true;
		if (array1 == null || array2 == null) return false;
		int length = array1.length;
		if (array2.length != length) return false;
		for (int index = 0; index < length; index++) {
			if (!strategy.equals(array1[index], array2[index])) return false;
		}
		return true;
	}

	/**
	returns a {@link Hash.Strategy} whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link #unorderedArrayHashCode(Strategy, Object[])},
	using the provided (elementStrategy) as the first argument,
	and whose {@link Hash.Strategy#equals(Object, Object)}
	method delegates to {@link #unorderedArrayEqualsAuto(Strategy, Object[], Object[])},
	again using the provided (elementStrategy) as the first argument.
	*/
	public static <T> Hash.@NotNull Strategy<T @Nullable []> unorderedArrayStrategy(
		Hash.@NotNull Strategy<? super T> elementStrategy
	) {
		return new Hash.Strategy<>() {

			@Override
			public int hashCode(T @Nullable [] array) {
				return unorderedArrayHashCode(elementStrategy, array);
			}

			@Override
			public boolean equals(T @Nullable [] a, T @Nullable [] b) {
				return unorderedArrayEqualsAuto(elementStrategy, a, b);
			}

			@Override
			public String toString() {
				return "HashStrategies.unorderedArrayStrategy(" + elementStrategy + ')';
			}
		};
	}

	/**
	returns a hash which depends on all the
	objects in the provided varargs array,
	but not their order.

	technical details:
		if the provided varargs array is null or has a length of 0,
		then the returned hash code will be 0.

		otherwise, strategy.{@link Hash.Strategy#hashCode(Object)}
		is invoked on each element in the array,
		and the return value of that is accumulated into the
		final hash in a way which is not directly specified,
		but is guaranteed to not depend on the order
		in which objects are present in the array.
	*/
	@SafeVarargs
	public static <T> int unorderedArrayHashCode(
		Hash.@NotNull Strategy<? super T> strategy,
		T @Nullable ... objects
	) {
		if (objects == null) return 0;
		int hash = 0;
		for (T object : objects) {
			hash += HashCommon.mix(strategy.hashCode(object));
		}
		return hash;
	}

	/**
	returns true if the two arrays have the same contents,
	but in any order (including the same order), false otherwise.
	equality of elements in the arrays is determined by the provided Strategy.

	for example, the following arrays are considered equal by this method:
		[]          == []
		[ 0 ]       == [ 0 ]
		[ 0, 1 ]    == [ 0, 1 ]
		[ 0, 1 ]    == [ 1, 0 ]
		[ 0, 1, 2 ] == [ 0, 1, 2 ]
		[ 0, 1, 2 ] == [ 2, 1, 0 ]
		[ 0, 0, 1 ] == [ 1, 0, 0 ]
	by contrast, the following arrays are considered NOT equal by this method:
		[]          != [ 0 ]
		[ 0 ]       != [ 1 ]
		[ 0, 0 ]    != [ 0, 1 ]
		[ 0, 0, 1 ] != [ 0, 0, 0 ]
		[ 0, 0, 1 ] != [ 0, 0, 1, 1 ]
		[ 0, 0, 1 ] != [ 0, 1 ]

	@apiNote this method was designed to minimize memory usage,
	but it is not particularly fast for large arrays.
		for arrays with reference equality
		(in other words, array1 == array2),
		a fast path is taken and the time
		and space complexity are both O(1).

		for arrays with different lengths,
		a fast path is taken, and the time
		and space complexity are both O(1).

		for arrays with value equality (in other words,
		the two arrays are equal according to
		{@link #orderedArrayEquals(Strategy, Object[], Object[])}),
		the time complexity is O(n), and the space complexity is O(1).

		for all other arrays, the time complexity is O(n^2),
		and the space complexity is O(n).

		note however that this method attempts to check
		for ordered equality first, before falling back
		on unordered equality only if a mismatch is found.
		as such, if the two arrays have ordered equality on
		low indexes and unordered equality on higher indexes,
		then the time complexity will be somewhere between O(n) and O(n^2),
		and the space complexity will be somewhere between O(1) and O(n).

	because the worst case time complexity is O(n^2), this method
	is only recommended for small arrays. large arrays should use
	{@link #unorderedArrayEqualsBig(Strategy, Object[], Object[])} instead.
	*/
	public static <T> boolean unorderedArrayEqualsSmall(
		Hash.@NotNull Strategy<? super T> strategy,
		T @Nullable [] array1,
		T @Nullable [] array2
	) {
		if (array1 == array2) return true;
		if (array1 == null || array2 == null) return false;
		int length = array1.length;
		if (array2.length != length) return false;
		/**
		requirements:
			every element in array2 must
			equal a UNIQUE element in array1.
			if array2 contains duplicate elements,
			we must ensure that array1
			contains the same duplicated elements,
			the same number of times, but in any order.

		general algorithm:
			we first loop through array2.
				we then try to find a matching element in array1.
					if such a match is found, it is removed from array1 to prevent
					it from counting as a match for a different element in array2.
						this is achieved by replacing it with array1[array1Length - 1],
						and then decrementing array1Length, since this will
						be faster than shifting every subsequent element down.
						however, array1 must be cloned before this operation can be performed,
						to prevent modifications to array1 from being visible to the caller.
						we also want to avoid cloning array1 unless absolutely necessary.
						more on this point is explained later.
					otherwise, we return false.
			if all elements in array2 have a match in array1, we return true.

		the strategy we use to avoid cloning array1
		actually doubles as a potential optimization
		in the case that array2 actually equals array1,
		including being in the same order:
			we first loop through to check ordered equality,
			and only if a mismatch is found, we clone
			array1 and start using the unordered algorithm.
			oh, and since everything before that mismatch did successfully match,
			we don't need to clone the *entire* array1.
			we only need to clone the indexes greater than or equal to the first mismatch.
			as one final optimization, we also only perform this cloning if the
			first mismatch actually has a match elsewhere in array1.

		I put *way* too much fucking effort into this algorithm.
		*/
		for (int firstNonMatch = 0; firstNonMatch < length; firstNonMatch++) {
			if (!strategy.equals(array1[firstNonMatch], array2[firstNonMatch])) {
				//found a mismatch! now we proceed with the unordered algorithm.
				return implUnorderedArrayEqualsSmall(strategy, array1, array2, firstNonMatch, length);
			}
		}
		return true;
	}

	public static <T> boolean implUnorderedArrayEqualsSmall(
		Hash.@NotNull Strategy<? super T> strategy,
		T @NotNull [] array1,
		T @NotNull [] array2,
		int firstNonMatch,
		int length
	) {
		@Nullable T target = array2[firstNonMatch];
		@Nullable T @NotNull [] array1Clone;
		int clone1Length;
		foundFirstMismatch: {
			for (int index1 = firstNonMatch; ++index1 < length;) {
				if (strategy.equals(target, array1[index1])) {
					array1Clone = Arrays.copyOfRange(array1, firstNonMatch, length);
					clone1Length = array1Clone.length;
					array1Clone[index1 - firstNonMatch] = array1Clone[--clone1Length];
					break foundFirstMismatch;
				}
			}
			return false;
		}
		nextTarget:
		for (int index2 = firstNonMatch; ++index2 < length;) {
			target = array2[index2];
			for (int index1 = 0; index1 < clone1Length; index1++) {
				if (strategy.equals(target, array1Clone[index1])) {
					array1Clone[index1] = array1Clone[--clone1Length];
					continue nextTarget;
				}
			}
			//no match found.
			return false;
		}
		return true;
	}

	/**
	returns true if the two arrays have the same contents,
	but in any order (including the same order), false otherwise.
	equality of elements in the arrays is determined by the provided Strategy.

	for example, the following arrays are considered equal by this method:
		[]          == []
		[ 0 ]       == [ 0 ]
		[ 0, 1 ]    == [ 0, 1 ]
		[ 0, 1 ]    == [ 1, 0 ]
		[ 0, 1, 2 ] == [ 0, 1, 2 ]
		[ 0, 1, 2 ] == [ 2, 1, 0 ]
		[ 0, 0, 1 ] == [ 1, 0, 0 ]
	by contrast, the following arrays are considered NOT equal by this method:
		[]          != [ 0 ]
		[ 0 ]       != [ 1 ]
		[ 0, 0 ]    != [ 0, 1 ]
		[ 0, 0, 1 ] != [ 0, 0, 0 ]
		[ 0, 0, 1 ] != [ 0, 0, 1, 1 ]
		[ 0, 0, 1 ] != [ 0, 1 ]

	this method is faster than
	{@link #unorderedArrayEqualsSmall(Strategy, Object[], Object[])}
	for large arrays (O(n) instead of O(n^2)),
	but will also use more memory.
	the memory *complexity* still scales with O(n),
	but will still be higher in general.

	note however that for small arrays,
	{@link #unorderedArrayEqualsSmall(Strategy, Object[], Object[])}
	is likely to be faster than this method due to
	the fact that that method does not need to compute
	hash codes of array elements like this method does.

	this method attempts to check for ordered equality first,
	before falling back on unordered equality only if a mismatch is found.
	as such, if the two arrays have ordered equality on all indexes,
	then the time complexity will be O(n) and the space complexity will be O(1).
	additionally, if the two arrays have ordered equality on
	low indexes and unordered equality on higher indexes,
	then the time complexity will still be O(n),
	but the space complexity will be somewhere between O(1) and O(n).
	*/
	public static <T> boolean unorderedArrayEqualsBig(
		Hash.@NotNull Strategy<? super T> strategy,
		T @Nullable [] array1,
		T @Nullable [] array2
	) {
		if (array1 == array2) return true;
		if (array1 == null || array2 == null) return false;
		int length = array1.length;
		if (array2.length != length) return false;
		/**
		algorithm:
			convert array1 to a Object2IntMap where the key is each element in array1,
			and the value is the number of times that element is present in array1.
			for each element in array2, try to find it in map1.
			if found, decrement the associated value.
			if not found, or associated value was already 0, return false.

		ordered fast path:
			if array1 and array2 contain the same elements in the same order,
			then no Object2IntMap is allocated. more specifically,
			we loop through to find the first non-matching index,
			and only allocate an Object2IntMap for subsequent indexes.
		*/
		for (int firstNonMatch = 0; firstNonMatch < length; firstNonMatch++) {
			if (!strategy.equals(array1[firstNonMatch], array2[firstNonMatch])) {
				//found a mismatch! proceed with unordered compare now.
				return implUnorderedArrayEqualsBig(strategy, array1, array2, firstNonMatch, length);
			}
		}
		return true;
	}

	public static <T> boolean implUnorderedArrayEqualsBig(
		Hash.@NotNull Strategy<? super T> strategy,
		T @NotNull [] array1,
		T @NotNull [] array2,
		int firstNonMatch,
		int length
	) {
		Object2IntOpenCustomHashMap<T> elementCounts = new Object2IntOpenCustomHashMap<>(length - firstNonMatch, strategy);
		for (int index = firstNonMatch; index < length; index++) {
			elementCounts.addTo(array1[index], 1);
		}
		for (int index = firstNonMatch; index < length; index++) {
			if (elementCounts.addTo(array2[index], -1) <= 0) return false;
		}
		return true;
	}

	public static <T> boolean unorderedArrayEqualsAuto(
		Hash.@NotNull Strategy<? super T> strategy,
		T @Nullable [] array1,
		T @Nullable [] array2
	) {
		if (array1 == array2) return true;
		if (array1 == null || array2 == null) return false;
		int length = array1.length;
		if (array2.length != length) return false;

		for (int firstNonMatch = 0; firstNonMatch < length; firstNonMatch++) {
			if (!strategy.equals(array1[firstNonMatch], array2[firstNonMatch])) {
				//found a mismatch! proceed with unordered compare now.
				int remaining = length - firstNonMatch;
				return (
					remaining > 4
					? implUnorderedArrayEqualsSmall(strategy, array1, array2, firstNonMatch, length)
					: implUnorderedArrayEqualsBig(strategy, array1, array2, firstNonMatch, length)
				);
			}
		}
		return true;
	}

	public static abstract class NamedHashStrategy<T> implements Hash.Strategy<T> {

		public @NotNull String toString;

		public NamedHashStrategy(@NotNull String toString) {
			this.toString = toString;
		}

		@Override
		public String toString() {
			return this.toString;
		}
	}
}