package procotol;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public enum DoorWay {

    GET("GET"),
    POST("POST"),
    PUT("PUT"),
    DELETE("DELETE");

    public final String methodName;

    private DoorWay(final String method) {
        this.methodName = method;
    }

    public static DoorWay parse(final String methodName) {
        for (DoorWay method : DoorWay.values()) {
            if (method.methodName.equals(methodName)) {  
                return method;
            }
        }
        return null;
    }

    // Get annotation.
    @Inherited
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Gets.class)
    public @interface Get {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    public @interface Gets {
        Get[] value();
    }

    // Post annotation.
    @Inherited
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Posts.class)
    public @interface Post {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    public @interface Posts {
        Post[] value();
    }

    // Put annotation.
    @Inherited
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Puts.class)
    public @interface Put {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    public @interface Puts {
        Put[] value();
    }

    // Delete annotation.
    @Inherited
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @Repeatable(Deletes.class)
    public @interface Delete {
        String value();
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    public @interface Deletes {
        Delete[] value();
    }

}
