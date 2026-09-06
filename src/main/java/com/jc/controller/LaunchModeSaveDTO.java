// LaunchModeSaveDTO.java
package com.jc.controller;

import com.jc.entity.LaunchMode;
import lombok.Data;

import java.util.List;

@Data
public class LaunchModeSaveDTO {
    private LaunchMode launchMode;
    private List<String> diagramIds;
}