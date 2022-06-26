package com.springbatch.tutorials.batch.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseResponse {

    private List<CourseMetaData> elements;

    private PageModel paging;


    public List<CourseMetaData> getElements() {
        return elements;
    }

    public void setElements(List<CourseMetaData> elements) {
        this.elements = elements;
    }

    public PageModel getPaging() {
        return paging;
    }

    public void setPaging(PageModel paging) {
        this.paging = paging;
    }

    public static class PageModel {
        private String next;
        private String total;

        public String getNext() {
            return next;
        }

        public Integer getNextValue() {
            return Integer.parseInt(next);
        }

        public void setNext(String next) {
            this.next = next;
        }

        public String getTotal() {
            return total;
        }

        public Integer getTotalValue() {
            return Integer.parseInt(total);
        }

        public boolean isNextNull() {
            return this.next == null;
        }

        public void setTotal(String total) {
            this.total = total;
        }

        @Override
        public String toString() {
            return "PageModel{" +
                    "next='" + next + '\'' +
                    ", total='" + total + '\'' +
                    '}';
        }
    }

}
