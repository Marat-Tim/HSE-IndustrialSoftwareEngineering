package ru.marattim.todolist.tests_infrastructure;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.lang.NonNull;
import org.springframework.test.context.ActiveProfilesResolver;

public class TestProfileResolver implements ActiveProfilesResolver {

    private static final String TEST_PROFILE = "test";

    @Override
    @NonNull
    public String[] resolve(@NonNull Class<?> testClass) {
        System.setProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME, TEST_PROFILE);
        return new String[]{TEST_PROFILE};
    }

}