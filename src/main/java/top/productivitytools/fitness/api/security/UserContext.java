package top.productivitytools.fitness.api.security;

import top.productivitytools.fitness.api.entities.FitnessUser;

public final class UserContext {
    private static final ThreadLocal<FitnessUser> CURRENT_USER = new ThreadLocal<>();

    private UserContext() {}

    public static void setCurrentUser(FitnessUser user) {
        CURRENT_USER.set(user);
    }

    public static FitnessUser getCurrentUser() {
        return CURRENT_USER.get();
    }

    public static void clear() {
        CURRENT_USER.remove();
    }
}
