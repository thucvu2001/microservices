package vn.thucvu.controller.request;

import lombok.Getter;
import vn.thucvu.common.Operation;

import java.io.Serializable;

@Getter
public class SearchField<T> implements Serializable {
    private String field;
    private Operation operation;
    private T value;
}