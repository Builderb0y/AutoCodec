package builderb0y.autocodec.reflection.manipulators;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import builderb0y.autocodec.AutoCodec;
import builderb0y.autocodec.annotations.UseGetter;
import builderb0y.autocodec.annotations.UseSetter;
import builderb0y.autocodec.reflection.ReflectionManager;
import builderb0y.autocodec.reflection.manipulators.impl.InstanceReaderImpl;
import builderb0y.autocodec.reflection.manipulators.impl.InstanceReaderWriterImpl;
import builderb0y.autocodec.reflection.manipulators.impl.InstanceWriterImpl;
import builderb0y.autocodec.reflection.memberViews.FieldLikeMemberView;
import builderb0y.autocodec.reflection.memberViews.FieldView;
import builderb0y.autocodec.reflection.reification.ReifiedType;

import static org.junit.Assert.*;

public class MemberManipulatorTest {

	public static final int
		IS_READER       = 1 << 0,
		IS_WRITER       = 1 << 1,
		BYPASSES_ACCESS = 1 << 2;

	public static final AutoCodec AUTO_CODEC = new AutoCodec() {

		@Override
		public @NotNull ReflectionManager createReflectionManager() {
			return new ReflectionManager() {

				@Override
				public boolean canView(@NotNull Field field) {
					return true;
				}
			};
		}
	};

	public static record Creator(
		@NotNull String name,
		@NotNull Creator.Impl impl,
		int flags
	) {
		public static interface Impl {
			public abstract @NotNull InstanceManipulator<Fields, Integer> of(@NotNull FieldView<Fields, Integer> field) throws IllegalAccessException;
		}
		public boolean isReader() { return (this.flags & IS_READER) != 0; }
		public boolean isWriter() { return (this.flags & IS_WRITER) != 0; }
		public boolean bypassesAccess() { return (this.flags & BYPASSES_ACCESS) != 0; }

		public static final @NotNull Creator @NotNull [] VALUES = {
			new Creator("InstanceReaderImpl (automatic)", field -> InstanceReaderImpl.of(field, field.createInstanceReaderHandle(AUTO_CODEC)), IS_READER | BYPASSES_ACCESS),
			new Creator("InstanceWriterImpl (automatic)", field -> InstanceWriterImpl.of(field, field.createInstanceWriterHandle(AUTO_CODEC)), IS_WRITER | BYPASSES_ACCESS),
			new Creator("InstanceReaderWriterImpl (automatic)", field -> InstanceReaderWriterImpl.of(field, field.createInstanceReaderHandle(AUTO_CODEC), field.createInstanceWriterHandle(AUTO_CODEC)), IS_READER | IS_WRITER | BYPASSES_ACCESS),

			new Creator("InstanceReaderImpl (manual)", field -> InstanceReaderImpl.of(field, getterMethodHandle(field)), IS_READER),
			new Creator("InstanceWriterImpl (manual)", field -> InstanceWriterImpl.of(field, setterMethodHandle(field)), IS_WRITER),
			new Creator("InstanceReaderWriterImpl (manual)", field -> InstanceReaderWriterImpl.of(field, getterMethodHandle(field), setterMethodHandle(field)), IS_READER | IS_WRITER),
		};

		public static @NotNull MethodHandle getterMethodHandle(@NotNull FieldView<Fields, Integer> field) throws IllegalAccessException {
			return MethodHandles.publicLookup().unreflectGetter(field.getActualMember());
		}

		public static @NotNull MethodHandle setterMethodHandle(@NotNull FieldView<Fields, Integer> field) throws IllegalAccessException {
			return MethodHandles.publicLookup().unreflectSetter(field.getActualMember());
		}
	}

	@Test
	public void testAutomatics() throws IllegalAccessException {
		FieldLikeMemberView<Fields, ?>[] members = AUTO_CODEC.reflect(ReifiedType.from(Fields.class)).getFields(false);
		assertEquals(8, members.length);
		for (int index = 0; index < 8; index++) {
			@SuppressWarnings("unchecked")
			FieldView<Fields, Integer> field = (FieldView<Fields, Integer>)(members[index]);
			for (Creator creator : Creator.VALUES) {
				boolean expectSuccess = (creator.bypassesAccess() || Modifier.isPublic(field.field.getModifiers())) && (!creator.isWriter() || !Modifier.isFinal(field.field.getModifiers()));
				if (expectSuccess) {
					Fields fields = new Fields();
					InstanceManipulator<Fields, Integer> manipulator = creator.impl.of(field);
					if (creator.isReader()) {
						assertEquals(index, ((InstanceReader<Fields, Integer>)(manipulator)).get(fields).intValue());
					}
					if (creator.isWriter()) {
						((InstanceWriter<Fields, Integer>)(manipulator)).set(fields, index + 100);
						if (creator.isReader()) {
							assertEquals(index + 100, ((InstanceReader<Fields, Integer>)(manipulator)).get(fields).intValue());
						}
					}
				}
				else {
					try {
						creator.impl.of(field);
						fail("Should not be able to create " + creator.name + " for field " + field);
					}
					catch (IllegalAccessException expected) {}
				}
			}
		}
	}

	public static class Fields {

		@UseGetter("getPublicField")
		@UseSetter("setPublicField")
		public int publicField = 0;
		@UseGetter("getProtectedField")
		@UseSetter("setProtectedField")
		protected int protectedField = 1;
		@UseGetter("getPackageField")
		@UseSetter("setPackageField")
		int packageField = 2;
		@UseGetter("getPrivateField")
		@UseSetter("setPrivateField")
		private int privateField = 3;
		@UseGetter("getPublicFinalField")
		public final int publicFinalField = 4;
		@UseGetter("getProtectedFinalField")
		protected final int protectedFinalField = 5;
		@UseGetter("getPackageFinalField")
		final int packageFinalField = 6;
		@UseGetter("getPrivateFinalField")
		private final int privateFinalField = 7;

		public int getPublicField() {
			return this.publicField;
		}

		public void setPublicField(int publicField) {
			this.publicField = publicField;
		}

		public int getProtectedField() {
			return this.protectedField;
		}

		public void setProtectedField(int protectedField) {
			this.protectedField = protectedField;
		}

		public int getPackageField() {
			return this.packageField;
		}

		public void setPackageField(int packageField) {
			this.packageField = packageField;
		}

		public int getPrivateField() {
			return this.privateField;
		}

		public void setPrivateField(int privateField) {
			this.privateField = privateField;
		}

		public int getPublicFinalField() {
			return this.publicFinalField;
		}

		public int getProtectedFinalField() {
			return this.protectedFinalField;
		}

		public int getPackageFinalField() {
			return this.packageFinalField;
		}

		public int getPrivateFinalField() {
			return this.privateFinalField;
		}
	}
}