package builderb0y.autocodec.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
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