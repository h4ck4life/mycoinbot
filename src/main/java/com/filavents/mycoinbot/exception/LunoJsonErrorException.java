package com.filavents.mycoinbot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="Error in external api response")
public class LunoJsonErrorException extends RuntimeException {
}
