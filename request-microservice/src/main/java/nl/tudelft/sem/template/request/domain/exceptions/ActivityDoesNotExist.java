package nl.tudelft.sem.template.request.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Provided activity does not exist")
public class ActivityDoesNotExist extends RuntimeException {
    static final long serialVersionUID = -2374827348L;
}
