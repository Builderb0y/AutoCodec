package builderb0y.autocodec.reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import builderb0y.autocodec.util.TypeFormatter;

/**
common logic for finding members from a {@link ReflectContext}.
the implementations of this interface allow you to get
the first member, the only member, or all the members.
this interface is also extensible to allow users to create their own collectors.
for example, maybe you'd want the *last* member.
that is not provided by default, but you can implement it quite easily.
*/
public interface MemberCollector<T_Member, T_Collection> {

	/**
	called to indicate that a member matched the given predicate.
	returns true if the loop through all members should be aborted,
	or false if more members are desired.
	throws {@link ReflectException} if the
	*/
	public abstract boolean accept(@NotNull T_Member member) throws ReflectException;

	/**
	called after all available members have been passed through {@link #accept(Object)}.
	the return value of this method is passed back to
	the caller trying to find members in the first place.
	*/
	public abstract T_Collection getResult() throws ReflectException;

	/**
	used for logging.
	the format is: "finding (searchType) in ..."
	should return something like "first method" or "all fields".
	the "memberType" parameter will be set to either "field" or "method".
	*/
	public abstract String searchType(String memberType);

	/**
	returns a MemberCollector which yields the first member it encounters,
	or null if no members matched the provided predicate.
	*/
	public static <T_Member> @NotNull MemberCollector<T_Member, T_Member> tryFirst() {
		return new First<>(false);
	}

	/**
	returns a MemberCollector which yields the first member it encounters,
	or throws a {@link ReflectException} if no members matched the provided predicate.
	*/
	public static <T_Member> @NotNull MemberCollector<T_Member, T_Member> forceFirst() {
		return new First<>(true);
	}

	/**
	returns a MemberCollector which yields the only member it encounters.
	the returned MemberCollector will yield null if it encounters 0 or more than 1 member.
	*/
	public static <T_Member> @NotNull MemberCollector<T_Member, T_Member> tryUnique() {
		return new One<>(false, false);
	}

	/**
	returns a MemberCollector which yields the only member it encounters.
	the returned MemberCollector will throw a {@link ReflectException}
	if it encounters 0 or more than 1 member.
	*/
	public static <T_Member> @NotNull MemberCollector<T_Member, T_Member> forceUnique() {
		return new One<>(true, true);
	}

	/**
	returns a MemberCollector which yields every member it encounters.
	if no members match the provided predicate,
	then the returned MemberCollector will yield an empty List.
	*/
	public static <T_Member> @NotNull MemberCollector<T_Member, List<T_Member>> tryAll() {
		return new Many<>(false, false);
	}

	/**
	returns a MemberCollector which yields every member it encounters.
	if no members match the provided predicate,
	then the returned MemberCollector will throw a {@link ReflectException}.
	*/
	public static <T_Member> @NotNull MemberCollector<T_Member, List<T_Member>> forceAll() {
		return new Many<>(true, false);
	}

	public static <T_Member> @NotNull MemberCollector<T_Member, T_Member> expectOne(boolean throwOnZero, boolean throwOnMany) {
		return new One<>(throwOnZero, throwOnMany);
	}

	public static <T_Member> @NotNull MemberCollector<T_Member, @NotNull List<@NotNull T_Member>> expectMany(boolean throwOnZero, boolean throwOnOne) {
		return new Many<>(throwOnZero, throwOnOne);
	}

	public static @NotNull ReflectException noMatch() {
		return new ReflectException("No members matched the provided predicate.");
	}

	public static <T_Member> @NotNull T_Member requireMatch(@Nullable T_Member member) {
		if (member != null) return member;
		else throw noMatch();
	}

	public static class One<T_Member> implements MemberCollector<T_Member, T_Member> {

		public @Nullable T_Member result;
		public final boolean throwOnZero, throwOnMany;

		public One(boolean throwOnZero, boolean throwOnMany) {
			this.throwOnZero = throwOnZero;
			this.throwOnMany = throwOnMany;
		}

		@Override
		public boolean accept(@NotNull T_Member member) throws ReflectException {
			if (this.result == null) {
				this.result = member;
				return false;
			}
			else {
				if (this.throwOnMany) {
					throw new ReflectException("More than one member matched the provided predicate: " + this.result + " and " + member);
				}
				else {
					this.result = null;
					return true;
				}
			}
		}

		@Override
		public T_Member getResult() throws ReflectException {
			if (this.result == null && this.throwOnZero) {
				throw noMatch();
			}
			return this.result;
		}

		@Override
		public String searchType(String memberType) {
			return "unique " + memberType;
		}

		@Override
		public String toString() {
			return (
				TypeFormatter.appendSimpleClassUnchecked(
					new StringBuilder(32),
					this.getClass()
				)
				.append(": { throwOnZero: ").append(this.throwOnZero)
				.append(", throwOnMany: ").append(this.throwOnMany)
				.append(" }")
				.toString()
			);
		}
	}

	public static class Many<T_Member> implements MemberCollector<T_Member, List<T_Member>> {

		public @Nullable List<T_Member> result;
		public final boolean throwOnZero, throwOnOne;

		public Many(boolean throwOnZero, boolean throwOnOne) {
			this.throwOnZero = throwOnZero;
			this.throwOnOne = throwOnOne;
		}

		@Override
		public boolean accept(@NotNull T_Member member) throws ReflectException {
			if (this.result == null) this.result = new ArrayList<>(4);
			this.result.add(member);
			return false;
		}

		@Override
		public List<T_Member> getResult() throws ReflectException {
			if (this.result == null) {
				if (this.throwOnZero) throw noMatch();
				else return Collections.emptyList();
			}
			else if (this.result.size() == 1) {
				if (this.throwOnOne) throw new ReflectException("Expected more than one match.");
			}
			return this.result;
		}

		@Override
		public String searchType(String memberType) {
			return "all " + memberType + 's';
		}

		@Override
		public String toString() {
			return (
				TypeFormatter.appendSimpleClassUnchecked(
					new StringBuilder(32),
					this.getClass()
				)
				.append(": { throwOnZero: ").append(this.throwOnZero)
				.append(", throwOnOne: ").append(this.throwOnOne)
				.append(" }")
				.toString()
			);
		}
	}

	public static class First<T_Member> implements MemberCollector<T_Member, T_Member> {

		public @Nullable T_Member result;
		public final boolean throwOnZero;

		public First(boolean throwOnZero) {
			this.throwOnZero = throwOnZero;
		}

		@Override
		public boolean accept(@NotNull T_Member member) throws ReflectException {
			this.result = member;
			return true;
		}

		@Override
		public T_Member getResult() throws ReflectException {
			if (this.result == null && this.throwOnZero) {
				throw noMatch();
			}
			return this.result;
		}

		@Override
		public String searchType(String memberType) {
			return "first " + memberType;
		}

		@Override
		public String toString() {
			return (
				TypeFormatter.appendSimpleClassUnchecked(
					new StringBuilder(32),
					this.getClass()
				)
				.append(": { throwOnZero: ").append(this.throwOnZero).append(" }")
				.toString()
			);
		}
	}
}