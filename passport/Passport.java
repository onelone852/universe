package passport;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedType;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class Passport {

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface DoorWay {}

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Observable {}

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface Waiter {}

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface ResponseWriter {}

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface PathParameter {
        String value();
    }

    @Target(ElementType.TYPE_USE)
    @Retention(RetentionPolicy.RUNTIME)
    public static @interface AccessVersion {}

    public static sealed interface PassportType permits PassportType.DoorWay, PassportType.Observable, PassportType.Waiter, PassportType.ResponseWriter, PassportType.PathParameter, PassportType.AccessVersion {
        public static record DoorWay() implements PassportType {}
        public static record Observable() implements PassportType {}
        public static record Waiter() implements PassportType {}
        public static record ResponseWriter() implements PassportType {}
        public static record PathParameter(String name) implements PassportType {}
        public static record AccessVersion() implements PassportType {}

        public static List<PassportType> buildPassportTypes(final AnnotatedType[] types) throws IllegalArgumentException {

            final Stream<PassportType> passportsStream = Arrays.stream(types).map((AnnotatedType at) -> {
                if (at.isAnnotationPresent(Passport.DoorWay.class)) {
                    return new PassportType.DoorWay();
                } else if (at.isAnnotationPresent(Passport.Observable.class)) {
                    return new PassportType.Observable();
                } else if (at.isAnnotationPresent(Passport.Waiter.class)) {
                    return new PassportType.Waiter();
                } else if (at.isAnnotationPresent(Passport.ResponseWriter.class)) {
                    return new PassportType.ResponseWriter();
                } else if (at.isAnnotationPresent(Passport.PathParameter.class)) {
                    String name = at.getAnnotation(Passport.PathParameter.class).value();
                    return new PassportType.PathParameter(name);
                } else if (at.isAnnotationPresent(Passport.AccessVersion.class)) {
                    return new PassportType.AccessVersion();
                }
                return null;
            });
            final List<PassportType> passports = passportsStream.toList();
            
            if (passports.contains(null)) {
                throw new IllegalArgumentException();
            }
            return passports;

        }
    }
}
