package path;

import java.lang.reflect.Method;
import java.util.List;

import passport.Passport.PassportType;

public record Planet(Method method, List<PassportType> param, List<String> pathParams) {}
