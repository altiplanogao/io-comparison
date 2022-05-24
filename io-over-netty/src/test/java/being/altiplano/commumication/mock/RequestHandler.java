package being.altiplano.commumication.mock;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Function;

public class RequestHandler implements Function<MRequest, MResponse> {
    @Override
    public MResponse apply(MRequest mRequest) {
        String input = mRequest.getData();
        int sn = mRequest.sn;
        switch (mRequest.code) {
            case UPPER:
                return new MResponse(sn, input.toUpperCase());
            case LOWER:
                return new MResponse(sn, input.toLowerCase());
            case ECHO:
                return new MResponse(sn, input);
            case ECHO_TWICE:
                return new MResponse(sn, input + input);
            case REVERSE:
                return new MResponse(sn, StringUtils.reverse(input));
            case COUNT:
                return new MResponse(sn, Integer.toString(input.length()));
            case NO_REPLY:
                return null;
            default:
                throw new NotImplementedException("Unexpected " + mRequest.code);
        }
    }
}
