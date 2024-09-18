package builderb0y.autocodec.coders;

import java.lang.reflect.Array;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Unit;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.DefaultEmpty;
import builderb0y.autocodec.coders.AutoCoder.NamedCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.constructors.AutoConstructor;
import builderb0y.autocodec.constructors.AutoConstructor.NamedConstructor;
import builderb0y.autocodec.constructors.ConstructContext;
import builderb0y.autocodec.constructors.ConstructException;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.EncodeContext;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.reflection.reification.TypeClassification;
import builderb0y.autocodec.util.DFUVersions;
import builderb0y.autocodec.util.ObjectOps;

public class DefaultEmptyCoder<T_Decoded> extends NamedCoder<T_Decoded> {

	public final @NotNull AutoCoder<T_Decoded> nonEmpty;
	public final @NotNull AutoConstructor<T_Decoded> constructor;
	public final boolean alwaysEncode;

	public DefaultEmptyCoder(
		@NotNull ReifiedType<T_Decoded> handledType,
		@NotNull AutoCoder<T_Decoded> nonEmpty,
		@NotNull AutoConstructor<T_Decoded> constructor,
		boolean alwaysEncode
	) {
		super(handledType);
		this.nonEmpty = nonEmpty;
		this.constructor = constructor;
		this.alwaysEncode = alwaysEncode;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @Nullable T_Decoded decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
		if (context.isEmpty()) try {
			return context.constructWith(this.constructor);
		}
		catch (ConstructException exception) {
			throw new DecodeException(exception);
		}
		else {
			return context.decodeWith(this.nonEmpty);
		}
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull T_Encoded encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		T_Encoded encoded = context.encodeWith(this.nonEmpty);
		if (!this.alwaysEncode) {
			Stream<?> stream = DFUVersions.getResult(context.ops.getStream(encoded));
			if (stream != null && stream.findAny().isEmpty()) {
				encoded = context.empty();
			}
			else {
				stream = DFUVersions.getResult(context.ops.getMapValues(encoded));
				if (stream != null && stream.findAny().isEmpty()) {
					encoded = context.empty();
				}
			}
		}
		return encoded;
	}

	@Override
	public @Nullable Stream<@NotNull String> getKeys() {
		return this.nonEmpty.getKeys();
	}

	public static class EmptyArrayConstructor<T_Array> extends NamedConstructor<T_Array> {

		public final @NotNull Class<?> componentClass;

		public EmptyArrayConstructor(@NotNull ReifiedType<T_Array> arrayType) {
			super(arrayType);
			ReifiedType<?> componentType = arrayType.getArrayComponentType();
			if (componentType == null) throw new FactoryException("Not an array: " + arrayType);
			Class<?> componentClass = componentType.getRawClass();
			if (componentClass == null) throw new FactoryException("Unable to access raw class of " + componentType);
			this.componentClass = componentClass;
		}

		@Override
		@OverrideOnly
		@SuppressWarnings("unchecked")
		public <T_Encoded> @NotNull T_Array construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
			return (T_Array)(Array.newInstance(this.componentClass, 0));
		}
	}

	public static class SharedConstructor<T_Decoded> extends NamedConstructor<T_Decoded> {

		public final @NotNull T_Decoded instance;

		public SharedConstructor(@NotNull ReifiedType<T_Decoded> type, @NotNull T_Decoded instance) {
			super(type);
			this.instance = instance;
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @NotNull T_Decoded construct(@NotNull ConstructContext<T_Encoded> context) throws ConstructException {
			return this.instance;
		}
	}

	public static class Factory extends NamedCoderFactory {

		public static final Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		public <T_HandledType> @Nullable AutoCoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			DefaultEmpty annotation = context.type.getAnnotations().getFirst(DefaultEmpty.class);
			if (annotation != null) {
				AutoCoder<T_HandledType> fallback = context.forceCreateFallbackCoder(this);
				AutoConstructor<T_HandledType> constructor;
				if (context.type.getClassification() == TypeClassification.ARRAY) {
					constructor = new EmptyArrayConstructor<>(context.type);
				}
				else {
					constructor = context.forceCreateConstructor();
				}
				if (annotation.shared()) try {
					@SuppressWarnings("TestOnlyProblems") //one of the rare cases where construction is useful without imprinting.
					T_HandledType object = context.autoCodec.construct(constructor, Unit.INSTANCE, ObjectOps.INSTANCE);
					constructor = new SharedConstructor<>(context.type, object);
				}
				catch (ConstructException exception) {
					throw new FactoryException(exception);
				}
				return new DefaultEmptyCoder<>(context.type, fallback, constructor, annotation.alwaysEncode());
			}
			return null;
		}
	}
}