package builderb0y.autocodec.reflection;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.Mirror;
import builderb0y.autocodec.util.*;
import builderb0y.autocodec.util.TypeFormatter.TypeFormatterAppendable;

/**
a holder for annotations, similar to {@link AnnotatedElement}.
unlike {@link AnnotatedElement}, one instance
of this class is used for declared annotations,
and a different instance is used for inherited annotations.
this allows us to have a common set of logic for both.
additionally, unlike {@link AnnotatedElement#getAnnotations()},
our {@link #getAll()} method will return {@link Repeatable repeated} annotations too.

AnnotationContainer's can be created from {@link AnnotatedElement AnnotatedElement's}
via the factory methods {@link #from(AnnotatedElement)} and {@link #fromDeclared(AnnotatedElement)}.
alternatively, an AnnotationContainer can also be created directly from
an array of annotations via the factory method {@link #of(Annotation...)}.
*/
public class AnnotationContainer implements TypeFormatterAppendable {

	public static final @NotNull ObjectArrayFactory<AnnotationContainer> ARRAY_FACTORY = ArrayFactories.ANNOTATION_CONTAINER;
	public static final @NotNull AnnotationContainer EMPTY_ANNOTATION_CONTAINER = new AnnotationContainer(ArrayFactories.ANNOTATION.empty());

	/**
	{@link Hash.Strategy} whose {@link Hash.Strategy#equals(Object, Object)}
	method delegates to {@link #equalsOrdered(AnnotationContainer)},
	and whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link #hashCodeOrdered()}.
	@see #equalsOrdered(AnnotationContainer)
	@see #hashCodeOrdered()
	*/
	public static final Hash.@NotNull Strategy<@Nullable AnnotationContainer>
		ORDERED_HASH_STRATEGY = HashStrategies.of(
			AnnotationContainer::hashCodeOrdered,
			AnnotationContainer::equalsOrdered
		);

	/**
	{@link Hash.Strategy} whose {@link Hash.Strategy#equals(Object, Object)}
	method delegates to {@link #equalsUnordered(AnnotationContainer)},
	and whose {@link Hash.Strategy#hashCode(Object)}
	method delegates to {@link #hashCodeUnordered()}.
	@see #equalsUnordered(AnnotationContainer)
	@see #hashCodeUnordered()
	*/
	public static final Hash.@NotNull Strategy<@Nullable AnnotationContainer>
		UNORDERED_HASH_STRATEGY = HashStrategies.of(
			AnnotationContainer::hashCodeUnordered,
			AnnotationContainer::equalsUnordered
		);

	/**
	the list of annotations held in this container,
	including unwrapped {@link Repeatable} annotations.
	we use a simple array here instead of a
	Map<Class<? extends Annotation>, Annotation[]>
	because we expect that most annotated elements
	will not contain a large number of annotations.
	*/
	public final @NotNull Annotation @NotNull [] annotations;

	public AnnotationContainer(@NotNull Annotation @NotNull [] annotations) {
		this(true, annotations);
	}

	public AnnotationContainer(boolean flatten, @NotNull Annotation @NotNull [] annotations) {
		this.annotations = flatten ? flatten(annotations) : annotations;
	}

	public static @NotNull Annotation @NotNull [] flatten(@NotNull Annotation @NotNull [] annotations) {
		return RepeatableCache.flatten(annotations);
	}

	public static @NotNull AnnotationContainer of(@NotNull Annotation @NotNull ... annotations) {
		return annotations.length == 0 ? EMPTY_ANNOTATION_CONTAINER : new AnnotationContainer(annotations);
	}

	public static @NotNull AnnotationContainer of(boolean flatten, @NotNull Annotation @NotNull ... annotations) {
		return annotations.length == 0 ? EMPTY_ANNOTATION_CONTAINER : new AnnotationContainer(flatten, annotations);
	}

	public static @NotNull AnnotationContainer from(@Nullable AnnotatedElement element) {
		return element == null ? EMPTY_ANNOTATION_CONTAINER : of(element.getAnnotations());
	}

	public static @NotNull AnnotationContainer fromDeclared(@Nullable AnnotatedElement element) {
		return element == null ? EMPTY_ANNOTATION_CONTAINER : of(element.getDeclaredAnnotations());
	}

	public static @NotNull AnnotationContainer fromAll(@Nullable AnnotatedElement @Nullable ... elements) {
		if (elements == null) return EMPTY_ANNOTATION_CONTAINER;
		else return switch (elements.length) {
			case 0 -> EMPTY_ANNOTATION_CONTAINER;
			case 1 -> from(elements[0]);
			default -> of(
				false,
				Arrays
				.stream(elements)
				.filter(Objects::nonNull)
				.flatMap((AnnotatedElement element) -> Arrays.stream(flatten(element.getAnnotations())))
				.toArray(Annotation[]::new)
			);
		};
	}

	public static @NotNull AnnotationContainer fromAllDeclared(@Nullable AnnotatedElement @Nullable ... elements) {
		if (elements == null) return EMPTY_ANNOTATION_CONTAINER;
		else return switch (elements.length) {
			case 0 -> EMPTY_ANNOTATION_CONTAINER;
			case 1 -> fromDeclared(elements[0]);
			default -> of(
				false,
				Arrays
				.stream(elements)
				.filter(Objects::nonNull)
				.flatMap((AnnotatedElement element) -> Arrays.stream(flatten(element.getDeclaredAnnotations())))
				.toArray(Annotation[]::new)
			);
		};
	}

	/** returns true if there is at least one annotation in this container, false otherwise. */
	public boolean hasAny() {
		return this.count() != 0;
	}

	/**
	similar to {@link AnnotatedElement#isAnnotationPresent(Class)},
	this method returns true if we have at least 1 annotation whose
	{@link Annotation#annotationType() type} is (annotationClass),
	and false otherwise.
	unlike {@link AnnotatedElement#isAnnotationPresent(Class)},
	we distinguish between declared annotations and inherited annotations.
	if this AnnotationContainer represents declared annotations,
	then this method checks if a declared annotation of the given type is present.
	otherwise, this method is equivalent to {@link AnnotatedElement#isAnnotationPresent(Class)}.
	*/
	public <A extends Annotation> boolean has(@NotNull Class<A> annotationClass) {
		for (Annotation annotation : this.annotations) {
			if (annotation.annotationType() == annotationClass) {
				return true;
			}
		}
		return false;
	}

	/** returns the number of annotations in this container. */
	public int count() {
		return this.annotations.length;
	}

	/** returns true if there are no annotations in this container, false otherwise. */
	public boolean isEmpty() {
		return this.count() == 0;
	}

	/**
	returns the number of annotations in this AnnotationContainer
	whose {@link Annotation#annotationType() type} is (annotationClass).
	this method is mostly just useful for annotations which are {@link Repeatable}.
	*/
	public <A extends Annotation> int count(@NotNull Class<A> annotationClass) {
		int count = 0;
		for (Annotation annotation : this.annotations) {
			if (annotation.annotationType() == annotationClass) {
				count++;
			}
		}
		return count;
	}

	/**
	similar to {@link AnnotatedElement#getAnnotation(Class)}
	or {@link AnnotatedElement#getDeclaredAnnotation(Class)},
	this method returns the first annotation whose {@link Annotation#annotationType()}
	is (annotationClass), or null if no such annotation exists.
	*/
	public <A extends Annotation> @Nullable A getFirst(@NotNull Class<A> annotationClass) {
		for (Annotation annotation : this.annotations) {
			if (annotation.annotationType() == annotationClass) {
				return annotationClass.cast(annotation);
			}
		}
		return null;
	}

	/**
	similar to {@link AnnotatedElement#getAnnotations()}
	or {@link AnnotatedElement#getDeclaredAnnotations()},
	this method returns an array containing all the annotations
	in this container, in the order in which they are declared.
	if any of the annotations present at creation time repeated,
	they will be included in the returned array after their container.
	for example, given the annotation structure: {@code
		@Repeatable(B.class)
		@interface A {
			String value();
		}

		@interface B {
			A[] value();
		}

		@A("foo")
		@A("bar")
		class C {}
	}
	the array returned by {@code
		AnnotationContainer.of(C.class).getAll()
	}
	will return the following annotations: {@code
		@B(value = { @A("foo"), @A("bar") })
		@A("foo")
		@A("bar")
	}

	@apiNote the returned array is NOT defensively copied!
	as such, it should not be modified by the caller!
	*/
	public @NotNull Annotation @NotNull [] getAll() {
		return this.annotations;
	}

	/**
	similar to {@link AnnotatedElement#getAnnotationsByType(Class)}
	or {@link AnnotatedElement#getDeclaredAnnotationsByType(Class)},
	this method returns an array containing all the annotations
	in this container whose {@link Annotation#annotationType()}
	is (annotationClass), in the order in which they are declared.

	@apiNote at the time of writing this, the implementation of this
	method creates a new array to return every time it is called,
	but future implementations may change this to possibly
	return a shared array for each (annotationClass) instead.
	as such, the returned array should not be modified by the caller.
	*/
	@SuppressWarnings("unchecked")
	public <A extends Annotation> @NotNull A @NotNull [] getAll(@NotNull Class<A> annotationClass) {
		A first = null;
		Annotation[] annotations = this.annotations;
		int length = annotations.length;
		for (int index = 0; index < length; index++) {
			if (annotations[index].annotationType() == annotationClass) {
				if (first != null) {
					int count = 2; //include first and annotations[index].
					for (int countIndex = index; ++countIndex < length;) {
						if (annotations[countIndex].annotationType() == annotationClass) {
							count++;
						}
					}
					A[] array = (A[])(Array.newInstance(annotationClass, count));
					array[0] = first;
					array[1] = annotationClass.cast(annotations[index]);
					int writeIndex = 2;
					for (int readIndex = index; ++readIndex < length;) {
						if (annotations[readIndex].annotationType() == annotationClass) {
							array[writeIndex++] = annotationClass.cast(annotations[readIndex]);
						}
					}
					assert writeIndex == count;
					return array;
				}
				else {
					first = annotationClass.cast(annotations[index]);
				}
			}
		}
		if (first != null) {
			A[] array = (A[])(Array.newInstance(annotationClass, 1));
			array[0] = first;
			return array;
		}
		else {
			return (A[])(Array.newInstance(annotationClass, 0));
		}
	}

	/**
	returns an array containing all the annotations in
	this container which match the given predicate.
	*/
	public @NotNull Annotation @NotNull [] getAll(@NotNull Predicate<? super Annotation> predicate) {
		Annotation first = null;
		Annotation[] annotations = this.annotations;
		int length = annotations.length;
		for (int index = 0; index < length; index++) {
			if (predicate.test(annotations[index])) {
				if (first != null) {
					int count = 2; //include first and annotations[index].
					for (int countIndex = index; ++countIndex < length;) {
						if (predicate.test(annotations[countIndex])) {
							count++;
						}
					}
					Annotation[] array = new Annotation[count];
					array[0] = first;
					array[1] = annotations[index];
					int writeIndex = 2;
					for (int readIndex = index; ++readIndex < length;) {
						if (predicate.test(annotations[readIndex])) {
							array[writeIndex++] = annotations[readIndex];
						}
					}
					assert writeIndex == count;
					return array;
				}
				else {
					first = annotations[index];
				}
			}
		}
		if (first != null) {
			Annotation[] array = new Annotation[1];
			array[0] = first;
			return array;
		}
		else {
			return ArrayFactories.ANNOTATION.empty();
		}
	}

	/**
	returns a {@link Object#hashCode() hash code}
	for this AnnotationContainer which depends
	on the order in which annotations are declared.

	@see #ORDERED_HASH_STRATEGY
	@see #equalsOrdered(AnnotationContainer)
	*/
	public int hashCodeOrdered() {
		return HashStrategies.orderedArrayHashCode(
			HashStrategies.defaultStrategy(),
			this.annotations
		);
	}

	/**
	returns a {@link Object#hashCode() hash code}
	for this AnnotationContainer which does NOT depend
	on the order in which annotations are declared.

	@see #UNORDERED_HASH_STRATEGY
	@see #equalsUnordered(AnnotationContainer)
	*/
	public int hashCodeUnordered() {
		return HashStrategies.unorderedArrayHashCode(
			HashStrategies.defaultStrategy(),
			this.annotations
		);
	}

	/**
	returns true if this AnnotationContainer has
	the same annotations as that AnnotationContainer,
	in the same order. false otherwise.

	@see #ORDERED_HASH_STRATEGY
	@see #hashCodeOrdered()
	*/
	public boolean equalsOrdered(@NotNull AnnotationContainer that) {
		return Arrays.equals(this.annotations, that.annotations);
	}

	/**
	returns true if this AnnotationContainer has
	the same annotations as that AnnotationContainer,
	in any order, including the same order. false otherwise.

	@see #UNORDERED_HASH_STRATEGY
	@see #hashCodeUnordered()
	*/
	public boolean equalsUnordered(@NotNull AnnotationContainer that) {
		return HashStrategies.unorderedArrayEqualsSmall(
			HashStrategies.defaultStrategy(),
			this.annotations,
			that.annotations
		);
	}

	@Override
	public void appendTo(TypeFormatter formatter) {
		if (formatter.annotations) {
			Annotation[] annotations = this.annotations;
			int length = annotations.length;
			if (length != 0) {
				formatter.append(annotations[0]);
				for (int index = 1; index < length; index++) {
					formatter.append(' ').append(annotations[index]);
				}
			}
		}
	}

	@Override
	public String toString() {
		if (this.isEmpty()) return "";
		return new TypeFormatter(this.count() << 6).annotations(true).simplify(false).append(this).toString();
	}

	/** default implementation simply delegates to {@link #hashCodeOrdered()}. */
	@Override
	public int hashCode() {
		return this.hashCodeOrdered();
	}

	/** default implementation simply delegates to {@link #equalsOrdered(AnnotationContainer)}. */
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AnnotationContainer that && this.equalsOrdered(that);
	}

	/**
	logic for processing {@link Repeatable} annotations.
	specifically, calling {@link AnnotatedElement#getAnnotationsByType(Class)}
	with a type which itself is meta-annotated with {@link Repeatable}
	will return an array of annotations of that type, as expected.
	however, those annotations will NOT be present in the array
	returned by {@link AnnotatedElement#getAnnotations()}.
	instead, that array will only contain the
	{@link Repeatable#value() container annotation}
	which itself holds the other annotations.
	this class is tasked with unpacking those
	{@link Repeatable} annotations from their container.
	@see #flatten(Annotation[])
	*/
	public static record RepeatableCache(
		/**
		the class which holds all of the repeated annotations.
		for example, given the following annotations: {@code
			@Repeatable(B.class)
			public @interface A {

				public abstract String value();
			}

			public @interface B {

				public abstract A[] value();
			}
		},
		A is the wrapped class, and B is the container class.
		when multiple instances of A are applied to a type,
		reflectively, it behaves as if a single instance
		of B were applied instead, and B.value() returns all
		the instances of A which were applied in the source code.
		in other words, {@code
			@A("a")
			@A("b")
			@A("c")
			MyType
		}
		becomes {@code
			@B({ @A("a"), @A("b"), @A("c") })
			MyType
		}

		note however that if A is NOT annotated with {@link Repeatable},
		then A becomes the container class, and {@link RepeatableCache#accessor} is null.
		*/
		@NotNull Class<? extends Annotation> containerClass,
		@Nullable Class<? extends Annotation> wrappedClass,
		/**
		in the above example, the accessor would be B::value,
		or null if A is not {@link Repeatable}.
		*/
		@Nullable MethodHandle accessor,
		/**
		if {@link RepeatableCache#containerClass} is annotated with {@link Mirror},
		then this component contains the classes of all the annotations that
		containerClass is mirroring. for example: {@code
			public @interface A {}

			@A
			@Mirror(A.class)
			public @interface B {}
		}
		if containerClass points to B, then this record
		component contains the singleton set of A.class.
		*/
		@Nullable Set<@NotNull Class<? extends Annotation>> mirrors
	) {

		public static final @NotNull MethodType ACCESSOR_METHOD_TYPE = MethodType.methodType(Annotation[].class, Annotation.class);

		/**
		{@link Repeatable} annotations know what container
		to use for their storage, but the container annotation
		does not have a convenient marker for what it stores.
		in fact, it doesn't even have a marker specifying that
		it's a container in the first place (that I'm aware of).
		the logic I use to determine A: whether or not an
		annotation is a container, and B: what annotations
		it contains is in {@link #computeCache(Class)},
		but that method is not particularly efficient.
		so, we cache its results here.
		*/
		public static final @NotNull ClassValue<@NotNull RepeatableCache> CACHE = new ClassValue<>() {

			@Override
			public @NotNull RepeatableCache computeValue(@NotNull Class<?> type) {
				return computeCache(type.asSubclass(Annotation.class));
			}
		};

		/**
		if any annotations in the provided array are
		{@link Repeatable#value() container annotations},
		their contents are unpacked and inserted into the
		array immediately after the container they came from.
		the array is expanded in the process.
		technically it's converted to a {@link List}
		in this case, but whatever. technical details.
		anyway, once all annotations are unpacked,
		they are returned in a new array.
		if the provided array did not have
		any container annotations in it,
		then the provided array is returned as-is.
		*/
		public static @NotNull Annotation @NotNull [] flatten(@NotNull Annotation @NotNull [] annotations) {
			int length = annotations.length;
			if (length == 0) return ArrayFactories.ANNOTATION.empty();
			List<Annotation> list = null;
			for (int index = 0; index < length; index++) {
				if (list != null) list.add(annotations[index]);
				RepeatableCache cache = getCache(annotations[index].annotationType());
				if (cache.isContainer()) {
					Annotation[] wrapped = cache.unwrapContainer(annotations[index]);
					int wrappedLength = wrapped.length;
					if (wrappedLength > 0) {
						if (list == null) {
							list = new ArrayList<>(length + wrapped.length);
							for (int addIndex = 0; addIndex <= index; addIndex++) {
								list.add(annotations[addIndex]);
							}
						}
						for (int wrappedIndex = 0; wrappedIndex < wrappedLength; wrappedIndex++) {
							list.add(wrapped[wrappedIndex]);
						}
					}
				}
				if (cache.mirrors() != null) {
					Annotation[] wrapped = cache.unwrapMirrors();
					int wrappedLength = wrapped.length;
					if (wrappedLength > 0) {
						if (list == null) {
							list = new ArrayList<>(length + wrapped.length);
							for (int addIndex = 0; addIndex <= index; addIndex++) {
								list.add(annotations[addIndex]);
							}
						}
						for (int wrappedIndex = 0; wrappedIndex < wrappedLength; wrappedIndex++) {
							list.add(wrapped[wrappedIndex]);
						}
					}
				}
			}
			return list == null ? annotations : ArrayFactories.ANNOTATION.collectionToArray(list);
		}

		/**
		if we have previously determined whether or not
		(annotationClass) is an annotation container class,
		returns that cached status.
		otherwise, computes that status and adds it to our cache.
		@see #CACHE
		*/
		public static @NotNull RepeatableCache getCache(@NotNull Class<? extends Annotation> annotationClass) {
			return CACHE.get(annotationClass);
		}

		/**
		computes whether or not the provided (possibleContainerClass)
		is actually a container or not.
		if it is, the returned RepeatableCache
		will have a non-null {@link #wrappedClass} and {@link #accessor}.
		otherwise, it will have a null wrappedClass and accessor.
		the conditions required for (possibleContainerClass)
		to be a container are:
		1: it must have a method named value() which returns an array of annotations.
		2: the annotations returned by value() must be meta-annotated with {@link Repeatable}.
		3: the {@link Repeatable#value() repeatable container type} must be (possibleContainerClass).
		*/
		public static @NotNull RepeatableCache computeCache(@NotNull Class<? extends Annotation> possibleContainerClass) {
			Mirror mirrorAnnotation = possibleContainerClass.getDeclaredAnnotation(Mirror.class);
			Set<Class<? extends Annotation>> mirrorValues = mirrorAnnotation != null ? Set.of(mirrorAnnotation.value()) : null;
			for (Method method : possibleContainerClass.getDeclaredMethods()) {
				if (method.getName().equals("value")) {
					Class<?> possibleWrappedClass = method.getReturnType().getComponentType();
					if (possibleWrappedClass != null && possibleWrappedClass.isAnnotation()) {
						Repeatable repeatable = possibleWrappedClass.getDeclaredAnnotation(Repeatable.class);
						if (repeatable != null && repeatable.value() == possibleContainerClass) {
							try {
								return new RepeatableCache(
									possibleContainerClass,
									possibleWrappedClass.asSubclass(Annotation.class),
									MethodHandles.publicLookup().unreflect(method).asType(ACCESSOR_METHOD_TYPE),
									mirrorValues
								);
							}
							catch (IllegalAccessException ignored) {}
						}
					}
				}
			}
			return new RepeatableCache(possibleContainerClass, null, null, mirrorValues);
		}

		public boolean isContainer() {
			return this.accessor != null;
		}

		/**
		invokes our {@link #accessor} (if present), and returns its result.
		if our {@link #accessor} is not present, throws {@link IllegalStateException}.
		*/
		public @NotNull Annotation @NotNull [] unwrapContainer(@NotNull Annotation container) {
			if (this.accessor == null) {
				throw new IllegalStateException("Not a container: " + this.containerClass);
			}
			try {
				return (Annotation[])(this.accessor.invokeExact(container));
			}
			catch (Throwable throwable) {
				throw AutoCodecUtil.rethrow(throwable);
			}
		}

		public boolean isMirror() {
			return this.mirrors != null;
		}

		/**
		returns all the annotations that this annotation mirrors.
		if this annotation does not mirror any other annotations,
		returns an empty array.
		*/
		public @NotNull Annotation @NotNull [] unwrapMirrors() {
			if (!this.isMirror()) {
				throw new IllegalStateException("Not mirroring: " + this.containerClass);
			}
			if (this.mirrors.isEmpty()) {
				return ArrayFactories.ANNOTATION.empty();
			}
			List<Annotation> list = new ArrayList<>(8);
			this.recursiveUnwrapMirrors(list);
			return ArrayFactories.ANNOTATION.collectionToArray(list);
		}

		/**
		internal logic for unwrapping mirrors.
		this method handles unwrapping recursively,
		in order to support the transitive property of mirroring.
		*/
		public void recursiveUnwrapMirrors(@NotNull List<@NotNull Annotation> list) {
			for (Annotation annotation : from(this.containerClass).getAll()) {
				if (this.mirrors.contains(annotation.annotationType())) {
					list.add(annotation);
					RepeatableCache cache = getCache(annotation.annotationType());
					if (cache.isMirror()) cache.recursiveUnwrapMirrors(list);
				}
			}
		}
	}
}