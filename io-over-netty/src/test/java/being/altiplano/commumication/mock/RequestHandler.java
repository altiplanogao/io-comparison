package being.altiplano.commumication.mock;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class RequestHandler implements Function<MRequest, MResponse> {
    @Override
    public MResponse apply(MRequest mRequest) {
        String input = mRequest.getData();
        switch (mRequest.code) {
            case UPPER:
                return new MResponse(input.toUpperCase());
            case LOWER:
                return new MResponse(input.toLowerCase());
            case ECHO:
                return new MResponse(input);
            case ECHO_TWICE:
                return new MResponse(input + input);
            case REVERSE:
                return new MResponse(StringUtils.reverse(input));
            case COUNT:
                return new MResponse(Integer.toString(input.length()));
            case NO_REPLY:
                return new MResponse();
            default:
                throw new NotImplementedException("Unexpected " + mRequest.code);
        }
    }
}
