package builderb0y.autocodec.reflection;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.Hidden;
import builderb0y.autocodec.reflection.memberViews.*;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ArrayFactories;
import builderb0y.autocodec.util.TypeFormatter;

public class ReflectionManager {

	public final @NotNull Map<@NotNull Class<?>, @NotNull ClassCache<?>> classCache = new HashMap<>(128);
	public final @NotNull Map<@NotNull ReifiedType<?>, @NotNull TypeCache<?>> typeCache = new Object2ObjectOpenCustomHashMap<>(256, ReifiedType.ORDERED_ANNOTATIONS_STRATEGY);



	//////////////////////////////// queries ////////////////////////////////



	/**
	returns an array containing all the fields, record components, and pseudo-fields
	declared in (owner) that this ReflectionManager {@link #canView(Field)}.
	note: the returned array is not defensively copied. do not modify it!
	*/
	public <T_Owner> @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] getFields(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited
	) {
		return (
			inherited
			? this.getTypeCache(context.owner).inheritedFields()
			: this.getTypeCache(context.owner).fields()
		);
	}

	/**
	searches through all of our {@link #getFields(ReflectContext, boolean)},
	and collects the ones which match the given predicate into the requested format.
	*/
	public <T_Owner, T_Collect> T_Collect searchFields(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited,
		@NotNull Predicate<? super FieldLikeMemberView<T_Owner, ?>> predicate,
		@NotNull MemberCollector<FieldLikeMemberView<T_Owner, ?>, T_Collect> collector
	)
	throws ReflectException {
		FieldLikeMemberView<T_Owner, ?>[] fields = this.getFields(context, inherited);
		for (FieldLikeMemberView<T_Owner, ?> field : fields) {
			if (MemberPredicate.testAndDescribe(predicate, field, context.logger()) && collector.accept(field)) {
				context.logger().logMessage("Collector said we should stop searching now.");
				break;
			}
		}
		return collector.getResult();
	}

	/**
	returns an array containing all the methods and constructors declared
	in owner that this ReflectionManager {@link #canView(Method)}.
	note: the returned array is not defensively copied. do not modify it!
	*/
	public <T_Owner> @NotNull MethodLikeMemberView<T_Owner, ?> @NotNull [] getMethods(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited
	) {
		return (
			inherited
			? this.getTypeCache(context.owner).inheritedMethods()
			: this.getTypeCache(context.owner).methods()
		);
	}

	/**
	searches through all of our {@link #getMethods(ReflectContext, boolean)},
	and collects the ones which match the given predicate into the requested format.
	*/
	public <T_Owner, T_Collect> T_Collect searchMethods(
		@NotNull ReflectContext<T_Owner> context,
		boolean inherited,
		@NotNull Predicate<? super MethodLikeMemberView<T_Owner, ?>> predicate,
		@NotNull MemberCollector<MethodLikeMemberView<T_Owner, ?>, T_Collect> collector
	)
	throws ReflectException {
		MethodLikeMemberView<T_Owner, ?>[] methods = this.getMethods(context, inherited);
		for (MethodLikeMemberView<T_Owner, ?> method : methods) {
			if (MemberPredicate.testAndDescribe(predicate, method, context.logger()) && collector.accept(method)) {
				context.logger().logMessage("Collector said we should stop searching now.");
				break;
			}
		}
		return collector.getResult();
	}



	//////////////////////////////// visibility restrictions ////////////////////////////////



	/**
	returns true if this ReflectionManager should expose
	this class's fields and methods, and false otherwise.
	returning false effectively means that this ReflectionManager will
	pretend that the class is empty, and has no declared fields or methods.
	subclasses can override this method to add additional
	restrictions on which classes should be exposed.
	the default implementation requires that the class not be synthetic.
	synthetic classes are often found in lambda expressions,
	and the fields in them are typically captured local variables,
	and we don't want to mess with those.
	*/
	@OverrideOnly
	public boolean canView(@NotNull Class<?> clazz) {
		return !clazz.isSynthetic() && !clazz.isAnnotationPresent(Hidden.class);
	}

	/**
	returns true if this ReflectionManager should
	expose this field, and false otherwise.
	returning false effectively means that this ReflectionManager
	will pretend that the field does not exist, and the field
	will therefore never be returned by any of the methods above.
	subclasses can override this method to add additional
	restrictions on what fields should be exposed,
	or remove some of the ones in the default implementation.
	and speaking of which, the default implementation requires that the field be:
		* public
		* not transient
		* not synthetic
		* not an underlying field for a record component.
	*/
	@OverrideOnly
	public boolean canView(@NotNull Field field) {
		final int synthetic = 0x00001000; //Modifier.SYNTHETIC is not public.
		return (field.getModifiers() & (Modifier.PUBLIC | Modifier.TRANSIENT | synthetic)) == Modifier.PUBLIC && !field.isAnnotationPresent(Hidden.class);
		//underlying fields for record components are private, and will fail the above test.
	}

	/**
	returns true if this ReflectionManager should
	expose this record component, and false otherwise.
	returning false effectively means that this ReflectionManager
	will pretend that the record component does not exist, and the record
	component will therefore never be returned by any of the methods above.
	subclasses can override this method to add restrictions
	on what record components should be exposed.
	the default implementation allows access to all record
	components which are not annotated with {@link Hidden}.
	*/
	@OverrideOnly
	public boolean canView(@NotNull RecordComponent recordComponent) {
		return !recordComponent.isAnnotationPresent(Hidden.class);
	}

	/**
	returns true if this ReflectionManager should
	expose this pseudo-field, and false otherwise.
	returning false effectively means that this ReflectionManager
	will pretend that the pseudo-field does not exist, and the pseudo-field
	will therefore never be returned by any of the methods above.
	subclasses can override this method to add restrictions
	on what pseudo-fields should be exposed.
	the default implementation allows access to all pseudo-fields.
	*/
	@OverrideOnly
	public boolean canView(@NotNull PseudoField pseudoField) {
		return true;
	}

	/**
	returns true if this ReflectionManager should
	expose this method, and false otherwise.
	returning false effectively means that this ReflectionManager
	will pretend that the method does not exist, and the method
	will therefore never be returned by any of the methods above.
	subclasses can override this method to add additional
	restrictions on what methods should be exposed,
	or remove some of the ones in the default implementation.
	and speaking of which, the default implementation requires that the method be:
		* public
		* not synthetic
		* not annotated with {@link Hidden}.
	*/
	@OverrideOnly
	public boolean canView(@NotNull Method method) {
		final int synthetic = 0x00001000; //Modifier.SYNTHETIC is not public.
		return (method.getModifiers() & (Modifier.PUBLIC | synthetic)) == Modifier.PUBLIC && !method.isAnnotationPresent(Hidden.class);
	}

	/**
	returns true if this ReflectionManager should
	expose this constructor, and false otherwise.
	returning false effectively means that this ReflectionManager
	will pretend that the constructor does not exist, and the constructor
	will therefore never be returned by any of the methods above.
	subclasses can override this method to add restrictions
	on what constructors should be exposed,
	or remove some of the ones in the default implementation.
	and speaking of which, the default implementation requires that the method be:
		* public
		* not synthetic
		* not annotated with {@link Hidden}.
	*/
	@OverrideOnly
	public boolean canView(@NotNull Constructor<?> constructor) {
		final int synthetic = 0x00001000; //Modifier.SYNTHETIC is not public.
		return (constructor.getModifiers() & (Modifier.PUBLIC | synthetic)) == Modifier.PUBLIC && !constructor.isAnnotationPresent(Hidden.class);
	}



	//////////////////////////////// lookups ////////////////////////////////



	/**
	returns a {@link MethodHandles.Lookup} for the given class.
	the default implementation simply returns {@link MethodHandles#lookup()}.
	subclasses can override this method to grant more access to specific classes.
	*/
	public MethodHandles.@NotNull Lookup getLookup(@NotNull Class<?> in) {
		return MethodHandles.lookup();
	}

	public MethodHandles.@NotNull Lookup getLookup(@NotNull ReifiedType<?> in) {
		return this.getLookup(in.requireRawClass());
	}



	//////////////////////////////// cache ////////////////////////////////

	@SuppressWarnings("unchecked")
	public <T_Owner> @NotNull ClassCache<T_Owner> getClassCache(@NotNull Class<T_Owner> owner) {
		return (ClassCache<T_Owner>)(this.classCache.computeIfAbsent(owner, this::createClassCache));
	}

	public <T_Owner> @NotNull ClassCache<T_Owner> createClassCache(@NotNull Class<T_Owner> owner) {
		return this.new ClassCache<>(owner);
	}

	@SuppressWarnings("unchecked")
	public <T_Owner> @NotNull TypeCache<T_Owner> getTypeCache(@NotNull ReifiedType<T_Owner> owner) {
		return (TypeCache<T_Owner>)(this.typeCache.computeIfAbsent(owner, this::createTypeCache));
	}

	public <T_Owner> @NotNull TypeCache<T_Owner> createTypeCache(@NotNull ReifiedType<T_Owner> owner) {
		return this.new TypeCache<>(owner);
	}

	public class ClassCache<T_Owner> {

		public final @NotNull Class<T_Owner> owner;

		public @NotNull Field                @Nullable [] fields;
		public @NotNull RecordComponent      @Nullable [] recordComponents;
		public @NotNull PseudoField          @Nullable [] pseudoFields;
		public @NotNull Method               @Nullable [] methods;
		public @NotNull Constructor<T_Owner> @Nullable [] constructors;

		public ClassCache(@NotNull Class<T_Owner> owner) {
			this.owner = owner;
		}

		public <T> T @NotNull [] filter(T @Nullable [] array, T @NotNull [] emptyArray, @NotNull BiPredicate<@NotNull ReflectionManager, T> predicate) {
			if (array == null) return emptyArray;
			int length = array.length;
			int writeIndex = 0;
			for (int readIndex = 0; readIndex < length; readIndex++) {
				if (predicate.test(ReflectionManager.this, array[readIndex])) {
					array[writeIndex++] = array[readIndex];
				}
			}
			if (writeIndex == 0) return emptyArray;
			if (writeIndex == length) return array;
			return Arrays.copyOf(array, writeIndex);
		}

		public @NotNull Field @NotNull [] fields() {
			Field[] fields = this.fields;
			if (fields == null) {
				this.fields = fields = this.filter(
					this.owner.getDeclaredFields(),
					ArrayFactories.FIELD.empty(),
					ReflectionManager::canView
				);
			}
			return fields;
		}

		public @NotNull RecordComponent @NotNull [] recordComponents() {
			RecordComponent[] recordComponents = this.recordComponents;
			if (recordComponents == null) {
				this.recordComponents = recordComponents = this.filter(
					this.owner.getRecordComponents(),
					ArrayFactories.RECORD_COMPONENT.empty(),
					ReflectionManager::canView
				);
			}
			return recordComponents;
		}

		public @NotNull PseudoField @NotNull [] pseudoFields() {
			PseudoField[] pseudoFields = this.pseudoFields;
			if (pseudoFields == null) {
				this.pseudoFields = pseudoFields = this.filter(
					PseudoField.getPseudoFields(this.owner),
					ArrayFactories.PSEUDO_FIELD.empty(),
					ReflectionManager::canView
				);
			}
			return pseudoFields;
		}

		public @NotNull Method @NotNull [] methods() {
			Method[] methods = this.methods;
			if (methods == null) {
				this.methods = methods = this.filter(
					this.owner.getDeclaredMethods(),
					ArrayFactories.METHOD.empty(),
					ReflectionManager::canView
				);
			}
			return methods;
		}

		@SuppressWarnings("unchecked")
		public @NotNull Constructor<T_Owner> @NotNull [] constructors() {
			Constructor<T_Owner>[] constructors = this.constructors;
			if (constructors == null) {
				this.constructors = constructors = this.filter(
					(Constructor<T_Owner>[])(this.owner.getDeclaredConstructors()),
					ArrayFactories.CONSTRUCTOR.emptyGeneric(),
					ReflectionManager::canView
				);
			}
			return constructors;
		}

		@Override
		public String toString() {
			return TypeFormatter.appendSimpleClassUnchecked(new StringBuilder(64), this.getClass()).append(" for ").append(this.owner).toString();
		}
	}

	public class TypeCache<T_Owner> {

		public final @NotNull ReifiedType<T_Owner> owner;

		public @NotNull FieldLikeMemberView<T_Owner, ?> @Nullable [] fields, inheritedFields;
		public @NotNull MethodLikeMemberView<T_Owner, ?> @Nullable [] methods, inheritedMethods;

		public TypeCache(@NotNull ReifiedType<T_Owner> owner) {
			this.owner = owner;
		}

		public @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] fields() {
			FieldLikeMemberView<T_Owner, ?>[] fields = this.fields;
			if (fields == null) {
				ReifiedType<T_Owner> owner = this.owner;
				Class<? super T_Owner> rawClass = owner.getRawClass();
				if (rawClass == null || !ReflectionManager.this.canView(rawClass)) {
					fields = FieldLikeMemberView.ARRAY_FACTORY.emptyGeneric();
				}
				else {
					ClassCache<? super T_Owner> classCache = ReflectionManager.this.getClassCache(rawClass);
					fields = (
						Stream.<Stream<FieldLikeMemberView<T_Owner, ?>>>of(
							Arrays.stream(classCache.fields          ()).map((Field           field    ) -> new FieldView          <>(owner, field    )),
							Arrays.stream(classCache.recordComponents()).map((RecordComponent component) -> new RecordComponentView<>(owner, component)),
							Arrays.stream(classCache.pseudoFields    ()).map((PseudoField     pseudo   ) -> new PseudoFieldView    <>(owner, pseudo   ))
						)
						.flatMap(Function.identity())
						.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
					);
				}
				this.fields = fields;
			}
			return fields;
		}

		public @NotNull FieldLikeMemberView<T_Owner, ?> @NotNull [] inheritedFields() {
			FieldLikeMemberView<T_Owner, ?>[] inheritedFields = this.inheritedFields;
			if (inheritedFields == null) {
				inheritedFields = this.inheritedFields = (
					this
					.owner
					.getInheritanceHierarchy()
					.stream()
					.flatMap((ReifiedType<? super T_Owner> type) -> Arrays.stream(ReflectionManager.this.getTypeCache(type).fields()))
					.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
				);
			}
			return inheritedFields;
		}

		public @NotNull MethodLikeMemberView<T_Owner, ?> @NotNull [] methods() {
			MethodLikeMemberView<T_Owner, ?>[] methods = this.methods;
			if (methods == null) {
				ReifiedType<T_Owner> owner = this.owner;
				@SuppressWarnings("unchecked")
				Class<T_Owner> rawClass = (Class<T_Owner>)(owner.getRawClass());
				if (rawClass == null || !ReflectionManager.this.canView(rawClass)) {
					methods = MethodLikeMemberView.ARRAY_FACTORY.emptyGeneric();
				}
				else {
					ClassCache<T_Owner> classCache = ReflectionManager.this.getClassCache(rawClass);
					methods = (
						Stream.<MethodLikeMemberView<T_Owner, ?>>concat(
							Arrays.stream(classCache.methods     ()).map((Method               method     ) -> new MethodView     <>(owner, method     )),
							Arrays.stream(classCache.constructors()).map((Constructor<T_Owner> constructor) -> new ConstructorView<>(owner, constructor))
						)
						.toArray(MethodLikeMemberView.ARRAY_FACTORY.generic())
					);
				}
				this.methods = methods;
			}
			return methods;
		}

		public @NotNull MethodLikeMemberView<T_Owner, ?> @NotNull [] inheritedMethods() {
			MethodLikeMemberView<T_Owner, ?>[] inheritedMethods = this.inheritedMethods;
			if (inheritedMethods == null) {
				inheritedMethods = this.inheritedMethods = (
					this
					.owner
					.getInheritanceHierarchy()
					.stream()
					.flatMap((ReifiedType<? super T_Owner> type) -> Arrays.stream(ReflectionManager.this.getTypeCache(type).methods()))
					.toArray(MethodLikeMemberView.ARRAY_FACTORY.generic())
				);
			}
			return inheritedMethods;
		}

		@Override
		public String toString() {
			return TypeFormatter.appendSimpleClassUnchecked(new StringBuilder(64), this.getClass()).append(" for ").append(this.owner).toString();
		}
	}
}