package io.github.ibuildthecloud.model;

import java.net.URL;

import javax.xml.bind.annotation.XmlTransient;

public class Pagination {

    String marker = null;
    URL first, previous, next;
    Integer limit;
    Long total;
    boolean partial = false;

    public Pagination(Integer maxLimit) {
        this.limit = maxLimit;
    }

    public URL getFirst() {
        return first;
    }

    public void setFirst(URL first) {
        this.first = first;
    }

    public URL getPrevious() {
        return previous;
    }

    public void setPrevious(URL previous) {
        this.previous = previous;
    }

    public URL getNext() {
        return next;
    }

    public void setNext(URL next) {
        this.next = next;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        if ( limit != null && this.limit.intValue() >= limit.intValue()) {
            this.limit = limit;
        }
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public boolean isPartial() {
        return partial;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    @XmlTransient
    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

}
