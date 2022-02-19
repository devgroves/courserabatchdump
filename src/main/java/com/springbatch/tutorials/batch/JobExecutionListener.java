package com.springbatch.tutorials.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;

import java.util.Date;

public class JobExecutionListener extends JobExecutionListenerSupport {

    public void afterJob(JobExecution jobExecution) {
        System.out.println("after Job Time" + new Date().toString());
    }

    public void beforeJob(JobExecution jobExecution) {
        System.out.println("before job time " + new Date().toString());
        jobExecution.getExecutionContext().put("startTime", new Date());
    }

}
