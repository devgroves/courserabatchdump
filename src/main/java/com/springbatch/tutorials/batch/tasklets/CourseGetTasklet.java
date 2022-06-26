package com.springbatch.tutorials.batch.tasklets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springbatch.tutorials.batch.model.CourseResponse;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class CourseGetTasklet implements Tasklet, StepExecutionListener {

    private static final Logger logger = LoggerFactory
            .getLogger(CourseGetTasklet.class);
    private static final int PAGE_LIMIT = 100;

    private ObjectMapper objectMapper;

    private static class IterationContext {
        private Integer times;
        private Integer offset;

        public IterationContext(Integer times, Integer offset) {
            this.times = times;
            this.offset = offset;
        }

        public Integer getTimes() {
            return times;
        }

        public Integer getOffset() {
            return offset;
        }

        public void incrementTimes() {
            this.times++;
        }

        public void setOffset(Integer offset) {
            this.offset = offset;
        }
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

                int noOfTimes  =
                        stepContribution.getStepExecution().getExecutionContext().getInt(
                                "noOfTimes", 0);
                int offset = stepContribution.getStepExecution().getExecutionContext().getInt(
                        "offset", 0);

                StringBuilder courseraUrl =
                        new StringBuilder("https://api.coursera.org/api/courses.v1?start=").append(String.valueOf(offset)).append("&limit=").append(String.valueOf(PAGE_LIMIT));

                CourseResponse courseResponse = null;
                CloseableHttpClient httpClient = HttpClients.createDefault();
                logger.info("Get the courseurl {}", courseraUrl.toString());

                try(FileWriter fileWriter = new FileWriter("output.json")) {

                    HttpGet request = new HttpGet(courseraUrl.toString());

                    // add request headers
                    request.addHeader("Accept", "applicaiton/json");

                    CloseableHttpResponse response = httpClient.execute(request);
                    System.out.println("response body " + response.getAllHeaders());
                    HttpEntity entity = response.getEntity();
                    if (entity != null) {
                        // return it as a String
                        String result = EntityUtils.toString(entity);
                        courseResponse = objectMapper.readValue(result, CourseResponse.class);
                        fileWriter.write(objectMapper.writeValueAsString(courseResponse.getElements()));
                        logger.info("elements {} paging {}", courseResponse.getElements(), courseResponse.getPaging());

                    }

                } catch (IOException exception) {
                    logger.error("io exception happened {}", exception);
                } catch (Exception exception) {
                    logger.error("general exception happened {}", exception);
                }
                CourseResponse.PageModel paging = courseResponse.getPaging();
                noOfTimes++;

				if (paging.isNextNull() || paging.getNextValue() > paging.getTotalValue())
                    return RepeatStatus.FINISHED;
				else {
                    stepContribution.getStepExecution().getExecutionContext().putInt("noOfTimes",
                            noOfTimes);
                    stepContribution.getStepExecution().getExecutionContext().putInt("offset",
                            paging.getNextValue());
                    return RepeatStatus.CONTINUABLE;
                }
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        logger.info("Before step execution");
        objectMapper = new ObjectMapper();
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        try {
            int noOfTimes  =
                    stepExecution.getExecutionContext().getInt(
                            "noOfTimes", 0);
            int offset = stepExecution.getExecutionContext().getInt(
                    "offset", 0);

            logger.info("After step execution completed {} after running  {} times and last offset {}",
                stepExecution.getStartTime(),
                noOfTimes, offset);
        } catch(Exception exctpion) {
            logger.error("exception ");
        }
        return null;
    }
}
