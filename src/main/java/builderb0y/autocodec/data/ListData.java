package builderb0y.autocodec.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.jetbrains.annotations.NotNull;

public class ListData<T_Encoded> extends Data<T_Encoded> {

	public @NotNull List<@NotNull Data<T_Encoded>> value;

	public ListData(@NotNull DynamicOps<T_Encoded> ops) {
		super(ops);
		this.value = new ObjectArrayList<>();
	}

	public ListData(@NotNull DynamicOps<T_Encoded> ops, int capacity) {
		super(ops);
		this.value = new ObjectArrayList<>(capacity);
	}

	public ListData(@NotNull DynamicOps<T_Encoded> ops, @NotNull List<@NotNull Data<T_Encoded>> value) {
		super(ops);
		this.value = value;
	}

	public @NotNull Stream<@NotNull Data<@NotNull T_Encoded>> streamNonEmpty() {
		return this.value.stream().filter((Data<T_Encoded> data) -> data != null && !data.isEmpty());
	}

	@Override
	public @NotNull T_Encoded encode() {
		return this.ops.createList(this.streamNonEmpty().map(Data<T_Encoded>::encode));
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createList(this.streamNonEmpty().map((Data<T_Encoded> element) -> element.convert(ops)));
	}

	public @NotNull Data<T_Encoded> get(int index) {
		return this.value.get(index);
	}

	public @NotNull Data<T_Encoded> set(int index, boolean value) {
		return this.set(index, new BooleanData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, byte value) {
		return this.set(index, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, short value) {
		return this.set(index, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, int value) {
		return this.set(index, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, long value) {
		return this.set(index, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, float value) {
		return this.set(index, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, double value) {
		return this.set(index, new NumberData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, String value) {
		return this.set(index, new StringData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> setByteList(int index, byte @NotNull ... value) {
		return this.set(index, new ByteListData<>(this.ops, ByteArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> setByteList(int index, @NotNull ByteList value) {
		return this.set(index, new ByteListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> setIntList(int index, int @NotNull ... value) {
		return this.set(index, new IntListData<>(this.ops, IntArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> setIntList(int index, @NotNull IntList value) {
		return this.set(index, new IntListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> setLongList(int index, long @NotNull ... value) {
		return this.set(index, new LongListData<>(this.ops, LongArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> setLongList(int index, @NotNull LongList value) {
		return this.set(index, new LongListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> setList(int index, @NotNull Data<T_Encoded> @NotNull ... value) {
		return this.set(index, new ListData<>(this.ops, ObjectArrayList.wrap(value)));
	}

	public @NotNull Data<T_Encoded> setList(int index, @NotNull ObjectList<@NotNull Data<T_Encoded>> value) {
		return this.set(index, new ListData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> setMap(int index, @NotNull Object2ObjectMap<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) {
		return this.set(index, new MapData<>(this.ops, value));
	}

	public @NotNull Data<T_Encoded> set(int index, @NotNull Data<T_Encoded> newData) {
		return this.value.set(index, newData);
	}

	public void append(int index, boolean value) {
		this.append(index, new BooleanData<>(this.ops, value));
	}

	public void append(int index, byte value) {
		this.append(index, new NumberData<>(this.ops, value));
	}

	public void append(int index, short value) {
		this.append(index, new NumberData<>(this.ops, value));
	}

	public void append(int index, int value) {
		this.append(index, new NumberData<>(this.ops, value));
	}

	public void append(int index, long value) {
		this.append(index, new NumberData<>(this.ops, value));
	}

	public void append(int index, float value) {
		this.append(index, new NumberData<>(this.ops, value));
	}

	public void append(int index, double value) {
		this.append(index, new NumberData<>(this.ops, value));
	}

	public void append(int index, String value) {
		this.append(index, new StringData<>(this.ops, value));
	}

	public void appendByteList(int index, byte @NotNull ... value) {
		this.append(index, new ByteListData<>(this.ops, ByteArrayList.wrap(value)));
	}

	public void appendByteList(int index, @NotNull ByteList value) {
		this.append(index, new ByteListData<>(this.ops, value));
	}

	public void appendIntList(int index, int @NotNull ... value) {
		this.append(index, new IntListData<>(this.ops, IntArrayList.wrap(value)));
	}

	public void appendIntList(int index, @NotNull IntList value) {
		this.append(index, new IntListData<>(this.ops, value));
	}

	public void appendLongList(int index, long @NotNull ... value) {
		this.append(index, new LongListData<>(this.ops, LongArrayList.wrap(value)));
	}

	public void appendLongList(int index, @NotNull LongList value) {
		this.append(index, new LongListData<>(this.ops, value));
	}

	public void appendList(int index, @NotNull Data<T_Encoded> @NotNull ... value) {
		this.append(index, new ListData<>(this.ops, ObjectArrayList.wrap(value)));
	}

	public void appendList(int index, @NotNull ObjectList<@NotNull Data<T_Encoded>> value) {
		this.append(index, new ListData<>(this.ops, value));
	}

	public void appendMap(int index, @NotNull Object2ObjectMap<@NotNull Data<T_Encoded>, @NotNull Data<T_Encoded>> value) {
		this.append(index, new MapData<>(this.ops, value));
	}

	public void append(int index, @NotNull Data<T_Encoded> newData) {
		this.value.add(index, newData);
	}

	@Override
	public boolean equals(Object object) {
		ListData<?> list;
		return object instanceof Data<?> data && (list = data.tryAsList()) != null && this.value.equals(list.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	@Override
	public @NotNull Data<T_Encoded> deepCopy() {
		return new ListData<>(this.ops, this.value.stream().map(Data<T_Encoded>::deepCopy).collect(Collectors.toCollection(ObjectArrayList::new)));
	}
}