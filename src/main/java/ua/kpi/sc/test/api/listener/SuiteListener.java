package ua.kpi.sc.test.api.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.xml.XmlSuite;
import ua.kpi.sc.test.api.config.Config;

public class SuiteListener implements ISuiteListener {

    private static final Logger log = LoggerFactory.getLogger(SuiteListener.class);

    @Override
    public void onStart(ISuite suite) {
        String parallel = Config.execution().getParallel();
        int threadCount = Config.execution().getThreadCount();

        XmlSuite xmlSuite = suite.getXmlSuite();

        if (!"none".equalsIgnoreCase(parallel)) {
            xmlSuite.setParallel(XmlSuite.ParallelMode.getValidParallel(parallel));
            xmlSuite.setThreadCount(threadCount);
            log.info("Suite '{}' configured: parallel={}, threads={}",
                    suite.getName(), parallel, threadCount);
        }

        String excludedGroups = System.getProperty("excludeGroups");
        if (excludedGroups != null && !excludedGroups.isEmpty()) {
            log.info("Excluded groups: {}", excludedGroups);
        }
    }

    @Override
    public void onFinish(ISuite suite) {
        log.info("Suite '{}' finished. Passed: {}, Failed: {}, Skipped: {}",
                suite.getName(),
                suite.getResults().values().stream()
                        .mapToInt(r -> r.getTestContext().getPassedTests().size()).sum(),
                suite.getResults().values().stream()
                        .mapToInt(r -> r.getTestContext().getFailedTests().size()).sum(),
                suite.getResults().values().stream()
                        .mapToInt(r -> r.getTestContext().getSkippedTests().size()).sum());
    }
}
