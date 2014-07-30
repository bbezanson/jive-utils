package com.jivesoftware.os.jive.utils.hwal.shared.hbase;

import com.google.common.base.Optional;

public class SipWALColumnKey {
    private final byte sort;
    private final long collisionId;
    private final Optional<Long> sipId;

    public SipWALColumnKey(byte sort, long collisionId, long sipId) {
        this.sort = sort;
        this.collisionId = collisionId;
        this.sipId = Optional.of(sipId);
    }

    /** Only used for reading from the sip WAL */
    public SipWALColumnKey(byte sort, long collisionId) {
        this.sort = sort;
        this.collisionId = collisionId;
        this.sipId = Optional.absent();
    }

    public byte getSort() {
        return sort;
    }

    public long getCollisionId() {
        return collisionId;
    }

    public Optional<Long> getSipId() {
        return sipId;
    }

    @Override
    public String toString() {
        return "MiruActivitySipWALColumnKey{" +
            "sort=" + sort +
            ", collisionId=" + collisionId +
            ", sipId=" + sipId +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SipWALColumnKey that = (SipWALColumnKey) o;

        if (collisionId != that.collisionId) {
            return false;
        }
        if (sort != that.sort) {
            return false;
        }
        if (sipId != null ? !sipId.equals(that.sipId) : that.sipId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) sort;
        result = 31 * result + (int) (collisionId ^ (collisionId >>> 32));
        result = 31 * result + (sipId != null ? sipId.hashCode() : 0);
        return result;
    }
}
