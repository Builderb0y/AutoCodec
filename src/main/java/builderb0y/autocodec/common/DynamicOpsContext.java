package builderb0y.autocodec.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DataResult.PartialResult;
import com.mojang.serialization.DynamicOps;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.encoders.EncodeException;
import builderb0y.autocodec.util.ObjectArrayFactory;

public abstract class DynamicOpsContext<T_Encoded> extends TaskContext {

	public static final @NotNull ObjectArrayFactory<DynamicOpsContext<?>> ARRAY_FACTORY = new ObjectArrayFactory<>(DynamicOpsContext.class).generic();

	public final @NotNull DynamicOps<T_Encoded> ops;

	public DynamicOpsContext(@NotNull AutoCodec codec, @NotNull DynamicOps<T_Encoded> ops) {
		super(codec);
		this.ops = ops;
	}

	public boolean isCompressed() {
		return this.ops.compressMaps();
	}

	//////////////// creating empty ////////////////

	public @NotNull T_Encoded empty() {
		return this.ops.empty();
	}

	public @NotNull T_Encoded emptyList() {
		return this.ops.emptyList();
	}

	public @NotNull T_Encoded emptyMap() {
		return this.ops.emptyMap();
	}

	//////////////// creating primitives ////////////////

	public @NotNull T_Encoded createNumber(@NotNull Number value) {
		return this.ops.createNumeric(value);
	}

	public @NotNull T_Encoded createByte(byte value) {
		return this.ops.createByte(value);
	}

	public @NotNull T_Encoded createShort(short value) {
		return this.ops.createShort(value);
	}

	public @NotNull T_Encoded createInt(int value) {
		return this.ops.createInt(value);
	}

	public @NotNull T_Encoded createLong(long value) {
		return this.ops.createLong(value);
	}

	public @NotNull T_Encoded createFloat(float value) {
		return this.ops.createFloat(value);
	}

	public @NotNull T_Encoded createDouble(double value) {
		return this.ops.createDouble(value);
	}

	public @NotNull T_Encoded createBoolean(boolean value) {
		return this.ops.createBoolean(value);
	}

	public @NotNull T_Encoded createString(String value) {
		return this.ops.createString(value);
	}

	//////////////// creating maps ////////////////

	/**
	ensures that the provided Stream does not contain any pairs where
	the {@link Pair#second} element is either null or {@link #empty()}.
	some serialization systems (mainly Minecraft's NBT data) do not handle
	serialization of empty values properly, which can lead to data corruption.
	in general, one should not attempt to create a map with null values in it.
	instead, entries with null values should not be present in the map at all.
	*/
	public @NotNull Stream<@NotNull Pair<@NotNull T_Encoded, @NotNull T_Encoded>> filterNulls(@NotNull Stream<@NotNull Pair<@NotNull T_Encoded, @Nullable T_Encoded>> stream) {
		return stream.filter(pair -> {
			if (pair.getSecond() == null || pair.getSecond() == this.empty()) {
				this.logger().logErrorLazy(() -> "Attempted to create map where " + pair.getFirst() + " is mapped to " + pair.getSecond() + '!');
				return false;
			}
			return true;
		});
	}

	/**
	creates a {@link T_Encoded} which represents a map containing entries
	whose keys correspond to the {@link Pair#first} element in the provided
	Stream's Pair's, and whose values correspond to the {@link Pair#second}
	element in the provided Stream's Pair's.
	*/
	public @NotNull T_Encoded createGenericMap(@NotNull Stream<@NotNull Pair<@NotNull T_Encoded, @NotNull T_Encoded>> stream) {
		return this.ops.createMap(this.filterNulls(stream));
	}

	/**
	creates a {@link T_Encoded} which represents a map containing
	entries whose keys correspond to other instances of {@link T_Encoded}
	which represent the {@link Pair#first} element in the
	provided Stream's Pair's, and whose values correspond to the
	{@link Pair#second} element in the provided Stream's Pair's.
	*/
	public @NotNull T_Encoded createStringMap(@NotNull Stream<@NotNull Pair<@NotNull String, @NotNull T_Encoded>> stream) {
		DynamicOps<T_Encoded> ops = this.ops;
		return ops.createMap(this.filterNulls(stream.map((Pair<String, T_Encoded> pair) -> Pair.of(ops.createString(pair.getFirst()), pair.getSecond()))));
	}

	/** creates a {@link T_Encoded} which represents the provided Map. */
	public @NotNull T_Encoded createGenericMap(@NotNull Map<@NotNull T_Encoded, @NotNull T_Encoded> map) {
		return this.ops.createMap(this.filterNulls(map.entrySet().stream().map((Map.Entry<T_Encoded, T_Encoded> entry) -> Pair.of(entry.getKey(), entry.getValue()))));
	}

	/**
	creates a {@link T_Encoded} which represents a map whose
	keys correspond to other instances of {@link T_Encoded}
	which represent the keys in the provided Map,
	and whose values correspond to the values in the provided MAp.
	*/
	public @NotNull T_Encoded createStringMap(@NotNull Map<@NotNull String, @NotNull T_Encoded> map) {
		DynamicOps<T_Encoded> ops = this.ops;
		return ops.createMap(this.filterNulls(map.entrySet().stream().map((Map.Entry<String, T_Encoded> entry) -> Pair.of(ops.createString(entry.getKey()), entry.getValue()))));
	}

	//////////////// creating lists and arrays ////////////////

	public @NotNull T_Encoded createList(@NotNull List<@NotNull T_Encoded> list) {
		return this.ops.createList(list.stream());
	}

	public @NotNull T_Encoded createList(@NotNull Stream<@NotNull T_Encoded> stream) {
		return this.ops.createList(stream);
	}

	public @NotNull T_Encoded createByteArray(byte @NotNull [] array) {
		return this.ops.createByteList(ByteBuffer.wrap(array));
	}

	public @NotNull T_Encoded createByteArray(@NotNull ByteBuffer buffer) {
		return this.ops.createByteList(buffer);
	}

	public @NotNull T_Encoded createIntArray(int @NotNull [] array) {
		return this.ops.createIntList(Arrays.stream(array));
	}

	public @NotNull T_Encoded createIntArray(@NotNull IntStream stream) {
		return this.ops.createIntList(stream);
	}

	public @NotNull T_Encoded createLongArray(long @NotNull [] array) {
		return this.ops.createLongList(Arrays.stream(array));
	}

	public @NotNull T_Encoded createLongArray(@NotNull LongStream stream) {
		return this.ops.createLongList(stream);
	}

	//////////////// merging ////////////////

	public static <T> T unwrap(@NotNull DataResult<T> result) {
		return result.get().map(
			Function.identity(),
			(PartialResult<T> partial) -> { throw new EncodeException(partial::message); }
		);
	}

	//////////////// merging lists ////////////////

	public @NotNull T_Encoded addToList(@NotNull T_Encoded list, @NotNull T_Encoded value) throws EncodeException {
		return unwrap(this.ops.mergeToList(list, value));
	}

	public @NotNull T_Encoded mergeList(@NotNull T_Encoded list, @NotNull List<T_Encoded> toAdd) throws EncodeException {
		return unwrap(this.ops.mergeToList(list, toAdd));
	}

	public @NotNull T_Encoded mergeLists(@NotNull T_Encoded list1, @NotNull T_Encoded list2) throws EncodeException {
		return this.mergeList(list1, unwrap(this.ops.getStream(list2)).collect(Collectors.toList()));
	}

	//////////////// merging maps ////////////////

	public @NotNull T_Encoded addToMap(@NotNull T_Encoded map, @NotNull T_Encoded key, @NotNull T_Encoded value) throws EncodeException {
		return unwrap(this.ops.mergeToMap(map, key, value));
	}

	public @NotNull T_Encoded addToStringMap(@NotNull T_Encoded map, @NotNull String key, @NotNull T_Encoded value) throws EncodeException {
		return this.addToMap(map, this.createString(key), value);
	}

	public @NotNull T_Encoded mergeMap(@NotNull T_Encoded map1, @NotNull Map<T_Encoded, T_Encoded> map2) {
		return unwrap(this.ops.mergeToMap(map1, map2));
	}

	public @NotNull T_Encoded mergeStringMap(@NotNull T_Encoded map1, @NotNull Map<String, T_Encoded> map2) {
		Map<T_Encoded, T_Encoded> converted = new LinkedHashMap<>(map2.size());
		for (Map.Entry<String, T_Encoded> entry : map2.entrySet()) {
			converted.put(this.createString(entry.getKey()), entry.getValue());
		}
		return this.mergeMap(map1, converted);
	}

	public @NotNull T_Encoded mergeMaps(@NotNull T_Encoded map1, @NotNull T_Encoded map2) {
		return this.mergeMap(map1, unwrap(this.ops.getMapValues(map2)).collect(Pair.toMap()));
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ": { ops: " + this.ops + " }";
	}
}