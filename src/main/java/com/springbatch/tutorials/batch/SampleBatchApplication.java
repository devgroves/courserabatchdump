/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springbatch.tutorials.batch;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.*;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableBatchProcessing
public class SampleBatchApplication {

	@Autowired
	private JobBuilderFactory jobs;


	@Autowired
	private StepBuilderFactory steps;

	@Autowired private AnnotationConfigApplicationContext applicationContext;


	@Bean
	protected Tasklet tasklet() {
		return new Tasklet() {
			@Override
			public RepeatStatus execute(StepContribution contribution,
					ChunkContext context) {

				Date startTime = (Date) context.getStepContext().getJobExecutionContext().get("startTime");
				Date currentTime = new Date();

				Integer value = (Integer) context.getAttribute("times");

				if ( value == null) {
					try {
						Job job = jobs.get("childjob").incrementer(new RunIdIncrementer())
								.listener(new JobExecutionListener()).start(step1()).build();
					} catch (Exception exception) {
						exception.printStackTrace();
						System.out.println("getting exception " + exception.getLocalizedMessage());
					}
					value = 0;
				}

				context.setAttribute("times", ++value);
				ExecutorService executorService = executorService( 5);
				executorService.shutdownNow();
				System.out.println(executorService.toString());
				if (currentTime.getTime() - startTime.getTime() > 5000)
					return RepeatStatus.FINISHED;
				else
					return RepeatStatus.CONTINUABLE;
			}
		};
	}

	@Bean
	@Scope("prototype")
	public ExecutorService executorService(Integer threadCount) {

		ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
		return executorService;
	}

	@Bean
	public Job job() throws Exception {
		return this.jobs.get("job").incrementer(new RunIdIncrementer())
				.listener(new JobExecutionListener()).start(step1()).build();
	}


	@Bean
	protected Step step1() throws Exception {
		String epochStr = String.valueOf(new Date().getTime());
		return this.steps.get("step1v" + epochStr).tasklet(tasklet()).throttleLimit(1).build();
	}


	public static void main(String[] args) throws Exception {
		// System.exit is common for Batch applications since the exit code can be used to
		// drive a workflow
		System.exit(SpringApplication.exit(SpringApplication.run(
				SampleBatchApplication.class, args)));
	}
}
