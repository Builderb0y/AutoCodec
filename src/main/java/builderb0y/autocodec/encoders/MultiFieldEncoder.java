package builderb0y.autocodec.encoders;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import org.jetbrains.annotations.ApiStatus.OverrideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.annotations.EncodeInline;
import builderb0y.autocodec.coders.AutoCoder;
import builderb0y.autocodec.common.FactoryContext;
import builderb0y.autocodec.common.FactoryException;
import builderb0y.autocodec.data.Data;
import builderb0y.autocodec.data.EmptyData;
import builderb0y.autocodec.data.MapData;
import builderb0y.autocodec.data.StringData;
import builderb0y.autocodec.decoders.AutoDecoder.NamedDecoder;
import builderb0y.autocodec.decoders.DecodeContext;
import builderb0y.autocodec.decoders.DecodeException;
import builderb0y.autocodec.encoders.AutoEncoder.NamedEncoder;
import builderb0y.autocodec.reflection.FieldPredicate;
import builderb0y.autocodec.reflection.manipulators.InstanceReader;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.reification.ReifiedType;
import builderb0y.autocodec.util.ObjectArrayFactory;

public class MultiFieldEncoder<T_Decoded> extends NamedEncoder<T_Decoded> {

	public final @NotNull FieldStrategy<T_Decoded, ?> @NotNull [] fields;

	public MultiFieldEncoder(@NotNull ReifiedType<T_Decoded> type, @NotNull FieldStrategy<T_Decoded, ?> @NotNull [] fields) {
		super(type);
		this.fields = fields;
	}

	@Override
	@OverrideOnly
	public <T_Encoded> @NotNull Data encode(@NotNull EncodeContext<T_Encoded, T_Decoded> context) throws EncodeException {
		if (context.object == null) return EmptyData.INSTANCE;
		Object2ObjectMap<Data, Data> map = new Object2ObjectLinkedOpenHashMap<>(this.fields.length);
		for (FieldStrategy<T_Decoded, ?> field : this.fields) {
			field.encodeOnto(context, map);
		}
		return new MapData(map);
	}

	@Override
	public @Nullable Stream<@NotNull String> getKeys() {
		//requirements:
		//	1: if any of our fields lack keys, then we lack keys too.
		//	2: since the only way to check if a field has keys is
		//	to get the keys, if any fields have no keys, all the
		//	rest of the keys (or Stream's of keys) need to be closed.
		//algorithm:
		//	1. dump the streams of keys into an array.
		//	2. if we ever encounter a null Stream,
		//	then we close all previous Stream's in the array,
		//	and return null immediately.
		//	3. if we do not encounter a null Stream,
		//	then concatenate the Stream's in the array.
		int componentCount = this.fields.length;
		@SuppressWarnings("unchecked") //generic array.
		Stream<String>[] streams = new Stream[componentCount];
		for (int index = 0; index < componentCount; index++) {
			if ((streams[index] = this.fields[index].getKeys()) == null) {
				for (int index2 = 0; index2 < index; index2++) {
					streams[index2].close();
				}
				return null;
			}
		}
		return Arrays.stream(streams).flatMap(Function.identity());
	}

	@Override
	public String toString() {
		return super.toString() + " (" + this.fields.length + " fields)";
	}

	public static class FieldStrategy<T_Record, T_Member> extends NamedDecoder<T_Member> {

		public static final @NotNull ObjectArrayFactory<FieldStrategy<?, ?>> ARRAY_FACTORY = new ObjectArrayFactory<>(FieldStrategy.class).generic();

		public final FieldLikeMemberView<T_Record, T_Member> field;
		public final InstanceReader<T_Record, T_Member> getter;
		public final AutoCoder<T_Member> coder;
		public final boolean inline;

		public FieldStrategy(FieldLikeMemberView<T_Record, T_Member> field, InstanceReader<T_Record, T_Member> getter, AutoCoder<T_Member> coder, boolean inline) {
			super(field.getType());
			this.field  = field;
			this.getter = getter;
			this.coder  = coder;
			this.inline = inline;
		}

		@Override
		@OverrideOnly
		public <T_Encoded> @Nullable T_Member decode(@NotNull DecodeContext<T_Encoded> context) throws DecodeException {
			if (this.inline) {
				return context.decodeWith(this.coder);
			}
			else {
				MapData map = context.forceAsMap();
				for (String alias : this.field.getAliases()) {
					Data member = map.value.get(new StringData(alias));
					if (member != null) return context.input(alias, member).decodeWith(this.coder);
				}
				return context.input(EmptyData.INSTANCE).decodeWith(this.coder);
			}
		}

		public <T_Encoded> void encodeOnto(
			@NotNull EncodeContext<T_Encoded, T_Record> context,
			@NotNull Map<@NotNull Data, @NotNull Data> map
		)
		throws EncodeException {
			T_Member member = this.getter.get(context.object);
			if (member == null) return;
			EncodeContext<T_Encoded, T_Member> memberContext = context.object(member);
			Data encodedMember = memberContext.encodeWith(this.coder);
			if (!encodedMember.isEmpty()) {
				if (this.inline) {
					MapData newMap = encodedMember.tryAsMap();
					if (newMap == null) throw new EncodeException(() -> member + " was annotated as @EncodeInline, but encodes into a value which is not a map: " + encodedMember);
					for (Map.Entry<Data, Data> entry : newMap.value.entrySet()) {
						if (map.putIfAbsent(entry.getKey(), entry.getValue()) != null) {
							throw new EncodeException(() -> entry.getKey() + " is a field used for more than one object!");
						}
					}
				}
				else {
					if (map.putIfAbsent(new StringData(this.field.getSerializedName()), encodedMember) != null) {
						throw new EncodeException(() -> this.field.getSerializedName() + " is a field used for more than one object!");
					}
				}
			}
		}

		@Override
		public @Nullable Stream<@NotNull String> getKeys() {
			return this.inline ? this.coder.getKeys() : Arrays.stream(this.field.getAliases());
		}
	}

	public static class Factory extends NamedEncoderFactory {

		public static final @NotNull Factory INSTANCE = new Factory();

		@Override
		@OverrideOnly
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public <T_HandledType> @Nullable AutoEncoder<?> tryCreate(@NotNull FactoryContext<T_HandledType> context) throws FactoryException {
			FieldLikeMemberView<?, ?>[] fields = (
				Arrays.stream(context.reflect().getFields(true))
				.filter(new FieldPredicate().notStatic())
				.toArray(FieldLikeMemberView.ARRAY_FACTORY.generic())
			);
			int length = fields.length;
			context.logger().logMessageLazy(() -> "Found " + length + " field(s) that are applicable for encoding.");
			if (length == 0) return null;
			FieldStrategy<?, ?>[] strategies = FieldStrategy.ARRAY_FACTORY.applyGeneric(length);
			for (int index = 0; index < length; index++) try {
				strategies[index] = new FieldStrategy(
					fields[index],
					fields[index].createInstanceReader(context),
					context.type(fields[index].getType()).forceCreateCoder(),
					fields[index].getAnnotations().has(EncodeInline.class)
				);
			}
			catch (IllegalAccessException exception) {
				throw new FactoryException(exception);
			}
			return new MultiFieldEncoder(context.type, strategies);
		}
	}
}