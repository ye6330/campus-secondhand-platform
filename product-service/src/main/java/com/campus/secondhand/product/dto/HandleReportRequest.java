package com.campus.secondhand.product.dto;

import javax.validation.constraints.Size;

public class HandleReportRequest {

    @Size(max = 200)
    private String handleNote;

    public String getHandleNote() {
        return handleNote;
    }

    public void setHandleNote(String handleNote) {
        this.handleNote = handleNote;
    }
}
