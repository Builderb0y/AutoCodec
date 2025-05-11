package builderb0y.autocodec.data;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
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

import builderb0y.autocodec.util.ObjectArrayFactory;

public class ListData extends Data implements Iterable<@NotNull Data> {

	public static final @NotNull ObjectArrayFactory<ListData> ARRAY_FACTORY = new ObjectArrayFactory<>(ListData.class);

	public @NotNull List<@NotNull Data> value;

	public ListData() {
		this.value = new ObjectArrayList<>();
	}

	public ListData(int capacity) {
		this.value = new ObjectArrayList<>(capacity);
	}

	public ListData(@NotNull List<@NotNull Data> value) {
		this.value = value;
	}

	public static @NotNull ListData singleton(@NotNull Data contents) {
		List<Data> list = new ObjectArrayList<>(4);
		list.add(contents);
		return new ListData(list);
	}

	public static @NotNull ListData wrap(@NotNull Data @NotNull ... elements) {
		return new ListData(ObjectArrayList.wrap(elements));
	}

	public static @NotNull ListData collect(@NotNull Stream<@NotNull Data> stream) {
		return new ListData(stream.collect(Collectors.toCollection(ObjectArrayList::new)));
	}

	public @NotNull Stream<@NotNull Data> streamNonEmpty() {
		return this.value.stream().filter((Data data) -> data != null && !data.isEmpty());
	}

	@Override
	public <T_NewEncoded> @NotNull T_NewEncoded convert(@NotNull DynamicOps<T_NewEncoded> ops) {
		return ops.createList(this.streamNonEmpty().map((Data element) -> element.convert(ops)));
	}

	@Override
	public void forEach(Consumer<? super @NotNull Data> action) {
		this.value.forEach(action);
	}

	@Override
	public @NotNull Iterator<@NotNull Data> iterator() {
		return this.value.iterator();
	}

	@Override
	public Spliterator<@NotNull Data> spliterator() {
		return this.value.spliterator();
	}

	public int size() {
		return this.value.size();
	}

	public @NotNull Data get(int index) {
		return this.value.get(index);
	}

	public @NotNull Data set(int index, boolean value) {
		return this.set(index, new BooleanData(value));
	}

	public @NotNull Data set(int index, byte value) {
		return this.set(index, new NumberData(value));
	}

	public @NotNull Data set(int index, short value) {
		return this.set(index, new NumberData(value));
	}

	public @NotNull Data set(int index, int value) {
		return this.set(index, new NumberData(value));
	}

	public @NotNull Data set(int index, long value) {
		return this.set(index, new NumberData(value));
	}

	public @NotNull Data set(int index, float value) {
		return this.set(index, new NumberData(value));
	}

	public @NotNull Data set(int index, double value) {
		return this.set(index, new NumberData(value));
	}

	public @NotNull Data set(int index, String value) {
		return this.set(index, new StringData(value));
	}

	public @NotNull Data setByteList(int index, byte @NotNull ... value) {
		return this.set(index, new ByteListData(ByteArrayList.wrap(value)));
	}

	public @NotNull Data setByteList(int index, @NotNull ByteList value) {
		return this.set(index, new ByteListData(value));
	}

	public @NotNull Data setIntList(int index, int @NotNull ... value) {
		return this.set(index, new IntListData(IntArrayList.wrap(value)));
	}

	public @NotNull Data setIntList(int index, @NotNull IntList value) {
		return this.set(index, new IntListData(value));
	}

	public @NotNull Data setLongList(int index, long @NotNull ... value) {
		return this.set(index, new LongListData(LongArrayList.wrap(value)));
	}

	public @NotNull Data setLongList(int index, @NotNull LongList value) {
		return this.set(index, new LongListData(value));
	}

	public @NotNull Data setList(int index, @NotNull Data @NotNull ... value) {
		return this.set(index, new ListData(ObjectArrayList.wrap(value)));
	}

	public @NotNull Data setList(int index, @NotNull ObjectList<@NotNull Data> value) {
		return this.set(index, new ListData(value));
	}

	public @NotNull Data setMap(int index, @NotNull Object2ObjectMap<@NotNull Data, @NotNull Data> value) {
		return this.set(index, new MapData(value));
	}

	public @NotNull Data set(int index, @NotNull Data newData) {
		return this.value.set(index, newData);
	}

	public void append(int index, boolean value) {
		this.append(index, new BooleanData(value));
	}

	public void append(int index, byte value) {
		this.append(index, new NumberData(value));
	}

	public void append(int index, short value) {
		this.append(index, new NumberData(value));
	}

	public void append(int index, int value) {
		this.append(index, new NumberData(value));
	}

	public void append(int index, long value) {
		this.append(index, new NumberData(value));
	}

	public void append(int index, float value) {
		this.append(index, new NumberData(value));
	}

	public void append(int index, double value) {
		this.append(index, new NumberData(value));
	}

	public void append(int index, String value) {
		this.append(index, new StringData(value));
	}

	public void appendByteList(int index, byte @NotNull ... value) {
		this.append(index, new ByteListData(ByteArrayList.wrap(value)));
	}

	public void appendByteList(int index, @NotNull ByteList value) {
		this.append(index, new ByteListData(value));
	}

	public void appendIntList(int index, int @NotNull ... value) {
		this.append(index, new IntListData(IntArrayList.wrap(value)));
	}

	public void appendIntList(int index, @NotNull IntList value) {
		this.append(index, new IntListData(value));
	}

	public void appendLongList(int index, long @NotNull ... value) {
		this.append(index, new LongListData(LongArrayList.wrap(value)));
	}

	public void appendLongList(int index, @NotNull LongList value) {
		this.append(index, new LongListData(value));
	}

	public void appendList(int index, @NotNull Data @NotNull ... value) {
		this.append(index, new ListData(ObjectArrayList.wrap(value)));
	}

	public void appendList(int index, @NotNull ObjectList<@NotNull Data> value) {
		this.append(index, new ListData(value));
	}

	public void appendMap(int index, @NotNull Object2ObjectMap<@NotNull Data, @NotNull Data> value) {
		this.append(index, new MapData(value));
	}

	public void append(int index, @NotNull Data newData) {
		this.value.add(index, newData);
	}

	public @NotNull Data remove(int index) {
		return this.value.remove(index);
	}

	@Override
	public boolean equals(Object object) {
		ListData list;
		return object instanceof Data data && (list = data.tryAsList()) != null && this.value.equals(list.value);
	}

	@Override
	public int hashCode() {
		return this.value.hashCode();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

	public @NotNull ListData shallowCopy() {
		return new ListData(new ObjectArrayList<>(this.value));
	}

	@Override
	public @NotNull ListData deepCopy() {
		return new ListData(this.value.stream().map(Data::deepCopy).collect(Collectors.toCollection(ObjectArrayList::new)));
	}
}