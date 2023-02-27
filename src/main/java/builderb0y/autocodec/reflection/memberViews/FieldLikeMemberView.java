package builderb0y.autocodec.reflection.memberViews;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;

import it.unimi.dsi.fastutil.Hash;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.UseName;
import builderb0y.autocodec.common.ReflectContextProvider;
import builderb0y.autocodec.reflection.PseudoField;
import builderb0y.autocodec.reflection.manipulators.*;
import builderb0y.autocodec.reflection.manipulators.impl.*;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.HashStrategies.NamedHashStrategy;
import builderb0y.autocodec.util.ObjectArrayFactory;
import builderb0y.autocodec.util.TypeFormatter;

/** common properties of {@link Field}, {@link RecordComponent}, and {@link PseudoField}. */
public abstract class FieldLikeMemberView<T_Owner, T_Member> extends MemberView<T_Owner> {

	public static final @NotNull ObjectArrayFactory<FieldLikeMemberView<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FieldLikeMemberView.class).generic();

	public static final Hash.@NotNull Strategy<FieldLikeMemberView<?, ?>>
		RAW_TYPE_STRATEGY = new HashStrategy("FieldLikeMemberView.RAW_TYPE_STRATEGY", ReifiedType.RAW_TYPE_STRATEGY),
		GENERIC_TYPE_STRATEGY = new HashStrategy("FieldLikeMemberView.GENERIC_TYPE_STRATEGY", ReifiedType.GENERIC_TYPE_STRATEGY),
		ORDERED_ANNOTATED_TYPE_STRATEGY = new HashStrategy("FieldLikeMemberView.ORDERED_ANNOTATED_TYPE_STRATEGY", ReifiedType.ORDERED_ANNOTATIONS_STRATEGY),
		UNORDERED_ANNOTATED_TYPE_STRATEGY = new HashStrategy("FieldLikeMemberView.UNORDERED_ANNOTATED_TYPE_STRATEGY", ReifiedType.UNORDERED_ANNOTATIONS_STRATEGY);

	public @Nullable ReifiedType<T_Member> type;

	public FieldLikeMemberView(@NotNull ReifiedType<T_Owner> declaringType) {
		super(declaringType);
	}

	public @Nullable String serializedName; //lazily initialized.

	public @NotNull String getSerializedName() {
		String serializedName = this.serializedName;
		if (serializedName == null) {
			UseName annotation = this.getType().getAnnotations().getFirst(UseName.class);
			this.serializedName = serializedName = annotation != null ? annotation.value() : this.getName();
		}
		return serializedName;
	}

	public abstract @NotNull MethodHandle createInstanceReaderHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException;

	public abstract @NotNull MethodHandle createInstanceWriterHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException;

	public @NotNull InstanceReader<T_Owner, T_Member> createInstanceReader(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return InstanceReaderImpl.of(this, this.createInstanceReaderHandle(provider));
	}

	public @NotNull InstanceWriter<T_Owner, T_Member> createInstanceWriter(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return InstanceWriterImpl.of(this, this.createInstanceWriterHandle(provider));
	}

	public @NotNull InstanceReaderWriter<T_Owner, T_Member> createInstanceReaderWriter(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return InstanceReaderWriterImpl.of(this, this.createInstanceReaderHandle(provider), this.createInstanceWriterHandle(provider));
	}

	public @NotNull MethodHandle createStaticReaderHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		throw new IllegalAccessException("Cannot create static reader handle for non-static member: " + this);
	}

	public @NotNull MethodHandle createStaticWriterHandle(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		throw new IllegalAccessException("Cannot create static writer handle for non-static member: " + this);
	}

	public @NotNull StaticReader<T_Member> createStaticReader(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return StaticReaderImpl.of(this, this.createStaticReaderHandle(provider));
	}

	public @NotNull StaticWriter<T_Member> createStaticWriter(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return StaticWriterImpl.of(this, this.createStaticWriterHandle(provider));
	}

	public @NotNull StaticReaderWriter<T_Member> createStaticReaderWriter(@NotNull ReflectContextProvider provider) throws IllegalAccessException {
		return StaticReaderWriterImpl.of(this, this.createStaticReaderHandle(provider), this.createStaticWriterHandle(provider));
	}

	public boolean isFinal() {
		return Modifier.isFinal(this.getModifiers());
	}

	/**
	this method exists solely to avoid duplicated code when reifying the member type.
	{@link #getType()} should always be used instead of this method.
	*/
	@Deprecated
	@SuppressWarnings("DeprecatedIsStillUsed")
	protected abstract @NotNull AnnotatedType _getAnnotatedType();

	@SuppressWarnings({ "unchecked", "deprecation" })
	public @NotNull ReifiedType<T_Member> getType() {
		ReifiedType<T_Member> type = this.type;
		if (type == null) {
			this.type = type = (ReifiedType<T_Member>)(this.getDeclaringType().resolveDeclaration(this._getAnnotatedType()));
		}
		return type;
	}

	@Override
	public void appendTo(TypeFormatter formatter) {
		formatter.append(Modifier.toString(this.getModifiers())).append(' ').append(this.getType()).append(' ').append(this.getDeclaringType()).append('.').append(this.getName());
	}

	@Override
	public int hashCode() {
		return ORDERED_ANNOTATED_TYPE_STRATEGY.hashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return (
			obj instanceof FieldLikeMemberView<?, ?> that &&
			ORDERED_ANNOTATED_TYPE_STRATEGY.equals(this, that)
		);
	}

	public static class HashStrategy extends NamedHashStrategy<FieldLikeMemberView<?, ?>> {

		public final Hash.@NotNull Strategy<ReifiedType<?>> typeStrategy, declaringTypeStrategy;

		public HashStrategy(
			@NotNull String toString,
			Hash.@NotNull Strategy<ReifiedType<?>> typeStrategy,
			Hash.@NotNull Strategy<ReifiedType<?>> declaringTypeStrategy
		) {
			super(toString);
			this.typeStrategy = typeStrategy;
			this.declaringTypeStrategy = declaringTypeStrategy;
		}

		public HashStrategy(@NotNull String toString, Hash.@NotNull Strategy<ReifiedType<?>> typeStrategy) {
			this(toString, typeStrategy, typeStrategy);
		}

		@Override
		public int hashCode(FieldLikeMemberView<?, ?> o) {
			if (o == null) return 0;
			return (
				o.getName().hashCode() +
				this.typeStrategy.hashCode(o.getType()) +
				this.declaringTypeStrategy.hashCode(o.getDeclaringType())
			);
		}

		@Override
		public boolean equals(FieldLikeMemberView<?, ?> a, FieldLikeMemberView<?, ?> b) {
			if (a == b) return true;
			if (a == null || b == null) return false;
			return (
				a.getName().equals(b.getName()) &&
				this.typeStrategy.equals(a.getType(), b.getType()) &&
				this.declaringTypeStrategy.equals(a.getDeclaringType(), b.getDeclaringType())
			);
		}
	}
}