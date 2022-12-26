package nl.tudelft.sem.template.request.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Request was not found")
public class RequestNotFoundException extends RuntimeException {
    static final long serialVersionUID = -7349827349823719L;
}
