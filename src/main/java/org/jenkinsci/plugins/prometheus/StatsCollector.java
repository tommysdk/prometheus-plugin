package org.jenkinsci.plugins.prometheus;

import io.prometheus.client.Collector;
import io.prometheus.client.Summary;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class StatsCollector extends Collector {

    private static final Logger LOG = LoggerFactory.getLogger(StatsCollector.class);
    private static final String DEFAULT_NAMESPACE = "default";

    private String namespace;

    public StatsCollector() {
        namespace = System.getenv("PROMETHEUS_NAMESPACE");
        if (StringUtils.isEmpty(namespace)) {
            LOG.debug("Since the environment variable 'PROMETHEUS_NAMESPACE' is empty, using 'default'");
            namespace = DEFAULT_NAMESPACE;
        }
        LOG.info("The prometheus namespace is [{}]", namespace);
    }

    @Override
    public List<MetricFamilySamples> collect() {
        LOG.debug("Collecting metrics for prometheus");
        final List<MetricFamilySamples> samples = new ArrayList<>();
        final String fullname = "builds";
        final String subsystem = "jenkins";
        String[] labelNameArray = {"job"};

        LOG.debug("getting summary of build times in milliseconds by Job");
        Summary summary = Summary.build().
                name(fullname + "_duration_milliseconds_summary").
                subsystem(subsystem).namespace(namespace).
                labelNames(labelNameArray).
                help("Summary of Jenkins build times in milliseconds by Job").
                create();

        String labelValueArray = "jobName";
        long buildDuration = 0L;
        summary.labels(labelValueArray).observe(buildDuration);

        LOG.debug("getting summary of build times by Job and Stage");

        if (summary.collect().get(0).samples.size() > 0) {
            LOG.debug("Adding [{}] samples from summary", summary.collect().get(0).samples.size());
            samples.addAll(summary.collect());
        }
        return samples;
    }
}