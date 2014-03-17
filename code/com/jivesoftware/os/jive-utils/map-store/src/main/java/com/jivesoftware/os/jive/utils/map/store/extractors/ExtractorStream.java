package com.jivesoftware.os.jive.utils.map.store.extractors;

/**
 *
 * @author jonathan.colt
 * @param <R>
 * @param <E>
 */
public interface ExtractorStream<R, E extends Throwable> {

    R stream(R v) throws E;

}
