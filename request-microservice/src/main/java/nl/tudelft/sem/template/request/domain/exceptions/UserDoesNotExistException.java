package nl.tudelft.sem.template.request.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.METHOD_NOT_ALLOWED, reason = "Provided user does not exist")
public class UserDoesNotExistException extends RuntimeException {
    static final long serialVersionUID = -17394613479L;
}
