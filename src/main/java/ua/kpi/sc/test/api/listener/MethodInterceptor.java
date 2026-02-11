package ua.kpi.sc.test.api.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IMethodInstance;
import org.testng.IMethodInterceptor;
import org.testng.ITestContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MethodInterceptor implements IMethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(MethodInterceptor.class);

    @Override
    public List<IMethodInstance> intercept(List<IMethodInstance> methods, ITestContext context) {
        String includeGroupsProp = System.getProperty("includeGroups");
        String excludeGroupsProp = System.getProperty("excludeGroups");

        if (includeGroupsProp == null && excludeGroupsProp == null) {
            return methods;
        }

        Set<String> includeGroups = parseGroups(includeGroupsProp);
        Set<String> excludeGroups = parseGroups(excludeGroupsProp);

        List<IMethodInstance> filtered = new ArrayList<>();

        for (IMethodInstance method : methods) {
            String[] groups = method.getMethod().getGroups();
            if (groups == null) {
                groups = new String[0];
            }

            Set<String> methodGroups = Set.of(groups);

            if (!excludeGroups.isEmpty() && methodGroups.stream().anyMatch(excludeGroups::contains)) {
                log.debug("Excluding method: {} (groups: {})",
                        method.getMethod().getMethodName(), methodGroups);
                continue;
            }

            if (!includeGroups.isEmpty() && methodGroups.stream().noneMatch(includeGroups::contains)) {
                log.debug("Filtering out method: {} (groups: {})",
                        method.getMethod().getMethodName(), methodGroups);
                continue;
            }

            filtered.add(method);
        }

        log.info("Method interceptor: {} of {} methods selected", filtered.size(), methods.size());
        return filtered;
    }

    private Set<String> parseGroups(String groupsStr) {
        if (groupsStr == null || groupsStr.isBlank()) {
            return Set.of();
        }
        return Arrays.stream(groupsStr.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
}
