package com.vei.ems.dto;

import java.util.Map;

/** Every error from the API has this shape, so the front end can show it easily. */
public record ErrorResponse(int status, String message, Map<String, String> errors) {
}
